package acolyte.reactivemongo

import shaded.netty.buffer.ChannelBuffer

import reactivemongo.bson.{
  BSONArray,
  BSONDocument,
  BSONElement,
  BSONString,
  BSONValue
}
import reactivemongo.bson.buffer.{
  ArrayBSONBuffer,
  ReadableBuffer,
  WritableBuffer
}

/**
 * Request executed against Mongo connection.
 */
trait Request {

  /** Fully qualified name of collection */
  def collection: String

  /** Request body (BSON statement) */
  def body: List[BSONDocument]

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
    val body = parse(go(buffer), Nil)
  }

  /** Parses body documents from prepared buffer. */
  @annotation.tailrec
  private def parse(buf: ReadableBuffer, body: List[BSONDocument]): List[BSONDocument] = if (buf.readable == 0) return body else {
    val doc = BSONDocument.read(buf)
    parse(buf, body :+ doc)
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

  /**
   * Request extractor.
   *
   * {{{
   * import reactivemongo.bson.BSONString
   * import acolyte.reactivemongo.Request
   *
   * request match {
   *   case Request("db.col", _) => // Any request on "db.col"
   *     resultA
   *
   *   case Request(colName, SimpleBody((k1, v1) :: (k2, v2) :: Nil)) =>
   *     // Any request with exactly 2 BSON properties
   *     resultB
   *
   *   case Request("db.col", SimpleBody(("email", BSONString(v)) :: _)) =>
   *     // Request on db.col starting with an "email" string property
   *     resultC
   *
   *   case Request("db.col", SimpleBody(("name", BSONString("eman")) :: _)) =>
   *     // Request on db.col starting with an "name" string property,
   *     // whose value is "eman"
   *     resultD
   *
   *   case Request(_, SimpleBody(("age": ValueDocument(
   *     ("\$gt", BSONInteger(minAge)) :: Nil)))) =>
   *     // Request on any collection, with an "age" document as property,
   *     // itself with exactly one integer "\$gt" property
   *     // e.g. `{ 'age': { '\$gt', 10 } }`
   *     resultE
   * }
   * }}}
   *
   * @return Collection name -> request body
   * @see SimpleBody
   * @see ValueDocument
   * @see CountRequest
   */
  def unapply(q: Request): Option[(String, List[BDoc])] =
    Some(q.collection → q.body.map(BDoc.apply))
}

/** BSONDocument wrapper for pattern matching */
case class BDoc(underlying: BSONDocument)

/**
 * Body extractor for simple request, with only one document.
 * If there are more than one document, matching just ignore extra ones.
 */
object SimpleBody {
  /** @return BSON properties from the first document of the body. */
  def unapply(body: List[BDoc]): Option[List[(String, BSONValue)]] =
    body.headOption.map(_.underlying.elements.map {
      case BSONElement(name, value) ⇒ name → value
    }.toList)

}

/** Complete request body extractor; Matches body with many documents. */
object RequestBody {
  /** @return List of document, each document as list of its BSON properties. */
  def unapply(body: List[BDoc]): Option[List[List[(String, BSONValue)]]] =
    Some(body.map(_.underlying.elements.map {
      case BSONElement(name, value) ⇒ name → value
    }.toList))
}

/** Insert request */
object InsertRequest {
  /** @return Collection name and elements of document to be inserted. */
  def unapply(insert: (WriteOp, Request)): Option[(String, List[(String, BSONValue)])] = (insert._1, insert._2.body) match {
    case (InsertOp, body :: Nil) ⇒
      Some(insert._2.collection → (body.elements.map {
        case BSONElement(name, value) ⇒ name → value
      }.toList))
    case _ ⇒ None
  }
}

/** Update request */
object UpdateRequest {
  /** @return Collection name, elements of selector/document to be updated. */
  def unapply(update: (WriteOp, Request)): Option[(String, List[(String, BSONValue)], List[(String, BSONValue)])] = (update._1, update._2.body) match {
    case (UpdateOp, selector :: body :: Nil) ⇒
      Some((
        update._2.collection,
        selector.elements.map {
          case BSONElement(name, value) ⇒ name → value
        }.toList,
        body.elements.map {
          case BSONElement(name, value) ⇒ name → value
        }.toList
      ))
    case _ ⇒ None
  }
}

/** Delete request */
object DeleteRequest {
  /** @return Collection name and elements of selector. */
  def unapply(delete: (WriteOp, Request)): Option[(String, List[(String, BSONValue)])] = (delete._1, delete._2.body) match {
    case (DeleteOp, selector :: Nil) ⇒
      Some(delete._2.collection → (selector.elements.map {
        case BSONElement(name, value) ⇒ name → value
      }.toList))
    case _ ⇒ None
  }
}

/** Request extractor for any command (at DB or collection level) */
object CommandRequest {
  /** @return The body of the command request */
  def unapply(request: Request): Option[List[(String, BSONValue)]] =
    request match {
      case Request(c, SimpleBody(body)) if (c endsWith ".$cmd") ⇒
        Some(body)

      case _ ⇒ None
    }
}

