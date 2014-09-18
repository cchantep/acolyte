package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }, ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import com.typesafe.config.Config
import akka.actor.{ ActorRef, ActorSystem ⇒ AkkaSystem, Props }

import reactivemongo.core.commands.GetLastError
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
private[reactivemongo] object Akka {
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
  def actorSystem(handler: ConnectionHandler, name: String = "ReactiveMongoAcolyte"): AkkaSystem = new ActorSystem(AkkaSystem(name), new ActorRefFactory() {
    /* For reverse engineering
    def before(system: AkkaSystem, next: ActorRef): ActorRef = {
      system actorOf Props(classOf[Actor], handler, next)
    }
     */

    val DbSystem = classOf[reactivemongo.core.actors.MongoDBSystem]

    def actorOf(system: AkkaSystem, props: Props): ActorRef = {
      if (props.actorClass == DbSystem) {
        system actorOf Props(classOf[Actor], handler)
      } else system.actorOf(props)
    }

    def actorOf(system: AkkaSystem, props: Props, n: String): ActorRef = {
      if (props.actorClass == DbSystem) {
        system.actorOf(Props(classOf[Actor], handler), n)
      } else system.actorOf(props, n)
    }
  })
}

private[reactivemongo] class Actor(
  handler: ConnectionHandler) extends akka.actor.Actor {

  def receive = {
    case msg @ CheckedWriteRequestExResp(
      r @ CheckedWriteRequest(op, doc, GetLastError(_, _, _, _))) ⇒

      val req = Request(op.fullCollectionName, doc.merged)
      val exp = new ExpectingResponse(msg)
      val cid = r()._1.channelIdHint getOrElse 1
      val resp = MongoDB.WriteOp(op).fold({
        MongoDB.WriteError(cid, s"No write operator: $msg") match {
          case Success(err) ⇒ err
          case _            ⇒ MongoDB.MkWriteError(cid)
        }
      }) { wop ⇒
        handler.writeHandler(cid, wop, req).
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

    case msg @ RequestMakerExpectingResponse(RequestMaker(
      op @ RQuery(_ /*flags*/ , coln, off, len), doc, _ /*pref*/ , chanId)) ⇒
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
