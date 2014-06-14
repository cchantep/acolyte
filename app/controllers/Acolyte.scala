package controllers

import java.util.Date
import java.sql.{ PreparedStatement, ResultSet, SQLWarning }

import resource.{ ManagedResource, managed }

import play.api.mvc.{ Action, Controller, SimpleResult }
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText, optional, text }

import play.api.libs.json.{ Json, JsResult, JsValue, Reads, Writes }

import acolyte.Acolyte.{ connection ⇒ AcolyteConnection, handleStatement }
import acolyte.{
  Execution,
  ExecutedStatement,
  QueryExecution,
  Result ⇒ AcolyteResult,
  UpdateExecution
}

object Acolyte extends Controller {

  def welcome = Assets.at(path = "/public", "index.html")

  sealed case class RouteData(json: String)

  def setup = Action { request ⇒
    Form[Option[RouteData]](mapping("json" -> optional(nonEmptyText))(
      _.map(RouteData))(_.map({ d ⇒ Some(d.json) }))).
      bindFromRequest()(request).fold[SimpleResult]({ f ⇒ Ok(s"${f.errors}") },
        { data ⇒ Ok(views.html.setup(data.map(_.json))) })
  }

  def run = Action { request ⇒
    Form(mapping("json" -> nonEmptyText)(
      RouteData.apply)(RouteData.unapply)).bindFromRequest()(request).
      fold[SimpleResult]({ f ⇒ Ok(s"${f.errors}") }, { data ⇒
        Ok(views.html.run(data.json))
      })
  }

  sealed case class ExecutionData(
    statement: String, json: String, parameters: Option[String])

  def executeStatement = Action { request ⇒
    Form(mapping("statement" -> nonEmptyText, "json" -> nonEmptyText,
      "parameters" -> optional(text))(ExecutionData.apply)(
        ExecutionData.unapply)).bindFromRequest()(request).fold[SimpleResult](
      { f ⇒ Ok(s"f.errors") }, { data ⇒
        (for {
          ps ← Reads.seq[RouteParameter](routeParamReads).reads(Json.parse(
            data.parameters getOrElse "[]"))
          rs ← Reads.seq[Route](routeReads).reads(Json parse data.json)
        } yield (ps -> rs)).fold[SimpleResult]({ e ⇒ PreconditionFailed(e.mkString) }, {
          case (ps, r :: rs) ⇒ executeWithRoutes(data.statement, ps, r :: rs)
          case _             ⇒ Ok(Json toJson false)
        })
      })
  }

  // ---

  @inline
  private def queryResult(res: QueryResult): Either[String, acolyte.QueryResult] = res match {
    case QueryError(msg) ⇒ Right(acolyte.QueryResult.Nil withWarning msg)
    case RowResult(rows) ⇒ Right(rows.asResult)
    case _               ⇒ Left(s"Unexpected query result: $res")
  }

  @inline
  private def updateResult(res: UpdateResult): Either[String, acolyte.UpdateResult] = res match {
    case UpdateError(msg) ⇒
      Right(acolyte.UpdateResult.Nothing withWarning msg)
    case UpdateCount(c) ⇒ Right(new acolyte.UpdateResult(c))
    case _              ⇒ Left(s"Unexpected update result: $res")
  }

  type QueryHandler = PartialFunction[QueryExecution, acolyte.QueryResult]
  type UpdateHandler = PartialFunction[UpdateExecution, acolyte.UpdateResult]

  case class HandlerData(queryPatterns: Seq[String] = Nil,
    queryHandler: Option[QueryHandler] = None,
    updateHandler: Option[UpdateHandler] = None)

  @inline
  private def updateHandler(ur: UpdateRoute, f: ⇒ Unit): Either[String, UpdateHandler] = ur match {
    case UpdateRoute(RoutePattern(e, Nil), res) ⇒
      updateResult(res).right map { r ⇒
        { case UpdateExecution(sql, _) if (e matches sql) ⇒ f; r }
      }

    case UpdateRoute(RoutePattern(e, ps), res) ⇒
      updateResult(res).right map { r ⇒
        { case UpdateExecution(sql, ps) if (e matches sql) ⇒ f; r }
      }

    case _ ⇒ Left(s"Unexpected update route: $ur")
  }

  @inline
  private def queryHandler(r: QueryRoute, f: ⇒ Unit): Either[String, QueryHandler] = r match {
    case QueryRoute(RoutePattern(e, Nil), res) ⇒
      queryResult(res).right map { r ⇒
        { case QueryExecution(sql, _) if (e matches sql) ⇒ f; r }
      }

    case QueryRoute(RoutePattern(e, ps), res) ⇒
      queryResult(res).right map { r ⇒
        { case QueryExecution(sql, ps) if (e matches sql) ⇒ f; r }
      }

    case _ ⇒ Left(s"Unexpected query route: $r")
  }