/** Request extractor for the `findAndModify` command */
object FindAndModifyRequest {
  /**
   * @return Collection name, query, update and then options
   */
  def unapply(request: Request): Option[(String, List[(String, BSONValue)], List[(String, BSONValue)], List[(String, BSONValue)])] = request match {
    case CommandRequest(("findAndModify", BSONString(col)) :: ps) ⇒ {
      var q = List.empty[(String, BSONValue)]
      var u = List.empty[(String, BSONValue)]
      val o = List.newBuilder[(String, BSONValue)]

      ps.foreach {
        case ("query", ValueDocument(query))   ⇒ q = query
        case ("update", ValueDocument(update)) ⇒ u = update
        case opt                               ⇒ o += opt
      }

      Some((col, q, u, o.result()))
    }

    case _ ⇒ None
  }
}

/**
 * Extractor of properties for a document used a BSON value
 * (when operator is used, e.g. `{ 'age': { '\$gt': 10 } }`).
 *
 * @see Request
 * @see Property
 */
object ValueDocument {
  def unapply(v: BSONValue): Option[List[(String, BSONValue)]] = v match {
    case doc @ BSONDocument(_) ⇒ Some(doc.elements.map {
      case BSONElement(name, value) ⇒ name → value
    }.toList)
    case _ ⇒ None
  }
}

/**
 * Extracts values of BSON array as list.
 * @see ValueDocument
 */
object ValueList {
  def unapply(arr: BSONArray): Option[List[BSONValue]] = Some(arr.values.toList)
}

/**
 * Body extractor for Count request.
 * @see SimpleBody
 */
object CountRequest {
  /**
   * @return Collection name -> query body (count BSON properties)
   */
  def unapply(q: Request): Option[(String, List[(String, BSONValue)])] =
    q match {
      case Request(col, SimpleBody(("count", BSONString(_)) ::
        ("query", ValueDocument(query)) :: _)) ⇒ Some(col → query)
      // TODO: limit

      case _ ⇒ None
    }
}

/**
 * In clause extractor
 * (\$in with BSONArray; e.g. { '\$in': [ ... ] })
 */
object InClause {
  /**
   * Matches BSON property with name \$in and a BSONArray as value,
   * and extracts subvalues from the array.
   */
  def unapply(bson: BSONValue): Option[List[BSONValue]] =
    bson match {
      case ValueDocument(("$in", a @ BSONArray(_)) :: _) ⇒
        Some(a.values.toList)
      case _ ⇒ None
    }
}

/**
 * Not-In clause extractor.
 * (\$nin with BSONArray; e.g. { '\$nin': [ ... ] })
 */
object NotInClause {
  /**
   * Matches BSON property with name \$nin and a BSONArray as value,
   * and extracts subvalues from the array.
   */
  def unapply(bson: BSONValue): Option[List[BSONValue]] =
    bson match {
      case ValueDocument(("$nin", a @ BSONArray(_)) :: _) ⇒
        Some(a.values.toList)
      case _ ⇒ None
    }
}

/**
 * Meta-extractor, to combine extractor on BSON properties.
 * @see SimpleBody
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
 * import acolyte.reactivemongo.{ Request, SimpleBody, Property, & }
 *
 * val EmailXtr = Property("email") // Without scalac plugin
 *
 * request match {
 *   case Request("db.col", SimpleBody(~(Property("email"), BSONString(e)))) =>
 *     // Request on db.col with an "email" string property,
 *     // anywhere in properties (possibly with others which are ignored there),
 *     // with `e` bound to extracted string value.
 *     resultA
 *
 *   case Request("db.col", SimpleBody(EmailXtr(BSONString(e)))) =>
 *     // Request on db.col with an "email" string property,
 *     // anywhere in properties (possibly with others which are ignored there),
 *     // with `e` bound to extracted string value.
 *     resultB // similar to case resultA without scalac plugin
 *
 *   case Request("db.col", SimpleBody(
 *     ~(Property("name"), BSONString("eman")))) =>
 *     // Request on db.col with an "name" string property with "eman" as value,
 *     // anywhere in properties (possibly with others which are ignored there).
 *     resultC
 *
 *   case Request(colName, SimpleBody(
 *     ~(Property("age"), BSONInteger(age)) &
 *     ~(Property("email"), BSONString(v)))) =>
 *     // Request on any collection, with an "age" integer property
 *     // and an "email" string property, possibly not in this order.
 *     resultD
 *
 *   case Request(colName, SimpleBody(
 *     ~(Property("age"), ValueDocument(
 *       ~(Property("\$gt"), BSONInteger(minAge)))) &
 *     ~(Property("email"), BSONString("demo@applicius.fr")))) =>
 *     // Request on any collection, with an "age" property with itself
 *     // a operator property "\$gt" having an integer value, and an "email"
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
    properties.collectFirst { case (`name`, value) ⇒ value }
}

/** Operator, along with request when writing. */
sealed trait WriteOp

/** Delete operator */
case object DeleteOp extends WriteOp

/** Insert operator */
case object InsertOp extends WriteOp

/** Update operator */
case object UpdateOp extends WriteOp
