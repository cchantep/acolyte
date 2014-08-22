package acolyte.reactivemongo

import _root_.reactivemongo.bson.BSONDocument

/** Response to Mongo query executed with Acolyte driver. */
sealed trait QueryResponse

/** Mongo Error, in response to some request. */
case class QueryError(message: String) extends QueryResponse

/** Query result */
case class QueryResult(content: Seq[BSONDocument]) extends QueryResponse
