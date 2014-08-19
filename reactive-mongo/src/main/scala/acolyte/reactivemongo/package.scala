package acolyte

package object reactivemongo {
  import _root_.reactivemongo.bson.BSONDocument

  /**
   * Mongo query handler.
   * If returns `None`, next handler is called.
   */
  type QueryHandler = Query ⇒ Option[Seq[BSONDocument]]
}
