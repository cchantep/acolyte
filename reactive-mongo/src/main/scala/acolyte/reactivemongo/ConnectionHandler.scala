package acolyte.reactivemongo

/** Connection handler */
case class ConnectionHandler(queryHandler: QueryHandler) {

  /**
   * Creates a copy of this connection handler,
   * with given query `handler` appended.
   */
  def withQueryHandler(handler: QueryHandler): ConnectionHandler =
    copy({ q ⇒ queryHandler(q).orElse(handler(q)) })

}

/** Companion object for connection handler. */
object ConnectionHandler {

  // TODO
  lazy val empty: ConnectionHandler = ConnectionHandler(_ ⇒ ???)

}
