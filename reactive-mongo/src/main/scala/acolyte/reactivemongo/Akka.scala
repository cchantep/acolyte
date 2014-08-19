package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import com.typesafe.config.Config
import akka.actor.{ ActorRef, ActorSystem ⇒ AkkaSystem, Props }

import reactivemongo.core.actors.{
  Close,
  CheckedWriteRequestExpectingResponse,
  RequestMakerExpectingResponse
}
import reactivemongo.core.protocol.{ Query ⇒ RQuery, RequestMaker }

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
    case msg @ CheckedWriteRequestExpectingResponse(req) ⇒
      println(s"wreq = $req")
      /*
       CheckedWriteRequest(Insert(0,test-db.a-col),BufferSequence(DynamicChannelBuffer(ridx=0, widx=37, cap=64),WrappedArray()),GetLastError(false,None,0,false))
       */
      next forward msg

    case msg @ RequestMakerExpectingResponse(RequestMaker(
      op @ RQuery(_ /*flags*/ , coln, off, len), doc, _ /*pref*/ , chanId)) ⇒
      val exp = new ExpectingResponse(msg)

      handler queryHandler Query(coln, doc.merged) match {
        case Some(body) ⇒
          println(s"query = ${body}")
          exp.promise.success(MongoDB.Success(chanId getOrElse 1, body: _*).get)

        case _ ⇒ ???
      }

    case close @ Close ⇒ /* Do nothing: next forward close */
    case msg ⇒
      /*
      println(s"message = $msg")
      next forward msg
       */
      ()
  }
}