  @inline
  private def routeHandler(i: Int, r: Route, hd: HandlerData, f: Int ⇒ Unit): Either[String, HandlerData] = r match {
    case qr @ QueryRoute(RoutePattern(e, ps), res) ⇒
      (hd.copy(queryPatterns = hd.queryPatterns :+ e),
        queryHandler(qr, f(i))) match {
          case (a @ HandlerData(_, None, _), Right(b)) ⇒
            Right(a.copy(queryHandler = Some(b)))

          case (a @ HandlerData(_, Some(b), _), Right(c)) ⇒
            Right(a.copy(queryHandler = Some(b orElse c)))

          case (_, Left(err)) ⇒ Left(err)
        }

    case ur @ UpdateRoute(_, _) ⇒ (hd, updateHandler(ur, f(i))) match {
      case (HandlerData(_, _, None), Right(a)) ⇒
        Right(hd.copy(updateHandler = Some(a)))

      case (HandlerData(_, _, Some(a)), Right(b)) ⇒
        Right(hd.copy(updateHandler = Some(a orElse b)))

      case (_, Left(err)) ⇒ Left(err)
    }
    case _ ⇒ Left(s"Unexpected route: $r")
  }

  @annotation.tailrec
  private def handler(i: Int, routes: Seq[Route], f: Int ⇒ Unit, h: Either[String, HandlerData]): Either[String, HandlerData] = (routes, h) match {
    case (r :: rs, Right(hd)) ⇒
      handler(i + 1, rs, f, routeHandler(i, r, hd, f))
    case _ ⇒ h
  }

  @inline
  private val fallbackHandler: UpdateHandler = {
    case e ⇒ sys.error(s"No route handler: $e")
  }

  private def execResult(sql: String, ps: Seq[RouteParameter], routes: Seq[Route], f: Int ⇒ Unit): Either[String, ManagedResource[(PreparedStatement, Either[ResultSet, Int])]] =
    handler(0, routes, f, Right(HandlerData())).right map { hd ⇒
      val handleQuery = hd.queryHandler.fold(handleStatement) { qh ⇒
        handleStatement.withQueryDetection(hd.queryPatterns: _*).
          withQueryHandler(qh orElse { case e ⇒ sys.error(s"No route handler: $e") })
      }

      for {
        con ← managed(AcolyteConnection(hd.updateHandler.
          fold(handleQuery.withUpdateHandler(fallbackHandler)) { uh ⇒
            handleQuery.withUpdateHandler(uh orElse fallbackHandler)
          }))

        x ← managed {
          ps.foldLeft(1 -> con.prepareStatement(sql)) { (st, p) ⇒
            (st, p) match {
              case ((i, s), StringParameter(v)) ⇒
                s.setString(i, v); (i + 1 -> s)
              case ((i, s), FloatParameter(v)) ⇒
                s.setFloat(i, v); (i + 1 -> s)
              case ((i, s), DateParameter(v)) ⇒
                s.setDate(i, new java.sql.Date(v.getTime)); (i + 1 -> s)
              case _ ⇒ st
            }
          } _2
        } map { st ⇒
          if (st.execute()) st -> Left(st.getResultSet)
          else st -> Right(st.getUpdateCount)
        }
      } yield x
    }

  @annotation.tailrec
  private def jsonResultSet(rs: ResultSet, c: Int, js: Seq[Traversable[JsValue]]): Seq[Traversable[JsValue]] = rs.next match {
    case true ⇒ jsonResultSet(rs, c, js :+ (for {
      i ← 1 to c
    } yield (rs.getObject(i) match {
      case d: Date ⇒ Json.toJson(DateFormat format d)
      case v       ⇒ Json.toJson(v.toString)
    })))

    case _ ⇒ js
  }

  private implicit object SQLWarningWrites extends Writes[SQLWarning] {
    def writes(w: SQLWarning): JsValue = Json obj (
      "reason" -> w.getMessage,
      "errorCode" -> w.getErrorCode,
      "cause" -> Option(w.getCause).map(_.getMessage)
    )
  }

  @inline
  private def executeWithRoutes(stmt: String, ps: Seq[RouteParameter], routes: Seq[Route]): SimpleResult = {
    var r: Int = -1

    (for {
      exe ← execResult(stmt, ps, routes, { r = _ }).right
      state ← exe.acquireFor({ x ⇒
        val (st, res) = x
        res.fold[JsValue]({ rs ⇒
          if (rs.getWarnings != null) {
            Json toJson Map("route" -> Json.toJson(r),
              "warning" -> Json.toJson(rs.getWarnings))

          } else {
            val meta = rs.getMetaData
            val c = meta.getColumnCount

            val ls = for { i ← 1 to c } yield {
              Option(meta.getColumnLabel(i)).
                orElse(Option(meta.getColumnName(i))) getOrElse s"Column #$i"
            }

            Json toJson Map("route" -> Json.toJson(r),
              "columns" -> Json.toJson(ls),
              "resultSet" -> Json.toJson(jsonResultSet(rs, c, Nil)))

          }
        }, { uc ⇒
          if (st.getWarnings != null) {
            Json toJson Map("route" -> Json.toJson(r),
              "warning" -> Json.toJson(st.getWarnings))
          } else Json toJson Map("route" -> r, "updateCount" -> uc)
        })
      }).left.map(_.mkString).right
    } yield state).fold({ err ⇒
      InternalServerError(Json toJson Map("exception" -> err))
    }, Ok(_))
  }
}
