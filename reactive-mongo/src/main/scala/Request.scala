package acolyte.reactivemongo

import reactivemongo.io.netty.buffer.ByteBuf

import reactivemongo.api.bson.{
  BSONArray,
  BSONDocument,
  BSONElement,
  BSONInteger,
  BSONString,
  BSONValue
}
import reactivemongo.api.bson.buffer.acolyte.{ readDocument, readableBuffer }

/**
 * Request executed against Mongo connection.
 */
trait Request {

  /** Fully qualified name of collection */
  def collection: String

  /** Request body (BSON statement) */
  def body: List[BSONDocument]

  /** See [[Request.pretty]] */
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
  def apply(name: String, buffer: ByteBuf): Request = new Request {
    val collection = name

    val body = parse(buffer, Nil)
  }

  /**
   * Returns a string representation of the given request,
   * for pretty-print it (debug).
   */
  def pretty(
      request: Request
    ): String = s"""Request(${request.collection}, [ ${request.body.map(
      BSONDocument.pretty
    ) mkString ", "} ])"""

  /** Parses body documents from prepared buffer. */
  @annotation.tailrec
  def parse(
      buf: ByteBuf,
      body: List[BSONDocument]
    ): List[BSONDocument] = {
    if (buf.readableBytes() == 0) {
      body.reverse
    } else {
      val sz = buf.getIntLE(buf.readerIndex)

      if (sz == 0) {
        body.reverse
      } else {
        val bytes = Array.ofDim[Byte](sz)

        // Avoid .readBytes(sz) which internally allocate a ByteBuf
        // (which would require to manage its release)
        buf.readBytes(bytes)

        val doc = readDocument(readableBuffer(bytes))

        parse(buf, doc :: body)
      }
    }
  }

  /**
   * Request extractor.
   *
   * {{{
   * import reactivemongo.api.bson.BSONInteger
   * import acolyte.reactivemongo.{
   *   PreparedResponse, Request, SimpleBody, ValueDocument
   * }
   *
   * def resultA: PreparedResponse = ???
   * def resultB: PreparedResponse = ???
   * def resultC: PreparedResponse = ???
   * def resultD: PreparedResponse = ???
   * def resultE: PreparedResponse = ???
   *
   * def check(request: Request) = request match {
   *   case Request("db.col", _) => // Any request on "db.col"
   *     resultA
   *
   *   case Request(colName, SimpleBody((k1, v1) :: (k2, v2) :: Nil)) =>
   *     // Any request with exactly 2 BSON properties
   *     println(colName + " -> " + (k1, v1).toString + ", " + (k2, v2).toString)
   *     resultB
   *
   *   case Request(_, SimpleBody(("age", ValueDocument(
   *     ("\\$gt", BSONInteger(minAge)) :: Nil)) :: _)) =>
   *     // Request on any collection, with an "age" document as property,
   *     // itself with exactly one integer "\\$gt" property
   *     // e.g. `{ 'age': { '\\$gt', 10 } }`
   *     println("minAge=" + minAge)
   *     resultE
   *
   *   case req =>
   *     sys.error("req = " + req)
   * }
   * }}}
   *
   * @return Collection name -> request body
   * @see [[SimpleBody]]
   * @see [[ValueDocument]]
   * @see [[CountRequest]]
   */
  def unapply(q: Request): Option[(String, List[BDoc])] =
    Some(q.collection -> q.body.map(BDoc.apply))
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
    body.headOption.map(_.underlying.elements.collect {
      case BSONElement(name, value) => name -> value
    }.toList)

}

/** Complete request body extractor; Matches body with many documents. */
object RequestBody {

  /** @return List of document, each document as list of its BSON properties. */
  def unapply(body: List[BDoc]): Option[List[List[(String, BSONValue)]]] =
    Some(body.map(_.underlying.elements.collect {
      case BSONElement(name, value) => name -> value
    }.toList))
}

/** Insert request */
object InsertRequest {

  /** @return Collection name and elements of document to be inserted. */
  def unapply(
      insert: (WriteOp, Request)
    ): Option[(String, List[(String, BSONValue)])] =
    (insert._1, insert._2.body) match {
      case (InsertOp, body :: Nil) =>
        Some(insert._2.collection -> (body.elements.collect {
          case BSONElement(name, value) => name -> value
        }.toList))

      case _ => None
    }
}

