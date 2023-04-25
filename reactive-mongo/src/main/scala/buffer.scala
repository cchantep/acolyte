package reactivemongo.api.bson.buffer // as a friend project

import reactivemongo.api.bson.BSONDocument

package object acolyte {
  type ReadableBuffer = reactivemongo.api.bson.buffer.ReadableBuffer
  type WritableBuffer = reactivemongo.api.bson.buffer.WritableBuffer

  @inline def writable(): WritableBuffer =
    reactivemongo.api.bson.buffer.WritableBuffer.empty

  def readableBuffer(bytes: Array[Byte]) =
    reactivemongo.api.bson.buffer.ReadableBuffer(bytes)

  @inline def readDocument(buf: ReadableBuffer): BSONDocument =
    DefaultBufferHandler.readDocument(buf)

  @inline def writeDocument(
      doc: BSONDocument,
      buf: WritableBuffer
    ): WritableBuffer =
    DefaultBufferHandler.writeDocument(doc, buf)

}
