package acolyte.reactivemongo

import scala.concurrent.{
  ExecutionContext,
  Future
}, ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import com.typesafe.config.ConfigFactory
import akka.actor.{ ActorRef, ActorSystem ⇒ AkkaSystem, Props }

import reactivemongo.api.commands.GetLastError
import reactivemongo.core.actors.{
  Close,
  CheckedWriteRequestExpectingResponse ⇒ CheckedWriteRequestExResp,
  RequestMakerExpectingResponse
}
import reactivemongo.core.protocol.{
  CheckedWriteRequest,
  Query ⇒ RQuery,
  RequestMaker,
  Response
}

/** Akka companion for Acolyte mongo system. */
object Akka {
  /**
   * Creates an Acolyte actor system for ReactiveMongo use.
   *
   * {{{
   * import acolyte.reactivemongo.MongoSystem
   * import akka.actor.ActorSystem
   *
   * val mongo: ActorSystem = Akka.actorSystem()
   * }}}
   *
   * @param handler Connection handler
   * @param name Actor system name (default: "ReactiveMongoAcolyte")
   */
  def actorSystem(name: String = "ReactiveMongoAcolyte"): AkkaSystem = {
    val cl = this.getClass.getClassLoader
    val cfg = ConfigFactory.load(cl)
    AkkaSystem(name, cfg, cl)
  }
}

private[reactivemongo] class Actor(
  handler: ConnectionHandler) extends reactivemongo.core.actors.MongoDBSystem {

  import reactivemongo.core.nodeset.{ Authenticate, ChannelFactory, Connection }

  override lazy val initialAuthenticates = Seq.empty[Authenticate]

  override protected def authReceive: PartialFunction[Any, Unit] = { case _ => () }

  override lazy val seeds = Seq.empty[String]

  override lazy val options = reactivemongo.api.MongoConnectionOptions()

  override protected def sendAuthenticate(connection: Connection,authentication: Authenticate): Connection = ???

  override lazy val channelFactory: ChannelFactory = new ChannelFactory(options)

  override lazy val receive: Receive = {
    case msg @ CheckedWriteRequestExResp(
      r @ CheckedWriteRequest(op, doc, GetLastError(_, _, _, _))) ⇒ {

      val req = Request(op.fullCollectionName, doc.merged)
      val exp = new ExpectingResponse(msg)
      val cid = r()._1.channelIdHint getOrElse 1
      val resp = MongoDB.WriteOp(op).fold({
        MongoDB.WriteError(cid, s"No write operator: $msg") match {
          case Success(err) ⇒ err
          case _            ⇒ MongoDB.MkWriteError(cid)
        }
      }) { 
        handler.writeHandler(cid, _, req).
          fold(NoWriteResponse(cid, msg.toString))(_ match {
            case Success(r) ⇒ r
            case Failure(e) ⇒ MongoDB.WriteError(cid, Option(e.getMessage).
              getOrElse(e.getClass.getName)) match {
              case Success(err) ⇒ err
              case _            ⇒ MongoDB.MkWriteError(cid)
            }
          })
      }

      exp.promise.success(resp)
    }

    case msg @ RequestMakerExpectingResponse(RequestMaker(
      op @ RQuery(_ /*flags*/ , coln, off, len),
      doc, _ /*pref*/ , chanId), _) ⇒ {
      val exp = new ExpectingResponse(msg)
      val cid = chanId getOrElse 1
      val resp = handler.queryHandler(cid, Request(coln, doc.merged)).
        fold(NoQueryResponse(cid, msg.toString))(_ match {
          case Success(r) ⇒ r
          case Failure(e) ⇒ MongoDB.QueryError(cid, Option(e.getMessage).
            getOrElse(e.getClass.getName)) match {
            case Success(err) ⇒ err
            case _            ⇒ MongoDB.MkQueryError(cid)
          }
        })

      resp.error.fold(exp.promise.success(resp))(exp.promise.failure(_))
    }

    case close @ Close ⇒ /* Do nothing: next forward close */
    case msg ⇒
      //println(s"message = $msg")
      //next forward msg
      ()
  }

  /** Fallback response when no handler provides a query response. */
  @inline private def NoQueryResponse(chanId: Int, req: String): Response =
    MongoDB.QueryError(chanId, s"No response: $req") match {
      case Success(resp) ⇒ resp
      case _             ⇒ MongoDB.MkQueryError(chanId)
    }

  /** Fallback response when no handler provides a write response. */
  @inline private def NoWriteResponse(chanId: Int, req: String): Response =
    MongoDB.WriteError(chanId, s"No response: $req") match {
      case Success(resp) ⇒ resp
      case _             ⇒ MongoDB.MkQueryError(chanId)
    }
}
