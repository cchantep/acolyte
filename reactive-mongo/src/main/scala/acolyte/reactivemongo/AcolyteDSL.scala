package acolyte.reactivemongo

import reactivemongo.api.MongoDriver
import reactivemongo.bson.BSONDocument

/**
 * Acolyte DSL for ReactiveMongo.
 */
object AcolyteDSL {

  /**
   * Returns Mongo driver configured with Acolyte handlers.
   *
   * @param param handler Connection handler
   */
  def driver(handler: ConnectionHandler): MongoDriver =
    new MongoDriver(Some(Akka.actorSystem(handler)))

  /**
   * Creates an empty connection handler.
   *
   * {{{
   * import acolyte.reactivemongo.AcolyteDSL.{ driver, handle }
   *
   * driver(handle)
   * }}}
   */
  def handle: ConnectionHandler = ConnectionHandler.empty

  /**
   * Creates a connection handler with given query handler,
   * but no write handler.
   *
   * {{{
   * import acolyte.reactivemongo.AcolyteDSL.{ driver, handleQuery }
   * import acolyte.reactivemongo.Request
   *
   * driver(handleQuery { req: Request => aResponse })
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
   * import acolyte.reactivemongo.AcolyteDSL.{ driver, handleWrite }
   * import acolyte.reactivemongo.{ Request, WriteOp }
   *
   * driver(handleWrite { (op: WriteOp, req: Request) => aResponse })
   * }}}
   *
   * @see [[ConnectionHandler.withQueryHandler]]
   */
  def handleWrite(handler: WriteHandler): ConnectionHandler =
    ConnectionHandler(writeHandler = handler)
}
