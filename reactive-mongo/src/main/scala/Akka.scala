package acolyte.reactivemongo

import scala.util.{ Failure, Success, Try }

import reactivemongo.io.netty.channel.{ ChannelId, DefaultChannelId }

import reactivemongo.api.bson.{ BSONArray, BSONDocument, BSONString, BSONValue }

import reactivemongo.acolyte.{
  Authenticate,
  Close,
  Closed,
  Connection,
  ExpectingResponse,
  MongoDBSystem,
  PrimaryAvailable,
  Query => RQuery,
  RegisterMonitor,
  RequestMaker,
  Response,
  SetAvailable
}
import reactivemongo.core.protocol.ProtocolMetadata

private[reactivemongo] class Actor(handler: ConnectionHandler)
    extends MongoDBSystem {

  lazy val initialAuthenticates = Seq.empty[Authenticate]

  protected def authReceive: PartialFunction[Any, Unit] = { case _ => () }

  val supervisor = "Acolyte"
  val name = "AcolyteConnection"
  lazy val seeds = Seq.empty[String]

  lazy val options = reactivemongo.api.MongoConnectionOptions()

  protected def sendAuthenticate(
      connection: Connection,
      authentication: Authenticate
    ): Connection = connection

  protected def newChannelFactory(effect: Unit) =
    reactivemongo.acolyte.channelFactory(supervisor, name, options)

  private def handleWrite(
      chanId: ChannelId,
      op: WriteOp,
      req: Request
    ): Option[Response] = Try(handler.writeHandler(chanId, op, req)) match {
    case Failure(cause) => Some(invalidWriteHandler(chanId, cause.getMessage))

    case Success(res) =>
      res.map {
        case Success(r) => r
        case Failure(e) =>
          MongoDB.writeError(
            chanId,
            Option(e.getMessage).getOrElse(e.getClass.getName)
          ) match {
            case Success(err) => err
            case _            => MongoDB.mkWriteError(chanId)
          }
      }
  }

  override lazy val receive: Receive = {
    case msg @ ExpectingResponse(
          RequestMaker(
            RQuery(_ /*flags*/, coln, _ /*off*/, _ /* len */ ),
            doc,
            _ /*pref*/,
            chanId
          ),
          promise
        ) => {

      val cid = chanId getOrElse newChannelId()
      val req = Request(coln, doc)

      val resp = req match {
        case Request(_, SimpleBody((k @ WriteQuery(op), BSONString(cn)) :: es))
            if (coln endsWith f".$$cmd") => {

          val opBody: Option[List[BSONDocument]] = if (k == "insert") {
            es.collectFirst {
              case ("documents", a: BSONArray) =>
                a.values.toList.collect { case doc: BSONDocument => doc }
            }
          } else {
            val Key = k + "s"

            es.collectFirst {
              case (Key, a: BSONArray) =>
                a.values.toList.collect { case doc: BSONDocument => doc }
            }
          }

          val wreq = new Request {
            val collection = coln.dropRight(4) + cn
            val body = opBody.getOrElse(List.empty)
          }

          handleWrite(cid, op, wreq).getOrElse(
            noWriteResponse(cid, msg.toString)
          )
        }

        case Request(coln, SimpleBody(ps)) => {
          val reqBody: List[BSONDocument] =
            ps.foldLeft(
              Option.empty[BSONDocument] -> (List.empty[(String, BSONValue)])
            ) {
              case ((_, opts), ("$query", q: BSONDocument)) =>
                Some(q) -> opts

              case ((q, opts), opt) => q -> (opt +: opts)
            } match {
              case (Some(q), opts) =>
                List(q, BSONDocument(opts.reverse))

              case (_, opts) =>
                List(BSONDocument(opts.reverse))
            }

          val qreq = new Request {
            val collection = coln
            val body = reqBody
          }

          Try(handler.queryHandler(cid, qreq)) match {
            case Failure(cause) => invalidQueryHandler(cid, cause.getMessage)

            case Success(res) =>
              res.fold(noQueryResponse(cid, msg.toString)) {
                case Success(r) => r

                case Failure(e) =>
                  MongoDB.queryError(
                    cid,
                    Option(e.getMessage).getOrElse(e.getClass.getName)
                  ) match {
                    case Success(err) => err
                    case _            => MongoDB.mkQueryError(cid)
                  }
              }
          }
        }

        case req =>
          sys.error(s"Unexpected request: $req")
      }

      resp.error.fold(promise.success(resp))(promise.failure(_))

      ()
    }

    case _: RegisterMonitor => {
      // TODO: configure protocol metadata
      val s = sender()
      s ! PrimaryAvailable(ProtocolMetadata.Default)
      s ! SetAvailable(ProtocolMetadata.Default)
    }

    case Close() => {
      sender() ! Closed

      try {
        postStop()
      } catch {
        case _: java.util.concurrent.TimeoutException =>
          logger.warn("Fails to close in a timely manner the MongoDBSystem")
      }
    }

    case _ /*msg*/ =>
      // println(s"message = $msg")

      // next forward msg
      ()
  }

  // ---

  private def newChannelId(): ChannelId = DefaultChannelId.newInstance()

  // Write operations sent as `Query`
  private object WriteQuery {

    def unapply(repr: String): Option[WriteOp] = repr match {
      case "insert" => Some(InsertOp)
      case "update" => Some(UpdateOp)
      case "delete" => Some(DeleteOp)
      case _        => None
    }
  }

  // Fallback response when no handler provides a query response.
  @inline private def noQueryResponse(
      chanId: ChannelId,
      req: String
    ): Response = MongoDB.queryError(chanId, s"No response: $req") match {
    case Success(resp) => resp
    case _             => MongoDB.mkQueryError(chanId)
  }

  // Fallback response when write handler is failing.
  @inline private def invalidWriteHandler(
      chanId: ChannelId,
      msg: String
    ): Response =
    MongoDB.writeError(chanId, s"Invalid write handler: $msg") match {
      case Success(resp) => resp
      case _             => MongoDB.mkWriteError(chanId)
    }

  // Fallback response when no handler provides a write response.
  @inline private def noWriteResponse(
      chanId: ChannelId,
      req: String
    ): Response = MongoDB.writeError(chanId, s"No response: $req") match {
    case Success(resp) => resp
    case _             => MongoDB.mkWriteError(chanId)
  }

  // Fallback response when query handler is failing.
  @inline private def invalidQueryHandler(
      chanId: ChannelId,
      msg: String
    ): Response =
    MongoDB.queryError(chanId, s"Invalid query handler: $msg") match {
      case Success(resp) => resp
      case _             => MongoDB.mkQueryError(chanId)
    }
}
