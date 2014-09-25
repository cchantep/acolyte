package acolyte.reactivemongo

import org.jboss.netty.buffer.ChannelBuffer

import reactivemongo.bson.{ BSONDocument, BSONString, BSONValue }
import reactivemongo.bson.buffer.{
  ArrayBSONBuffer,
  ReadableBuffer,
  WritableBuffer
}

/**
 * Request executed against Mongo connection.
 */
trait Request {

  /**
   * Fully qualified name of collection
   */
  def collection: String

  /**
   * Request body (BSON statement)
   */
  def body: BSONDocument

  override lazy val toString = s"Request($collection, $body)"
}

/** Request companion */
object Request {
  /**
   * Parses request for specified collection from given `buffer`.
   *
   * @param name Fully qualified name of collection
   * @param buffer Bytes to be parsed as BSON body
   */
  def apply(name: String, buffer: ChannelBuffer): Request = new Request {
    val collection = name
    val body = BSONDocument.read(go(buffer))
  }

  @annotation.tailrec
  private def go(chan: ChannelBuffer, body: WritableBuffer = new ArrayBSONBuffer()): ReadableBuffer = {
    val len = chan.readableBytes()

    if (len == 0) body.toReadableBuffer
    else {
      val buff = new Array[Byte](len)
      chan.readBytes(buff)

      go(chan, body.writeBytes(buff))
    }
  }
}

/**
 * Extractor of properties for a document used a BSON value
 * (when operator is used, e.g. `{ 'age': { '$gt': 10 } }`).
 *
 * @see RequestBody
 * @see Property
 */
object ValueDocument {
  def unapply(v: BSONValue): Option[List[(String, BSONValue)]] = v match {
    case doc @ BSONDocument(_) ⇒ Some(doc.elements.toList)
    case _                     ⇒ None
  }
}

/**
 * Request body extractor.
 *
 * {{{
 * import reactivemongo.bson.BSONString
 * import acolyte.reactivemongo.RequestBody
 *
 * request match {
 *   case RequestBody("db.col", _) => // Any request on "db.col"
 *     resultA
 *
 *   case RequestBody(colName, (k1, v1) :: (k2, v2) :: Nil) =>
 *     // Any request with exactly 2 BSON properties
 *     resultB
 *
 *   case RequestBody("db.col", ("email", BSONString(v)) :: _) =>
 *     // Request on db.col starting with an "email" string property
 *     resultC
 *
 *   case RequestBody("db.col", ("name", BSONString("eman")) :: _) =>
 *     // Request on db.col starting with an "name" string property,
 *     // whose value is "eman"
 *     resultD
 *
 *   case RequestBody(_, ("age": ValueDocument(
 *     ("$gt", BSONInteger(minAge)) :: Nil))) =>
 *     // Request on any collection, with an "age" document as property,
 *     // itself with exactly one integer "$gt" property
 *     // e.g. `{ 'age': { '$gt', 10 } }`
 *     resultE
 * }
 * }}}
 *
 * @see [[ValueDocument]]
 * @see [[CountRequest]]
 */
object RequestBody {
  /**
   * @return Collection name -> request body (BSON properties)
   */
  def unapply(q: Request): Option[(String, List[(String, BSONValue)])] =
    Some(q.collection -> q.body.elements.toList)

}

/**
 * Request extractor for Count command.
 * @see [[RequestBody]]
 */
object CountRequest {
  /**
   * @return Collection name -> query body (count BSON properties)
   */
  def unapply(q: Request): Option[(String, List[(String, BSONValue)])] =
    q match {
      case RequestBody(col, ("count", BSONString(_)) ::
        ("query", ValueDocument(query)) :: Nil) ⇒ Some(col -> query)
      case _ ⇒ None
    }
}

/**
 * Meta-extractor, to combine extractor on BSON properties.
 * @see RequestBody
 * @see Property
 */
object & {
  def unapply[A](a: A) = Some(a, a)
}

/**
 * Extractor for BSON property,
 * allowing partial and un-ordered match by name.
 * Rich match syntax `~(Property(name), ...)` requires use of
 * http://acolyte.eu.org/scalac-plugin.html
 *
 * {{{
 * import reactivemongo.bson.{ BSONInteger, BSONString }
 * import acolyte.reactivemongo.{ RequestBody, Property, & }
 *
 * val EmailXtr = Property("email") // Without scalac plugin
 *
 * request match {
 *   case RequestBody("db.col", ~(Property("email"), BSONString(e))) =>
 *     // Request on db.col with an "email" string property,
 *     // anywhere in properties (possibly with others which are ignored there),
 *     // with `e` bound to extracted string value.
 *     resultA
 *
 *   case RequestBody("db.col", EmailXtr(BSONString(e))) =>
 *     // Request on db.col with an "email" string property,
 *     // anywhere in properties (possibly with others which are ignored there),
 *     // with `e` bound to extracted string value.
 *     resultB // similar to case resultA without scalac plugin
 *
 *   case RequestBody("db.col", ~(Property("name"), BSONString("eman"))) =>
 *     // Request on db.col with an "name" string property with "eman" as value,
 *     // anywhere in properties (possibly with others which are ignored there).
 *     resultC
 *
 *   case RequestBody(colName,
 *     ~(Property("age"), BSONInteger(age)) &
 *     ~(Property("email"), BSONString(v))) =>
 *     // Request on any collection, with an "age" integer property
 *     // and an "email" string property, possibly not in this order.
 *     resultD
 *
 *   case RequestBody(colName,
 *     ~(Property("age"), ValueDocument(
 *       ~(Property("$gt"), BSONInteger(minAge)))) &
 *     ~(Property("email"), BSONString("demo@applicius.fr"))) =>
 *     // Request on any collection, with an "age" property with itself
 *     // a operator property "$gt" having an integer value, and an "email"
 *     // property (at the same level as age), without order constraint.
 *     resultE
 *
 * }
 * }}}
 *
 * @see &
 * @see ValueDocument
 */
case class Property(name: String) {
  def unapply(properties: List[(String, BSONValue)]): Option[BSONValue] =
    properties.toMap.lift(name)

}

/** Operator, along with request when writing. */
sealed trait WriteOp

/** Delete operator */
case object DeleteOp extends WriteOp

/** Insert operator */
case object InsertOp extends WriteOp

/** Update operator */
case object UpdateOp extends WriteOp
