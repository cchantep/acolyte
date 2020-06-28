package acolyte.reactivemongo

import scala.concurrent.{ Await, ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, AsyncDriver }
import play.modules.reactivemongo.ReactiveMongoApi

object PlayReactiveMongoDSL {
  /**
   * @param drv the MongoDB driver
   * @param con the MongoDB connection
   */
  def mongoApi(drv: AsyncDriver, con: MongoConnection)(implicit ec: ExecutionContext): ReactiveMongoApi = new AcolyteMongoApi(drv, con)
}

private[reactivemongo] final class AcolyteMongoApi(
    val asyncDriver: AsyncDriver,
    val connection: MongoConnection)(implicit ec: ExecutionContext) extends ReactiveMongoApi {
  import reactivemongo.api._, gridfs._

  def database: Future[DB] = connection.database("acolyte")

  def asyncGridFS = database.map(_.gridfs)
}