/** Update request */
object UpdateElement {

  /**
   * Extracts an update element: query (q), update operator (u),
   * and option `upsert` and `multi`.
   *
   * Matches value of the following form:
   *
   * `{ "q": { .. }, "u": { .. }, "upsert": true, "multi": false }`
   *
   * @return (q, u, upsert, multi)
   */
  def unapply(
      value: BSONValue
    ): Option[(BSONDocument, BSONValue, Boolean, Boolean)] = value match {
    case el: BSONDocument =>
      for {
        q <- el.getAsOpt[BSONDocument]("q")
        u <- el.getAsOpt[BSONValue]("u")
        upsert = el.getAsOpt[Boolean]("upsert").getOrElse(false)
        multi = el.getAsOpt[Boolean]("multi").getOrElse(false)
      } yield (q, u, upsert, multi)

    case _ => None
  }
}

/** Update request */
object UpdateRequest {

  /** @return Collection name, elements of selector/document to be updated. */
  def unapply(
      update: (WriteOp, Request)
    ): Option[(String, List[(String, BSONValue)], List[(String, BSONValue)], Boolean, Boolean)] =
    (update._1, update._2.body) match {
      case (UpdateOp, first :: _) =>
        UpdateElement.unapply(first).collect {
          case (selector, doc: BSONDocument, upsert, multi) =>
            (
              update._2.collection,
              selector.elements.collect {
                case BSONElement(name, value) => name -> value
              }.toList,
              doc.elements.collect {
                case BSONElement(name, value) => name -> value
              }.toList,
              upsert,
              multi
            )

          case (selector, arr: BSONArray, upsert, multi) =>
            (
              update._2.collection,
              selector.elements.collect {
                case BSONElement(name, value) => name -> value
              }.toList,
              arr.values.collect {
                case ValueDocument((name, stage) :: Nil) =>
                  name -> stage

              }.toList,
              upsert,
              multi
            )
        }

      case _ => None
    }
}

/** Delete request */
object DeleteRequest {

  /** @return Collection name and elements of selector. */
  def unapply(
      delete: (WriteOp, Request)
    ): Option[(String, List[(String, BSONValue)])] =
    (delete._1, delete._2.body) match {
      case (
            DeleteOp,
            ValueDocument(("q", selector: BSONDocument) :: _) :: Nil
          ) =>
        Some(delete._2.collection -> (selector.elements.collect {
          case BSONElement(name, value) => name -> value
        }.toList))

      case (
            DeleteOp,
            ValueDocument(("q", selector: BSONDocument) :: _) ::
            ValueDocument(_ /*options*/ ) :: _
          ) =>
        Some(delete._2.collection -> (selector.elements.collect {
          case BSONElement(name, value) => name -> value
        }.toList))

      case _ => None
    }
}

/** Request extractor for any command (at DB or collection level) */
object CommandRequest {

  /** @return The body of the command request */
  def unapply(request: Request): Option[List[(String, BSONValue)]] =
    request match {
      case Request(c, SimpleBody(body)) if (c endsWith f".$$cmd") =>
        Some(body)

      case _ => None
    }
}

/**
 * Request extractor for `startSession` command (at DB level).
 *
 * {{{
 * import acolyte.reactivemongo.{ Request, StartSessionRequest }
 *
 * def isStart(req: Request): Boolean = req match {
 *   case StartSessionRequest() => true
 *   case _ => false
 * }
 * }}}
 */
object StartSessionRequest {

  def unapply(request: Request): Boolean = request match {
    case CommandRequest(("startSession", BSONInteger(1)) :: _) =>
      true

    case _ =>
      false
  }
}

object StartTransactionRequest {

  def unapply(request: Request): Option[String] = request match {
    case Request(name, SimpleBody(Nil)) if (name.indexOf(".startx") != -1) =>
      Some(name)

    case _ =>
      None
  }
}

/**
 * Request extractor for the `findAndModify` command.
 *
 * @see [[QueryResponse.findAndModify]]
 */
object FindAndModifyRequest {

