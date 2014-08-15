package acolyte.reactivemongo

import reactivemongo.bson.{ BSONDocument, BSONValue }

/**
 * Query executed against Mongo connection.
 */
trait Query {

  /**
   * Fully qualified name of collection
   */
  def collection: String

  /**
   * Query body (BSON statement)
   */
  def body: BSONDocument
}

import org.jboss.netty.buffer.ChannelBuffer
import reactivemongo.bson.buffer.{
  ArrayBSONBuffer,
  ReadableBuffer,
  WritableBuffer
}

/** Query companion */
object Query {
  /**
   * Parses query for specified collection from given `buffer`.
   *
   * @param name Fully qualified name of collection
   * @param buffer Bytes to be parsed as BSON body
   */
  def apply(name: String, buffer: ChannelBuffer): Query = new Query {
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
 * @see QueryBody
 * @see Property
 */
object ValueDocument {
  def unapply(v: BSONValue): Option[List[(String, BSONValue)]] = v match {
    case doc @ BSONDocument(_) ⇒ Some(doc.elements.toList)
    case _                     ⇒ None
  }
}

/**
 * Query body extractor.
 *
 * {{{
 * import reactivemongo.bson.BSONString
 * import acolyte.reactivemongo.QueryBody
 *
 * query match {
 *   case QueryBody("db.col", _) => // Any query on "db.col"
 *     resultA
 * 
 *   case QueryBody(colName, (k1, v1) :: (k2, v2) :: Nil) =>
 *     // Any query with exactly 2 BSON properties
 *     resultB
 *
 *   case QueryBody("db.col", ("email", BSONString(v)) :: _) =>
 *     // Query on db.col starting with an "email" string property
 *     resultC
 *
 *   case QueryBody("db.col", ("name", BSONString("eman")) :: _) =>
 *     // Query on db.col starting with an "name" string property,
 *     // whose value is "eman"
 *     resultD
 *
 *   case QueryBody(_, ("age": ValueDocument(
 *     ("$gt", BSONInteger(minAge)) :: Nil))) =>
 *     // Query on any collection, with an "age" document as property,
 *     // itself with exactly one integer "$gt" property
 *     // e.g. `{ 'age': { '$gt', 10 } }`
 *     resultE
 * }
 * }}}
 *
 * @see ValueDocument
 */
object QueryBody {
  def unapply(q: Query): Option[(String, List[(String, BSONValue)])] =
    Some(q.collection -> q.body.elements.toList)

}

/**
 * Meta-extractor, to combine extractor on BSON properties.
 * @see QueryBody
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
 * import acolyte.reactivemongo.{ QueryBody, Property, & }
 *
 * val EmailXtr = Property("email") // Without scalac plugin
 *
 * query match {
 *   case QueryBody("db.col", ~(Property("email"), BSONString(e))) =>
 *     // Query on db.col with an "email" string property,
 *     // anywhere in properties (possibly with others which are ignored there),
 *     // with `e` bound to extracted string value.
 *     resultA
 *
 *   case QueryBody("db.col", EmailXtr(BSONString(e))) =>
 *     // Query on db.col with an "email" string property,
 *     // anywhere in properties (possibly with others which are ignored there),
 *     // with `e` bound to extracted string value.
 *     resultB // similar to case resultA without scalac plugin
 *
 *   case QueryBody("db.col", ~(Property("name"), BSONString("eman"))) =>
 *     // Query on db.col with an "name" string property with "eman" as value,
 *     // anywhere in properties (possibly with others which are ignored there).
 *     resultC
 *
 *   case QueryBody(colName,
 *     ~(Property("age"), BSONInteger(age)) &
 *     ~(Property("email"), BSONString(v))) =>
 *     // Query on any collection, with an "age" integer property
 *     // and an "email" string property, possibly not in this order.
 *     resultD
 *
 *   case QueryBody(colName,
 *     ~(Property("age"), ValueDocument(
 *       ~(Property("$gt"), BSONInteger(minAge)))) &
 *     ~(Property("email"), BSONString("demo@applicius.fr"))) =>
 *     // Query on any collection, with an "age" property with itself
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
