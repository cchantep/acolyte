package controllers

import java.util.Date
import java.sql.{ PreparedStatement, ResultSet, SQLWarning }

import javax.inject.{ Inject, Singleton }

import scala.jdk.CollectionConverters._

import _root_.resource.{ ManagedResource, managed }

import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents,
  Result => PlayResult
}
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText, optional, text }

import play.api.libs.json.{ Json, JsValue, Reads, Writes }

import acolyte.jdbc.AcolyteDSL.{
  connection => AcolyteConnection,
  handleStatement
}
import acolyte.jdbc.{
  DefinedParameter,
  QueryExecution,
  ParameterMetaData,
  UpdateExecution
}

@Singleton
class Acolyte @Inject() (
    cc: ControllerComponents,
    assets: Assets
) extends AbstractController(cc) {

  def welcome: Action[AnyContent] = assets.at(path = "/public", "index.html")

  def healthz: Action[AnyContent] = Action {
    Ok("ok")
  }

  sealed case class RouteData(json: String)

  def setup: Action[AnyContent] = Action { implicit request =>
    Form[Option[RouteData]](
      mapping("json" -> optional(nonEmptyText))(_.map(RouteData))(
        _.map(d => Some(d.json))
      )
    ).bindFromRequest()
      .fold[PlayResult](
        f => Ok(f.errors.toString),
        data => Ok(views.html.setup(data.map(_.json)))
      )
  }

  def run: Action[AnyContent] = Action { implicit request =>
    Form(mapping("json" -> nonEmptyText)(RouteData.apply)(RouteData.unapply))
      .bindFromRequest()
      .fold[PlayResult](
        f => Ok(f.errors.toString),
        data => Ok(views.html.run(data.json))
      )
  }

  sealed case class ExecutionData(
      statement: String,
      json: String,
      parameters: Option[String]
  )

  def executeStatement: Action[AnyContent] = Action { implicit request =>
    Form(
      mapping(
        "statement" -> nonEmptyText,
        "json" -> nonEmptyText,
        "parameters" -> optional(text)
      )(ExecutionData.apply)(ExecutionData.unapply)
    ).bindFromRequest()
      .fold[PlayResult](
        f => Ok(f.errors.toString),
        { data =>
          (for {
            ps <- Reads
              .seq[RouteParameter](routeParamReads)
              .reads(Json.parse(data.parameters.getOrElse("[]")))
            rs <- Reads.seq[Route](routeReads).reads(Json.parse(data.json))
          } yield ps -> rs).fold[PlayResult](
            e => PreconditionFailed(e.mkString),
            {
              case (ps, r :: rs) =>
                executeWithRoutes(data.statement, ps, r :: rs)
              case _ => Ok(Json.toJson(false))
            }
          )
        }
      )
  }

  // ---

  @inline
  private def queryResult(
      res: QueryResult
  ): Either[String, acolyte.jdbc.QueryResult] =
    res match {
      case QueryError(msg) =>
        Right(acolyte.jdbc.QueryResult.Nil.withWarning(msg))
      case RowResult(rows) => Right(rows.asResult)
      case _               => Left(s"Unexpected query result: $res")
    }

  @inline
  private def updateResult(
      res: UpdateResult
  ): Either[String, acolyte.jdbc.UpdateResult] =
    res match {
      case UpdateError(msg) =>
        Right(acolyte.jdbc.UpdateResult.Nothing.withWarning(msg))
      case UpdateCount(c) => Right(new acolyte.jdbc.UpdateResult(c))
      case _              => Left(s"Unexpected update result: $res")
    }

  type QueryHandler = PartialFunction[QueryExecution, acolyte.jdbc.QueryResult]
  type UpdateHandler =
    PartialFunction[UpdateExecution, acolyte.jdbc.UpdateResult]

  case class HandlerData(
      queryPatterns: Seq[String] = Nil,
      queryHandler: Option[QueryHandler] = None,
      updateHandler: Option[UpdateHandler] = None
  )

  @inline
  private def updateHandler(
      ur: UpdateRoute,
      f: => Unit
  ): Either[String, UpdateHandler] =
    ur match {
      case UpdateRoute(RoutePattern(e, Nil), res) =>
        updateResult(res).map { r =>
          { case UpdateExecution(sql, _) if sql.matches(e) => f; r }
        }

      case UpdateRoute(RoutePattern(e, ps), res) =>
        val Params = ps.map(executed)
        updateResult(res).map { r =>
          { case UpdateExecution(sql, Params) if sql.matches(e) => f; r }
        }

      case _ => Left(s"Unexpected update route: $ur")
    }

  @inline
  private def queryHandler(
      r: QueryRoute,
      f: => Unit
  ): Either[String, QueryHandler] =
    r match {
      case QueryRoute(RoutePattern(e, Nil), res) =>
        queryResult(res).map { r =>
          { case QueryExecution(sql, _) if sql.matches(e) => f; r }
        }

      case QueryRoute(RoutePattern(e, ps), res) =>
        val Params = ps.map(executed)
        queryResult(res).map { r =>
          { case QueryExecution(sql, Params) if sql.matches(e) => f; r }
        }

      case _ => Left(s"Unexpected query route: $r")
    }

  @inline
  private def routeHandler(
      i: Int,
      r: Route,
      hd: HandlerData,
      f: Int => Unit
  ): Either[String, HandlerData] =
    r match {
      case qr @ QueryRoute(RoutePattern(e, _), _) =>
        (
          hd.copy(queryPatterns = hd.queryPatterns :+ e),
          queryHandler(qr, f(i))
        ) match {
          case (a @ HandlerData(_, None, _), Right(b)) =>
            Right(a.copy(queryHandler = Some(b)))

          case (a @ HandlerData(_, Some(b), _), Right(c)) =>
            Right(a.copy(queryHandler = Some(b.orElse(c))))

          case (_, Left(err)) => Left(err)
        }

      case ur @ UpdateRoute(_, _) =>
        (hd, updateHandler(ur, f(i))) match {
          case (HandlerData(_, _, None), Right(a)) =>
            Right(hd.copy(updateHandler = Some(a)))

          case (HandlerData(_, _, Some(a)), Right(b)) =>
            Right(hd.copy(updateHandler = Some(a.orElse(b))))

          case (_, Left(err)) => Left(err)
        }
      case _ => Left(s"Unexpected route: $r")
    }

  @annotation.tailrec
  private def handler(
      i: Int,
      routes: Seq[Route],
      f: Int => Unit,
      h: Either[String, HandlerData]
  ): Either[String, HandlerData] =
    (routes, h) match {
      case (r :: rs, Right(hd)) =>
        handler(i + 1, rs, f, routeHandler(i, r, hd, f))
      case _ => h
    }

  @inline
  private def fallbackHandler: UpdateHandler = { case e =>
    sys.error(s"No route handler: $e")
  }

  @inline
  private def executed(p: RouteParameter): DefinedParameter =
    p match {
      case DateParameter(d)  => DefinedParameter(d, ParameterMetaData.Date)
      case FloatParameter(f) => DefinedParameter(f, ParameterMetaData.Float(f))
      case IntParameter(i)   => DefinedParameter(i, ParameterMetaData.Int)
      case _ =>
        DefinedParameter(
          p.asInstanceOf[StringParameter].value,
          ParameterMetaData.Str
        )
    }

  private def execResult(
      sql: String,
      ps: Seq[RouteParameter],
      routes: Seq[Route],
      f: Int => Unit
  ): Either[String, ManagedResource[
    (PreparedStatement, Either[ResultSet, Int])
  ]] =
    handler(0, routes, f, Right(HandlerData())).map { hd =>
      val handleQuery = hd.queryHandler.fold(handleStatement) { qh =>
        handleStatement
          .withQueryDetection(hd.queryPatterns: _*)
          .withQueryHandler(qh.orElse { case e =>
            sys.error(s"No route handler: $e")
          })
      }

      for {
        con <- managed(
          AcolyteConnection(
            hd.updateHandler.fold(
              handleQuery.withUpdateHandler(fallbackHandler)
            ) { uh =>
              handleQuery.withUpdateHandler(uh.orElse(fallbackHandler))
            }
          )
        )

        x <- managed {
          ps.foldLeft(1 -> con.prepareStatement(sql)) { (st, p) =>
            (st, p) match {
              case ((i, s), StringParameter(v)) =>
                s.setString(i, v); i + 1 -> s
              case ((i, s), FloatParameter(v)) =>
                s.setFloat(i, v); i + 1 -> s
              case ((i, s), IntParameter(v)) =>
                s.setInt(i, v); i + 1 -> s
              case ((i, s), DateParameter(v)) =>
                s.setDate(i, new java.sql.Date(v.getTime)); i + 1 -> s
              case _ => st
            }
          }._2
        }.map { st =>
          if (st.execute()) st -> Left(st.getResultSet)
          else st -> Right(st.getUpdateCount)
        }
      } yield x
    }

  private implicit object SQLWarningWrites extends Writes[SQLWarning] {
    def writes(w: SQLWarning): JsValue =
      Json.obj(
        "reason" -> w.getMessage,
        "errorCode" -> w.getErrorCode,
        "cause" -> Option(w.getCause).map(_.getMessage)
      )
  }

  @inline
  private def executeWithRoutes(
      stmt: String,
      ps: Seq[RouteParameter],
      routes: Seq[Route]
  ): PlayResult = {
    var r: Int = -1

    @annotation.tailrec
    def jsonResultSet(
        rs: ResultSet,
        c: Int,
        js: Seq[Iterable[JsValue]]
    ): Seq[Iterable[JsValue]] =
      if (rs.next()) {
        jsonResultSet(
          rs,
          c,
          js :+ (for {
            i <- 1 to c
          } yield rs.getObject(i) match {
            case d: Date => Json.toJson(DateFormat.format(d))
            case v       => Json.toJson(v.toString)
          })
        )
      } else js

    (for {
      exe <- execResult(stmt, ps, routes, { r = _ })
      state <- exe
        .acquireFor { x =>
          val (st, res) = x
          res.fold[JsValue](
            { rs =>
              if (rs.getWarnings != null) {
                Json.toJson(
                  Map(
                    "route" -> Json.toJson(r),
                    "warning" -> Json.toJson(rs.getWarnings)
                  )
                )
              } else {
                val meta = rs.getMetaData
                val c = meta.getColumnCount
                val ts: Seq[String] = routes(r) match {
                  case QueryRoute(_, RowResult(rows)) =>
                    rows.getColumnClasses.asScala.map { cl =>
                      val n = cl.getName

                      if (n == "java.util.Date") "date"
                      else if (n == "java.lang.String") "string"
                      else if (n == "java.lang.Integer" || n == "int") "int"
                      else if (n == "java.lang.Float" || n == "float") "float"
                      else n
                    }.toSeq
                  case _ => Nil
                }

                val ls: Iterable[Map[String, String]] =
                  for (i <- 1 to c) yield {
                    Map(
                      "_type" -> ts.lift(i - 1).getOrElse("string"),
                      "name" -> Option(meta.getColumnLabel(i))
                        .orElse(Option(meta.getColumnName(i)))
                        .getOrElse(s"Column #$i")
                    )
                  }

                Json.toJson(
                  Map(
                    "route" -> Json.toJson(r),
                    "schema" -> Json.toJson(ls),
                    "rows" -> Json.toJson(jsonResultSet(rs, c, Nil))
                  )
                )
              }
            },
            { uc =>
              if (st.getWarnings != null) {
                Json.toJson(
                  Map(
                    "route" -> Json.toJson(r),
                    "warning" -> Json.toJson(st.getWarnings)
                  )
                )
              } else Json.toJson(Map("route" -> r, "updateCount" -> uc))
            }
          )
        }
        .left
        .map(_.mkString)
    } yield state).fold(
      err => InternalServerError(Json.toJson(Map("exception" -> err))),
      Ok(_)
    )
  }
}
