package controllers

import java.util.Date

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

import scala.concurrent.Future

import resource.{ ManagedResource, managed }

import reactivemongo.bson.{
  BSONDateTime,
  BSONDocument,
  BSONDouble,
  BSONString,
  BSONValue
}

import play.api.mvc.{ Action, Controller, Result ⇒ PlayResult }
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText, optional, text }

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{ Json, JsResult, JsValue, Reads, Writes }

import play.modules.reactivemongo.json.BSONFormats

import acolyte.reactivemongo.{
  AcolyteDSL,
  ConnectionHandler,
  QueryHandler,
  QueryResponse,
  PreparedResponse,
  Request,
  WriteOp,
  WriteHandler,
  WriteResponse
}

object Acolyte extends Controller {

  def welcome = Assets.at(path = "/public", "index.html")

  sealed case class RouteData(json: String)

  def setup = Action { request ⇒
    Form[Option[RouteData]](mapping("json" -> optional(nonEmptyText))(
      _.map(RouteData))(_.map({ d ⇒ Some(d.json) }))).
      bindFromRequest()(request).fold[PlayResult](f ⇒ Ok(f.errors.toString),
        { data ⇒ Ok(views.html.setup(data.map(_.json))) })
  }

  def run = Action { request ⇒
    Form(mapping("json" -> nonEmptyText)(
      RouteData.apply)(RouteData.unapply)).bindFromRequest()(request).
      fold[PlayResult]({ f ⇒ Ok(f.errors.toString) }, { data ⇒
        Ok(views.html.run(data.json))
      })
  }

  sealed case class ExecutionData(
    statement: String, json: String, parameters: Option[String])

  def executeStatement = Action { request ⇒
    Form(mapping("statement" -> nonEmptyText, "json" -> nonEmptyText,
      "parameters" -> optional(text))(ExecutionData.apply)(
        ExecutionData.unapply)).bindFromRequest()(request).fold[PlayResult](
      { f ⇒ Ok(f.errors.toString) }, { data ⇒
        (for {
          ps ← Reads.seq[RouteParameter](routeParamReads).reads(Json.parse(
            data.parameters getOrElse "[]"))
          rs ← Reads.seq[Route](routeReads).reads(Json parse data.json)
        } yield (ps -> rs)).fold[PlayResult]({ e ⇒ PreconditionFailed(e.mkString) }, {
          case (ps, r :: rs) ⇒ executeWithRoutes(data.statement, ps, r :: rs)
          case _             ⇒ Ok(Json toJson false)
        })
      })
  }

  // ---

  @inline
  private def bsonVal(v: Any): BSONValue = v match {
    case str: String ⇒ BSONString(str)
    case f: Float    ⇒ BSONDouble(f)
    case d: Date     ⇒ BSONDateTime(d.getTime)
    case _           ⇒ sys.error(s"Unsupported BSON value: $v")
  }

  @inline
  private def bsonDoc[R <: Row](row: R, labels: List[String]): BSONDocument =
    (row, labels) match {
      case (Row1(a), al :: _) ⇒ BSONDocument(al -> bsonVal(a))
      case (Row2(a, b), al :: bl :: _) ⇒ BSONDocument(
        al -> bsonVal(a), bl -> bsonVal(b))
      case (Row3(a, b, c), al :: bl :: cl :: _) ⇒
        BSONDocument(al -> bsonVal(a), bl -> bsonVal(b), cl -> bsonVal(c))
      case _ ⇒ sys.error(s"Unsupported row type: $row")
    }

  @annotation.tailrec
  private def rowResult[R <: Row](rows: List[R], labels: List[String], bson: List[BSONDocument]): PreparedResponse = rows.headOption match {
    case Some(r) ⇒ rowResult(rows.tail, labels.tail,
      bson :+ bsonDoc(r, labels))

    case _ ⇒ QueryResponse(bson)
  }

  @inline
  private def queryResult(res: QueryResult): Either[String, PreparedResponse] =
    res match {
      case QueryError(msg) ⇒ Right(QueryResponse failed msg)
      case RowResult(rows) ⇒
        Right(QueryResponse successful BSONDocument())
      case _ ⇒ Left(s"Unexpected query result: $res")
    }

  @inline
  private def updateResult(res: UpdateResult): Either[String, PreparedResponse] = res match {
    case UpdateError(msg) ⇒
      Right(WriteResponse failed msg)
    case UpdateCount(c) ⇒ Right(WriteResponse successful c)
    // TODO: successful(count, updated)
    case _              ⇒ Left(s"Unexpected update result: $res")
  }

  @inline
  private def writeHandler(ur: UpdateRoute, f: ⇒ Unit): Either[String, WriteHandler] = ur match {
    case UpdateRoute(RoutePattern(c, Nil), res) ⇒
      val Col = c
      updateResult(res).right map { r ⇒
        WriteHandler({
          case Request(Col, _) ⇒
            f; r
          case _ ⇒ WriteResponse.undefined
        })
      }

    case UpdateRoute(RoutePattern(c, ps), res) ⇒
      val Col = c
      val Params = ??? // params extractor
      updateResult(res).right map { r ⇒
        WriteHandler({
          case Request(Col, Params) /* TODO: Params */ ⇒
            f; r
          case _ ⇒ WriteResponse.undefined
        })
      }

    case _ ⇒ Left(s"Unexpected update route: $ur")
  }

