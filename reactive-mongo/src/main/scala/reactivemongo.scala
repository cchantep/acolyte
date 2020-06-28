package reactivemongo // as a friend project

import scala.concurrent.Promise

import reactivemongo.core.actors.{ ExpectingResponse => ExpResp }

import reactivemongo.core.protocol.{
  ProtocolMetadata,
  RequestMaker => ReqMkr,
  RequestOp,
  Response
}

import reactivemongo.core.netty.ChannelFactory

import reactivemongo.api.MongoConnectionOptions

import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONSerializationPack

package object acolyte {
  type Authenticate = reactivemongo.core.nodeset.Authenticate

  type Connection = reactivemongo.core.nodeset.Connection

  // ---

  object Close {
    def unapply(that: Any): Boolean = that match {
      case reactivemongo.core.actors.Close |
        reactivemongo.core.actors.Close(_) => true

      case _ => false
    }
  }

  def Closed = reactivemongo.core.actors.Closed

  type Delete = reactivemongo.core.protocol.Delete

  object Delete {
    @inline def apply(fullCollectionName: String, flags: Int) =
      new reactivemongo.core.protocol.Delete(fullCollectionName, flags)

    def unapply(delete: reactivemongo.core.protocol.Delete) =
      Some(delete.fullCollectionName -> delete.flags)
  }

  object Insert {
    @inline def apply(flags: Int, fullCollectionName: String) =
      new reactivemongo.core.protocol.Insert(flags, fullCollectionName)

    def unapply(insert: reactivemongo.core.protocol.Insert) =
      Some(insert.flags -> insert.fullCollectionName)
  }

  type MessageHeader = reactivemongo.core.protocol.MessageHeader

  lazy val MessageHeader = reactivemongo.core.protocol.MessageHeader.apply _

  def readReply(buf: reactivemongo.io.netty.buffer.ByteBuf) =
    reactivemongo.core.protocol.Reply.readFrom(buf)

  type Response = reactivemongo.core.protocol.Response

  lazy val Response = reactivemongo.core.protocol.Response.apply _

  type ResponseInfo = reactivemongo.core.protocol.ResponseInfo

  object ResponseInfo {
    @inline def apply(channelId: reactivemongo.io.netty.channel.ChannelId) =
      new ResponseInfo(channelId)
  }

  object Update {
    @inline def apply(fullCollectionName: String, flags: Int) =
      new reactivemongo.core.protocol.Update(fullCollectionName, flags)

    def unapply(update: reactivemongo.core.protocol.Update) =
      Some(update.fullCollectionName -> update.flags)
  }

  object Query {
    import reactivemongo.core.protocol.{ Query => Qry }

    def unapply(q: Qry): Option[(Int, String, Int, Int)] = q match {
      case Qry(a, b, c, d) => Some((a, b, c, d))
      case _               => None
    }
  }

  type WriteRequestOp = reactivemongo.core.protocol.WriteRequestOp

  type RequestMaker = ReqMkr

  object RequestMaker {
    import reactivemongo.api.ReadPreference
    import reactivemongo.core.netty.BufferSequence
    import reactivemongo.io.netty.channel.ChannelId

    def unapply(mkr: ReqMkr): Option[(RequestOp, BufferSequence, ReadPreference, Option[ChannelId])] = mkr match {
      case ReqMkr(a, b, c, d) => Some((a, b, c, d))
      case _                  => None
    }
  }

  type RegisterMonitor = reactivemongo.core.actors.RegisterMonitor.type

  object PrimaryAvailable {
    @inline def apply(p: ProtocolMetadata) =
      reactivemongo.core.actors.PrimaryAvailable(p)
  }

  object SetAvailable {
    @inline def apply(p: ProtocolMetadata) =
      reactivemongo.core.actors.SetAvailable(p)
  }

  // ---

  object ExpectingResponse {
    def unapply(resp: ExpResp): Option[(ReqMkr, Promise[Response])] =
      resp match {
        case er: ExpResp =>
          Some(er.requestMaker -> resp.promise)

        case _ => None
      }
  }

  type Close = reactivemongo.core.actors.Close

  type MongoDBSystem = reactivemongo.core.actors.MongoDBSystem

  type MongoConnection = reactivemongo.api.MongoConnection

  object MongoConnection {
    @inline def apply(
      supervisor: String,
      name: String,
      actorSystem: akka.actor.ActorSystem,
      mongosystem: akka.actor.ActorRef,
      options: reactivemongo.api.MongoConnectionOptions) =
      new reactivemongo.api.MongoConnection(
        supervisor, name, actorSystem, mongosystem, options)
  }

  object ActorSystem {
    @inline def apply(drv: reactivemongo.api.AsyncDriver) = drv.system
  }

  // ---

  @inline def channelFactory(
    supervisor: String,
    name: String,
    options: MongoConnectionOptions) =
    new ChannelFactory(supervisor, name, options)

  val parseResponse = reactivemongo.core.protocol.Response.parse(_: Response)
}
