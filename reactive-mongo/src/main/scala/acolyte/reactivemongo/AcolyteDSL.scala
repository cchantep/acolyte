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
   * Creates an empty handler.
   *
   * {{{
   * import acolyte.reactivemongo.AcolyteDSL.{ connection, handleStatement }
   *
   * connection { handleStatement }
   * }}}
   */
  def handleStatement: ConnectionHandler = ???
}