  @inline
  private def queryHandler(r: QueryRoute, f: ⇒ Unit): Either[String, QueryHandler] = r match {
    case QueryRoute(RoutePattern(c, Nil), res) ⇒
      val Col = c
      queryResult(res).right map { r ⇒
        QueryHandler({
          case Request(Col, _) ⇒
            f; r
          case _ ⇒ QueryResponse.undefined
        })
      }

    case QueryRoute(RoutePattern(c, ps), res) ⇒
      val Col = c
      val Params = ??? // params extractor
      queryResult(res).right map { r ⇒
        QueryHandler({
          case Request(Col, Params) /* TODO: Params */ ⇒
            f; r
          case _ ⇒ QueryResponse.undefined
        })
      }

    case _ ⇒ Left(s"Unexpected query route: $r")
  }

  @inline
  private def routeHandler(i: Int, r: Route, ch: ConnectionHandler, f: Int ⇒ Unit): Either[String, ConnectionHandler] = r match {
    case qr @ QueryRoute(RoutePattern(_, ps), res) ⇒
      queryHandler(qr, f(i)).right.map(ch.withQueryHandler(_))

    case ur @ UpdateRoute(_, _) ⇒
      writeHandler(ur, f(i)).right.map(ch.withWriteHandler(_))

    case _ ⇒ Left(s"Unexpected route: $r")
  }

  @annotation.tailrec
  private def handler(i: Int, routes: Seq[Route], f: Int ⇒ Unit, h: Either[String, ConnectionHandler]): Either[String, ConnectionHandler] = (routes, h) match {
    case (r :: rs, Right(hd)) ⇒
      handler(i + 1, rs, f, routeHandler(i, r, hd, f))
    case _ ⇒ h
  }

  @inline
  private def fallbackHandler = WriteHandler({
    case e ⇒ sys.error(s"No route handler: $e")
  })

  private def execResult(bson: String, ps: Seq[RouteParameter], routes: Seq[Route], f: Int ⇒ Unit): Future[Either[List[BSONDocument], Int]] =
    handler(0, routes, f, Right(ConnectionHandler())).fold(
      { err ⇒ Future.failed(sys.error(err)) }, { ch ⇒
        // hd.queryHandler, hd.writeHandler
        /*
      val handleQuery = hd.queryHandler.fold(handleStatement) { qh ⇒
        handleStatement.withQueryDetection(hd.queryPatterns: _*).
          withQueryHandler(qh orElse {
            case e ⇒ sys.error(s"No route handler: $e")
          })
      }
         */
        implicit val driver = AcolyteDSL.driver

        AcolyteDSL.withFlatConnection(ch) { con ⇒
          Future.successful(Right(0)) // TODO
        }

        /*
        x ← managed {
          ps.foldLeft(1 -> con.prepareStatement(bson)) { (st, p) ⇒
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
       */
      })

  @annotation.tailrec
  private def jsonResult(rs: List[BSONDocument], js: List[JsValue]): List[JsValue] = rs match {
    case bson :: docs ⇒ jsonResult(docs, BSONFormats.toJSON(bson) :: js)
    case _            ⇒ js.reverse
  }

  @inline
  private def executeWithRoutes(stmt: String, ps: Seq[RouteParameter], routes: Seq[Route]): PlayResult = {
    var r: Int = -1

    execResult(stmt, ps, routes, { r = _ })

    /*
    (for {
      exe ← 
      state ← exe.acquireFor({ x ⇒
        val (st, res) = x
        res.fold[JsValue]({ rs ⇒
          if (rs.getWarnings != null) {
            Json toJson Map("route" -> Json.toJson(r),
              "warning" -> Json.toJson(rs.getWarnings))

          } else {
            val meta = rs.getMetaData
            val c = meta.getColumnCount
            val ts: Seq[String] = routes(r) match {
              case QueryRoute(_, RowResult(rows)) ⇒
                rows.columnClasses map { cl ⇒
                  val n = cl.getName

                  if (n == "java.util.Date") "date"
                  else if (n == "java.lang.String") "string"
                  else n
                } toSeq
              case _ ⇒ Nil
            }

            val ls: Traversable[Map[String, String]] =
              for { i ← 1 to c } yield {
                Map("_type" -> ts.lift(i).getOrElse("string"),
                  "name" -> Option(meta.getColumnLabel(i)).orElse(
                    Option(meta.getColumnName(i))).getOrElse(s"Column #$i"))
              }

            Json toJson Map("route" -> Json.toJson(r),
              "schema" -> Json.toJson(ls),
              "rows" -> Json.toJson(jsonResult(rs, Nil)))

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
     */
    ??? // TODO
  }
}
