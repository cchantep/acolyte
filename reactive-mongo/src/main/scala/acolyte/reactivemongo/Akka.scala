package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import com.typesafe.config.Config
import akka.actor.{ ActorRef, ActorSystem ⇒ AkkaSystem, Props }

import reactivemongo.core.commands.GetLastError
import reactivemongo.core.actors.{
  Close,
  CheckedWriteRequestExpectingResponse,
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
    def before(system: AkkaSystem, next: ActorRef): ActorRef = {
      system actorOf Props(classOf[Actor], handler, next)
    }
  })
}

private[reactivemongo] class Actor(
    handler: ConnectionHandler, next: ActorRef /* TODO: Remove */ ) extends akka.actor.Actor {
  def receive = {
    case msg @ CheckedWriteRequestExpectingResponse(
      CheckedWriteRequest(op, doc, GetLastError(_, _, _, _))) ⇒

      val req = Request(op.fullCollectionName, doc.merged)
      // op = Insert(0,test-db.a-col)

      println(s"oper = ${MongoDB.WriteOp(op)}, ${req.body.elements.toList}")
      next forward msg

    case msg @ RequestMakerExpectingResponse(RequestMaker(
      op @ RQuery(_ /*flags*/ , coln, off, len), doc, _ /*pref*/ , chanId)) ⇒
      val exp = new ExpectingResponse(msg)
      val cid = chanId getOrElse 1
      val resp = handler.queryHandler(cid, Request(coln, doc.merged)).
        fold(NoResponse(cid, msg.toString))(_ match {
          case Success(r) ⇒ r
          case Failure(e) ⇒ MongoDB.Error(cid, Option(e.getMessage).
            getOrElse(e.getClass.getName)) match {
            case Success(err) ⇒ err
            case _            ⇒ MongoDB.MkResponseError(cid)
          }
        })

      exp.promise.success(resp)

    case close @ Close ⇒ /* Do nothing: next forward close */
    case msg ⇒
      /*
      println(s"message = $msg")
      next forward msg
       */
      ()
  }

  /** Fallback response when no handler provides a response. */
  @inline private def NoResponse(chanId: Int, req: String): Response =
    MongoDB.Error(chanId, s"No response: $req") match {
      case Success(resp) ⇒ resp
      case _             ⇒ MongoDB.MkResponseError(chanId)
    }
}
