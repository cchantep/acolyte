package acolyte.reactivemongo

import scala.util.{ Failure, Success, Try }

import reactivemongo.io.netty.channel.{ ChannelId, DefaultChannelId }

import reactivemongo.api.WriteConcern
import reactivemongo.api.bson.{ BSONArray, BSONDocument, BSONString, BSONValue }

import reactivemongo.core.protocol.ProtocolMetadata
import reactivemongo.acolyte.{
  Authenticate,
  Connection,
  Close,
  Closed,
  ExpectingResponse,
  Query => RQuery,
  MongoDBSystem,
  RequestMaker,
  Response,
  PrimaryAvailable,
  RegisterMonitor,
  SetAvailable
}

private[reactivemongo] class Actor(handler: ConnectionHandler)
  extends MongoDBSystem {

  lazy val initialAuthenticates = Seq.empty[Authenticate]

  protected def authReceive: PartialFunction[Any, Unit] = { case _ ⇒ () }

  val supervisor = "Acolyte"
  val name = "AcolyteConnection"
  lazy val seeds = Seq.empty[String]

  lazy val options = reactivemongo.api.MongoConnectionOptions()

  protected def sendAuthenticate(connection: Connection, authentication: Authenticate): Connection = connection

  protected def newChannelFactory(effect: Unit) =
    reactivemongo.acolyte.channelFactory(supervisor, name, options)

  private def handleWrite(chanId: ChannelId, op: WriteOp, req: Request): Option[Response] = Try(handler.writeHandler(chanId, op, req)) match {
    case Failure(cause) ⇒ Some(InvalidWriteHandler(chanId, cause.getMessage))

    case Success(res) ⇒ res.map {
      case Success(r) ⇒ r
      case Failure(e) ⇒ MongoDB.WriteError(chanId, Option(e.getMessage).
        getOrElse(e.getClass.getName)) match {
        case Success(err) ⇒ err
        case _            ⇒ MongoDB.MkWriteError(chanId)
      }
    }
  }

  override lazy val receive: Receive = {
    case msg @ ExpectingResponse(RequestMaker(
      RQuery(_ /*flags*/ , coln, _ /*off*/ , _ /* len */ ),
      doc, _ /*pref*/ , chanId), promise) ⇒ {

      val cid = chanId getOrElse newChannelId()
      val req = Request(coln, doc.merged)

      val resp = req match {
        case Request(_, SimpleBody((k @ WriteQuery(op),
          BSONString(cn)) :: es)) if (coln endsWith ".$cmd") ⇒ {

          val opBody: Option[List[BSONDocument]] = if (k == "insert") {
            es.collectFirst {
              case ("documents", a: BSONArray) ⇒ a.values.toList.collect {
                case doc: BSONDocument ⇒ doc
              }
            }
          } else {
            val Key = k + "s"

            es.collectFirst {
              case (Key, a: BSONArray) ⇒ a.values.toList.collect {
                case doc: BSONDocument ⇒ doc
              }
            }
          }

          val wreq = new Request {
            val collection = coln.dropRight(4) + cn
            val body = opBody.getOrElse(List.empty)
          }

          handleWrite(cid, op, wreq).getOrElse(
            NoWriteResponse(cid, msg.toString))
        }

        case Request(coln, SimpleBody(ps)) ⇒ {
          val qreq = new Request {
            val collection = coln

            val body = ps.foldLeft(Option.empty[BSONDocument] → (
              List.empty[(String, BSONValue)])) {
              case ((_, opts), ("$query", q: BSONDocument)) ⇒
                Some(q) → opts

              case ((q, opts), opt) ⇒ q → (opts :+ opt)
            } match {
              case (q, opts) ⇒ q.toList :+ BSONDocument(opts)
            }
          }

          Try(handler.queryHandler(cid, qreq)) match {
            case Failure(cause) ⇒ InvalidQueryHandler(cid, cause.getMessage)

            case Success(res) ⇒ res.fold(NoQueryResponse(cid, msg.toString)) {
              case Success(r) ⇒ r
              case Failure(e) ⇒ MongoDB.QueryError(cid, Option(e.getMessage).
                getOrElse(e.getClass.getName)) match {
                case Success(err) ⇒ err
                case _            ⇒ MongoDB.MkQueryError(cid)
              }
            }
          }
        }
      }

      resp.error.fold(promise.success(resp))(promise.failure(_))
    }

    case _: RegisterMonitor ⇒ {
      // TODO: configure protocol metadata
      sender ! PrimaryAvailable(ProtocolMetadata.Default)
      sender ! SetAvailable(ProtocolMetadata.Default)
    }

    case Close() ⇒ {
      sender ! Closed
      postStop()
    }

    case msg ⇒
      //println(s"message = $msg")

      //next forward msg
      ()
  }

  // ---

  private def newChannelId(): ChannelId = DefaultChannelId.newInstance()

  // Write operations sent as `Query`
  private object WriteQuery {
    def unapply(repr: String): Option[WriteOp] = repr match {
      case "insert" ⇒ Some(InsertOp)
      case "update" ⇒ Some(UpdateOp)
      case "delete" ⇒ Some(DeleteOp)
      case _        ⇒ None
    }
  }

  // Fallback response when no handler provides a query response.
  @inline private def NoQueryResponse(chanId: ChannelId, req: String): Response = MongoDB.QueryError(chanId, s"No response: $req") match {
    case Success(resp) ⇒ resp
    case _             ⇒ MongoDB.MkQueryError(chanId)
  }

  // Fallback response when write handler is failing.
  @inline private def InvalidWriteHandler(chanId: ChannelId, msg: String): Response = MongoDB.WriteError(chanId, s"Invalid write handler: $msg") match {
    case Success(resp) ⇒ resp
    case _             ⇒ MongoDB.MkWriteError(chanId)
  }

  // Fallback response when no handler provides a write response.
  @inline private def NoWriteResponse(chanId: ChannelId, req: String): Response = MongoDB.WriteError(chanId, s"No response: $req") match {
    case Success(resp) ⇒ resp
    case _             ⇒ MongoDB.MkWriteError(chanId)
  }

  // Fallback response when query handler is failing.
  @inline private def InvalidQueryHandler(chanId: ChannelId, msg: String): Response = MongoDB.QueryError(chanId, s"Invalid query handler: $msg") match {
    case Success(resp) ⇒ resp
    case _             ⇒ MongoDB.MkQueryError(chanId)
  }
}
