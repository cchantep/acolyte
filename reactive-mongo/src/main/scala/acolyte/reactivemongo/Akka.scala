package acolyte.reactivemongo

import scala.concurrent.ExecutionContext
import com.typesafe.config.Config
import akka.actor.{ ActorRef, ActorSystem ⇒ AkkaSystem, Props }

import org.jboss.netty.buffer.ChannelBuffer
import reactivemongo.core.actors.{
  Close,
  CheckedWriteRequestExpectingResponse,
  RequestMakerExpectingResponse
}
import reactivemongo.core.protocol.{ Query, RequestMaker }
import reactivemongo.bson.buffer.{
  ArrayReadableBuffer,
  DefaultBufferHandler,
  ReadableBuffer
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
   * @param name Actor system name (default: "ReactiveMongoAcolyte")
   */
  def actorSystem(name: String = "ReactiveMongoAcolyte"): AkkaSystem = new ActorSystem(AkkaSystem(name), new ActorRefFactory() {
    def before(system: AkkaSystem, then: ActorRef): ActorRef = {
      system actorOf Props(classOf[Actor], then)
    }
  })
}

final class Actor(then: ActorRef) extends akka.actor.Actor {
  @annotation.tailrec
  def go(chan: ChannelBuffer, body: Array[Byte] = Array(), enc: String = "UTF-8"): Array[Byte] = {
    val len = chan.readableBytes()

    if (len == 0) body
    else {
      val buff = new Array[Byte](len)
      chan.readBytes(buff)

      go(chan, body ++ buff, enc)
    }
  }

  def receive = {
    case msg @ CheckedWriteRequestExpectingResponse(req) ⇒
      println(s"wreq = $req")
      then forward msg
    case msg @ RequestMakerExpectingResponse(RequestMaker(Query(_ /*flags*/ , coln, off, len), doc, _ /*pref*/ , _ /*channelId*/ )) ⇒
      val readable = ArrayReadableBuffer(go(doc.merged))
      val bson = DefaultBufferHandler.readDocument(readable)

      println(s"""expecting = Query($coln, $off, $len), [${bson.map(_.elements)}]""")
      then forward msg
    case close @ Close ⇒ /* Do nothing */ then forward close
    case msg ⇒
      println(s"message = $msg")
      then forward msg
  }
}
