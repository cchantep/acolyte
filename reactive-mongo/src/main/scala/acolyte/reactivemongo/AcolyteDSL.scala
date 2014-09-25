package acolyte.reactivemongo

/** Acolyte DSL for ReactiveMongo. */
object AcolyteDSL extends WithDriver
    with WithDB with WithCollection with WithHandler with WithResult {

  /**
   * Creates an empty connection handler.
   *
   * {{{
   * import reactivemongo.api.MongoDriver
   * import acolyte.reactivemongo.AcolyteDSL.{ withDriver, handle }
   *
   * withDriver(handle) { d =>
   *   val driver: MongoDriver = d // configured with empty handler
   *   // work with driver (e.g. call you function using Mongo)
   *   "Value"
   * }
   * }}}
   * @see [[withDriver]]
   */
  def handle: ConnectionHandler = ConnectionHandler.empty

  /**
   * Creates a connection handler with given query handler,
   * but no write handler.
   *
   * {{{
   * import reactivemongo.api.MongoDriver
   * import acolyte.reactivemongo.AcolyteDSL.{ withDriver, handleQuery }
   * import acolyte.reactivemongo.Request
   *
   * withDriver(handleQuery { req: Request => aResponse }) { d =>
   *   val driver: MongoDriver = d // configured with given handler
   *   // work with driver (e.g. call you function using Mongo)
   *   "Value"
   * }
   * }}}
   *
   * @see [[ConnectionHandler.withWriteHandler]]
   */
  def handleQuery(handler: QueryHandler): ConnectionHandler =
    ConnectionHandler(handler)

  /**
   * Creates a connection handler with given write handler,
   * but no query handler.
   *
   * {{{
   * import reactivemongo.api.MongoDriver
   * import acolyte.reactivemongo.AcolyteDSL.{ withDriver, handleWrite }
   * import acolyte.reactivemongo.{ Request, WriteOp }
   *
   * withDriver(handleWrite { (op: WriteOp, req: Request) => aResponse }) { d =>
   *   val driver: MongoDriver = d // configured with given handler
   *   // work with driver (e.g. call you function using Mongo)
   *   "Value"
   * }
   * }}}
   *
   * @see [[ConnectionHandler.withQueryHandler]]
   */
  def handleWrite(handler: WriteHandler): ConnectionHandler =
    ConnectionHandler(writeHandler = handler)

}
