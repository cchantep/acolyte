package acolyte.reactivemongo

import java.nio.ByteOrder

import org.jboss.netty.buffer.ChannelBuffers

import scala.util.Try

import reactivemongo.bson.BSONDocument
import reactivemongo.core.protocol.{
  Delete,
  Insert,
  MessageHeader,
  Reply,
  Response,
  ResponseInfo,
  Update,
  WriteRequestOp
}

/* MongoDB companion */
object MongoDB {

  /**
   * Builds an erroneous response.
   *
   * @param channelId Unique ID of channel
   * @param error Error message
   */
  def Error(channelId: Int, error: String): Try[Response] =
    mkResponse(channelId, 1 /*QueryFailure*/ ,
      Seq(BSONDocument("$err" -> error)))

  /**
   * Builds a successful response.
   *
   * @param channelId Unique ID of channel
   * @param docs BSON documents
   */
  def Success(channelId: Int, docs: Traversable[BSONDocument]): Try[Response] =
    mkResponse(channelId, 4 /* unspecified*/ , docs)

  /**
   * Builds a Mongo response.
   *
   * @param channelId Unique ID of channel
   * @param docs BSON documents
   */
  def mkResponse(channelId: Int, flags: Int, docs: Traversable[BSONDocument]): Try[Response] = Try {
    val body = new reactivemongo.bson.buffer.ArrayBSONBuffer()

    docs foreach { BSONDocument.write(_, body) }

    val len = 36 /* header size */ + body.index
    val buf = ChannelBuffers.buffer(ByteOrder.LITTLE_ENDIAN, len)

    buf.writeInt(len)
    buf.writeInt(System identityHashCode docs) // fake response ID
    buf.writeInt(System identityHashCode buf) // fake request ID
    buf.writeInt(4 /* OP_REPLY */ ) // opCode
    buf.writeInt(flags)
    buf.writeLong(0) // cursor ID
    buf.writeInt(0) // cursor starting from
    buf.writeInt(docs.size) // number of document
    buf.writeBytes(body.array)

    val in = ChannelBuffers.unmodifiableBuffer(buf)

    Response(MessageHeader(in), Reply(in), in, ResponseInfo(channelId))
  }

  /** Defines instance of WriteOp enum. */
  @inline def WriteOp(mongoOp: WriteRequestOp): Option[WriteOp] =
    mongoOp match {
      case Delete(_, _) ⇒ Some(DeleteOp)
      case Insert(_, _) ⇒ Some(InsertOp)
      case Update(_, _) ⇒ Some(UpdateOp)
      case _            ⇒ None
    }

  private[reactivemongo] def MkResponseError(channelId: Int = 0): Response = {
    val buf = ChannelBuffers.copiedBuffer(ByteOrder.LITTLE_ENDIAN, Array[Byte](76, 0, 0, 0, 16, -55, -63, 115, -49, 116, 119, 55, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 40, 0, 0, 0, 2, 36, 101, 114, 114, 0, 25, 0, 0, 0, 70, 97, 105, 108, 115, 32, 116, 111, 32, 99, 114, 101, 97, 116, 101, 32, 114, 101, 115, 112, 111, 110, 115, 101, 0, 0)) // "Fails to create response"
    val in = ChannelBuffers.unmodifiableBuffer(buf)

    Response(MessageHeader(in), Reply(in), in, ResponseInfo(channelId))
  }
}
