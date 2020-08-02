package acolyte.reactivemongo

import reactivemongo.io.netty.channel.{ ChannelId, DefaultChannelId }
import reactivemongo.io.netty.buffer.Unpooled

import scala.util.Try

import reactivemongo.api.bson.BSONDocument
import reactivemongo.acolyte.{
  readReply,
  Delete,
  Insert,
  MessageHeader,
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
   * @param chanId Unique ID of channel
   * @param error Error message
   */
  def queryError(chanId: ChannelId, error: String, code: Option[Int] = None): Try[Response] = mkResponse(chanId, 1 /*QueryFailure*/ , {
    val doc = BSONDocument("$err" → error)
    code.fold(Seq(doc)) { c ⇒ Seq(doc ++ ("code" → c)) }
  })

  /**
   * Builds a response for a success query.
   *
   * @param chanId Unique ID of channel
   * @param docs BSON documents
   */
  def querySuccess(chanId: ChannelId, docs: Traversable[BSONDocument]): Try[Response] = mkResponse(chanId, 4 /* unspecified */ , docs)

  /**
   * Builds a response with error details for a write operation.
   *
   * @param chanId Uniqe ID of channel
   * @param error Error message
   * @param code Error code
   */
  def writeError(chanId: ChannelId, error: String, code: Option[Int] = None): Try[Response] = mkResponse(chanId, 4 /* unspecified */ , List(
    BSONDocument("ok" → 0, "err" → error, "errmsg" → error,
      "code" → code.getOrElse(-1), "updatedExisting" → false, "n" → 0)))

  /**
   * Builds a response for a successful write operation.
   *
   * @param chanId Unique ID of channel
   * @param count The number of documents affected by last command, 0 if none
   * @param updatedExisting Some existing document has been updated
   */
  def writeSuccess(chanId: ChannelId, count: Int, updatedExisting: Boolean = false): Try[Response] = mkResponse(chanId, 4 /*unspecified*/ ,
    List(BSONDocument(
      "ok" → 1, "updatedExisting" → updatedExisting, "n" → count)))

  /**
   * Builds a Mongo response.
   *
   * @param chanId Unique ID of channel
   * @param docs BSON documents
   */
  def mkResponse(chanId: ChannelId, flags: Int, docs: Traversable[BSONDocument]): Try[Response] = Try {
    import _root_.reactivemongo.api.bson.buffer.acolyte.{
      writable,
      writeDocument
    }

    val body = writable()

    docs foreach { writeDocument(_, body) }

    val len = 36 /* header size */ + body.size
    val buf = Unpooled.buffer(len)

    buf.writeIntLE(len)
    buf.writeIntLE(System identityHashCode docs) // fake response ID
    buf.writeIntLE(System identityHashCode buf) // fake request ID
    buf.writeIntLE(4 /* OP_REPLY */ ) // opCode
    buf.writeIntLE(flags)
    buf.writeLongLE(0) // cursor ID
    buf.writeIntLE(0) // cursor starting from
    buf.writeIntLE(docs.size) // number of document
    buf.writeBytes(body.array)

    val in = Unpooled.wrappedUnmodifiableBuffer(buf)

    Response(MessageHeader(in), readReply(in), in, ResponseInfo(chanId))
  }

  /** Defines instance of WriteOp enum. */
  @inline def writeOp(mongoOp: WriteRequestOp): Option[WriteOp] =
    mongoOp match {
      case Delete(_, _) ⇒ Some(DeleteOp)
      case Insert(_, _) ⇒ Some(InsertOp)
      case Update(_, _) ⇒ Some(UpdateOp)
      case _            ⇒ None
    }

  private[reactivemongo] def mkQueryError(chanId: ChannelId = DefaultChannelId.newInstance()): Response = mkError(chanId, Array[Byte](76, 0, 0, 0, 16, -55, -63, 115, -49, 116, 119, 55, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 40, 0, 0, 0, 2, 36, 101, 114, 114, 0, 25, 0, 0, 0, 70, 97, 105, 108, 115, 32, 116, 111, 32, 99, 114, 101, 97, 116, 101, 32, 114, 101, 115, 112, 111, 110, 115, 101, 0, 0)) // "Fails to create response"

  private[reactivemongo] def mkWriteError(chanId: ChannelId = DefaultChannelId.newInstance()): Response = mkError(chanId, Array[Byte](-126, 0, 0, 0, -29, 50, 14, 73, -115, -6, 46, 67, 4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 94, 0, 0, 0, 16, 111, 107, 0, 0, 0, 0, 0, 2, 101, 114, 114, 0, 25, 0, 0, 0, 70, 97, 105, 108, 115, 32, 116, 111, 32, 99, 114, 101, 97, 116, 101, 32, 114, 101, 115, 112, 111, 110, 115, 101, 0, 2, 101, 114, 114, 109, 115, 103, 0, 25, 0, 0, 0, 70, 97, 105, 108, 115, 32, 116, 111, 32, 99, 114, 101, 97, 116, 101, 32, 114, 101, 115, 112, 111, 110, 115, 101, 0, 16, 99, 111, 100, 101, 0, -1, -1, -1, -1, 0))

  @inline private def mkError(chanId: ChannelId, docs: Array[Byte]): Response = {
    val buf = Unpooled.wrappedUnmodifiableBuffer(Unpooled.copiedBuffer(docs))

    Response(MessageHeader(buf), readReply(buf), buf, ResponseInfo(chanId))
  }
}

/** Response prepared for Mongo request executed with Acolyte driver. */
trait PreparedResponse {
  /** Applies this response to specified Mongo channel. */
  def apply(chanId: ChannelId): Option[Try[Response]]
}
