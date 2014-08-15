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
   * @param name Actor system name (default: "ReactiveMongoAcolyte")
   */
  def actorSystem(name: String = "ReactiveMongoAcolyte"): AkkaSystem = new ActorSystem(AkkaSystem(name), new ActorRefFactory() {
    def before(system: AkkaSystem, next: ActorRef): ActorRef = {
      system actorOf Props(classOf[Actor], next)
    }
  })
}

final class Actor(next: ActorRef) extends akka.actor.Actor {
  def receive = {
    case msg @ CheckedWriteRequestExpectingResponse(req) ⇒
      println(s"wreq = $req")
      next forward msg

    case msg @ RequestMakerExpectingResponse(RequestMaker(op @ RQuery(_ /*flags*/ , coln, off, len), doc, _ /*pref*/ , chanId)) ⇒
      val exp = new ExpectingResponse(msg)

      import reactivemongo.bson.{ BSONDocument, BSONInteger, BSONString }

      val q = Query(coln, doc.merged)

      q match {
        case QueryBody(colName, 
          ~(Property("age"), ValueDocument(
            ~(Property("$gt"), BSONInteger(minAge)))) &
          ~(Property("email"), BSONString("demo@applicius.fr"))) =>
          println(s"col = $colName, ${minAge}")
      }

      println(s"query = ${q.body.elements.toList}")

      val bsonObj1 = BSONDocument("email" -> "test1@test.fr", "age" -> 3)
      val bsonObj2 = BSONDocument("email" -> "test2@test.fr", "age" -> 5)

      exp.promise.success(MongoDB.mkResponse(chanId getOrElse 1, bsonObj1, bsonObj2).get)

    case close @ Close ⇒ /* Do nothing: next forward close */
    case msg ⇒
      /*
      println(s"message = $msg")
      next forward msg
       */
      ()
  }
}
