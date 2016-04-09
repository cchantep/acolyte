package acolyte.reactivemongo

import java.nio.ByteOrder

import shaded.netty.buffer.ChannelBuffers

import scala.util.Try

import reactivemongo.bson.{ BSONDocument, BSONInteger, BSONValue }
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
   * Builds a response with error details for a query.
   *
   * @param channelId Unique ID of channel
   * @param error Error message
   */
  def QueryError(channelId: Int, error: String, code: Option[Int] = None): Try[Response] = mkResponse(channelId, 1 /*QueryFailure*/ , {
    val doc = BSONDocument("$err" -> error)
    code.fold(Seq(doc)) { c ⇒ Seq(doc ++ ("code" -> c)) }
  })

  /**
   * Builds a response for a success query.
   *
   * @param channelId Unique ID of channel
   * @param docs BSON documents
   */
  def QuerySuccess(channelId: Int, docs: Traversable[BSONDocument]): Try[Response] = mkResponse(channelId, 4 /* unspecified */ , docs)

  /**
   * Builds a response with error details for a write operation.
   *
   * @param channelId Uniqe ID of channel
   * @param error Error message
   * @param code Error code
   */
  def WriteError(channelId: Int, error: String, code: Option[Int] = None): Try[Response] = mkResponse(channelId, 4 /* unspecified */ , List(
    BSONDocument("ok" -> 0, "err" -> error, "errmsg" -> error,
      "code" -> code.getOrElse(-1), "updatedExisting" -> false, "n" -> 0)))

  /**
   * Builds a response for a successful write operation.
   *
   * @param channelId Unique ID of channel
   * @param count The number of documents affected by last command, 0 if none
   * @param updatedExisting Some existing document has been updated
   */
  def WriteSuccess(channelId: Int, count: Int, updatedExisting: Boolean = false): Try[Response] = mkResponse(channelId, 4 /*unspecified*/ ,
    List(BSONDocument(
      "ok" -> 1, "updatedExisting" -> updatedExisting, "n" -> count)))

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

  private[reactivemongo] def MkQueryError(channelId: Int = 0): Response =
    mkError(channelId, Array[Byte](76, 0, 0, 0, 16, -55, -63, 115, -49, 116, 119, 55, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 40, 0, 0, 0, 2, 36, 101, 114, 114, 0, 25, 0, 0, 0, 70, 97, 105, 108, 115, 32, 116, 111, 32, 99, 114, 101, 97, 116, 101, 32, 114, 101, 115, 112, 111, 110, 115, 101, 0, 0)) // "Fails to create response"

  private[reactivemongo] def MkWriteError(channelId: Int = 0): Response =
    mkError(channelId, Array[Byte](-126, 0, 0, 0, -29, 50, 14, 73, -115, -6, 46, 67, 4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 94, 0, 0, 0, 16, 111, 107, 0, 0, 0, 0, 0, 2, 101, 114, 114, 0, 25, 0, 0, 0, 70, 97, 105, 108, 115, 32, 116, 111, 32, 99, 114, 101, 97, 116, 101, 32, 114, 101, 115, 112, 111, 110, 115, 101, 0, 2, 101, 114, 114, 109, 115, 103, 0, 25, 0, 0, 0, 70, 97, 105, 108, 115, 32, 116, 111, 32, 99, 114, 101, 97, 116, 101, 32, 114, 101, 115, 112, 111, 110, 115, 101, 0, 16, 99, 111, 100, 101, 0, -1, -1, -1, -1, 0))

  @inline private def mkError(channelId: Int, docs: Array[Byte]): Response = {
    val buf = ChannelBuffers.unmodifiableBuffer(
      ChannelBuffers.copiedBuffer(ByteOrder.LITTLE_ENDIAN, docs))

    Response(MessageHeader(buf), Reply(buf), buf, ResponseInfo(channelId))
  }
}

/** Response prepared for Mongo request executed with Acolyte driver. */
trait PreparedResponse {
  /** Applies this response to specified Mongo channel. */
  def apply(chanId: Int): Option[Try[Response]]
}