  /**
   * @return Collection name, query, update and then options
   */
  def unapply(
      request: Request
    ): Option[(String, List[(String, BSONValue)], List[(String, BSONValue)], List[(String, BSONValue)])] =
    request match {
      case CommandRequest(("findAndModify", BSONString(col)) :: ps) => {
        var q = List.empty[(String, BSONValue)]
        var u = List.empty[(String, BSONValue)]
        val o = List.newBuilder[(String, BSONValue)]

        ps.foreach {
          case ("query", ValueDocument(query))   => q = query
          case ("update", ValueDocument(update)) => u = update
          case opt                               => o += opt
        }

        Some((col, q, u, o.result()))
      }

      case _ => None
    }
}

object AggregateRequest {

  /**
   * @return Collection name, pipeline stages and then options
   */
  def unapply(
      request: Request
    ): Option[(String, List[BSONDocument], List[(String, BSONValue)])] =
    request match {
      case CommandRequest(
            ("aggregate", BSONString(col)) ::
            ("pipeline", ValueList(stages)) :: opts
          ) =>
        Some((col, stages.collect { case stage: BSONDocument => stage }, opts))

      case _ =>
        None
    }
}

/**
 * Extractor of properties for a document used a BSON value
 * (when operator is used, e.g. `{ 'age': { '\$gt': 10 } }`).
 *
 * @see [[Request]]
 * @see [[Property]]
 */
object ValueDocument {

  def unapply(v: BSONValue): Option[List[(String, BSONValue)]] = {
    v match {
      case doc: BSONDocument =>
        Some(doc.elements.collect {
          case BSONElement(name, value) => name -> value
        }.toList)

      case _ => None
    }
  }
}

/**
 * Extracts values of BSON array as list.
 * @see [[ValueDocument]]
 */
object ValueList {
  def unapply(arr: BSONArray): Option[List[BSONValue]] = Some(arr.values.toList)
}

/**
 * Body extractor for Count request.
 * @see [[SimpleBody]]
 */
object CountRequest {

  /**
   * @return Collection name -> query body (count BSON properties)
   */
  def unapply(request: Request): Option[(String, Int, List[(String, BSONValue)])] =
    request match {
      case CommandRequest(
            ("count", BSONString(col)) ::
            ("query", ValueDocument(query)) :: _
          ) =>
        Some((col, 0, query))

      case CommandRequest(
            ("count", BSONString(col)) ::
            ("skip", BSONInteger(skip)) ::
            ("query", ValueDocument(query)) :: _
          ) =>
        Some((col, skip, query))

      // TODO: limit

      case _ => None
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
      case ValueDocument(("$in", a: BSONArray) :: _) =>
        Some(a.values.toList)
      case _ => None
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
      case ValueDocument(("$nin", a: BSONArray) :: _) =>
        Some(a.values.toList)
      case _ => None
    }
}

/**
 * Meta-extractor, to combine extractor on BSON properties.
 * @see [[SimpleBody]]
 * @see [[Property]]
 */
@SuppressWarnings(Array("ObjectNames"))
object & {
  def unapply[A](a: A) = Some(a -> a)
}

/**
 * Extractor for BSON property,
 * allowing partial and un-ordered match by name.
 *
 * {{{
 * import reactivemongo.api.bson.BSONString
 * import acolyte.reactivemongo.{
 *   PreparedResponse, Property, Request, SimpleBody
 * }
 *
 * val EmailXtr = Property("email") // Without scalac plugin
 *
 * def resultA: PreparedResponse = ???
 * def resultB: PreparedResponse = ???
 * def resultC: PreparedResponse = ???
 * def resultD: PreparedResponse = ???
 * def resultE: PreparedResponse = ???
 *
 * def check(request: Request) = request match {
 *   case Request("db.col", SimpleBody(EmailXtr(BSONString(e)))) =>
 *     // Request on db.col with an "email" string property,
 *     // anywhere in properties (possibly with others which are ignored there),
 *     // with `e` bound to extracted string value.
 *     println(e)
 *     resultB // similar to case resultA without scalac plugin
 *
 *   case req =>
 *     sys.error("Unexpected request: " + req)
 * }
 * }}}
 *
 * @see [[&]]
 * @see [[ValueDocument]]
 */
case class Property(name: String) {

  def unapply(properties: List[(String, BSONValue)]): Option[BSONValue] =
    properties.collectFirst { case (`name`, value) => value }
}

/** Operator, along with request when writing. */
sealed trait WriteOp

/** Delete operator */
case object DeleteOp extends WriteOp

/** Insert operator */
case object InsertOp extends WriteOp

/** Update operator */
case object UpdateOp extends WriteOp
