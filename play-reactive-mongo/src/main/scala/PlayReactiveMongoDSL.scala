package acolyte.reactivemongo

import scala.concurrent.{ Await, ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }
import play.modules.reactivemongo.ReactiveMongoApi

object PlayReactiveMongoDSL {
  /**
   * @param drv the MongoDB driver
   * @param con the MongoDB connection
   */
  def mongoApi(drv: MongoDriver, con: MongoConnection)(implicit ec: ExecutionContext): ReactiveMongoApi = new AcolyteMongoApi(drv, con)
}

private[reactivemongo] final class AcolyteMongoApi(
    val driver: MongoDriver,
    val connection: MongoConnection)(implicit ec: ExecutionContext) extends ReactiveMongoApi {
  import scala.concurrent.duration._
  import reactivemongo.api._, gridfs._
  import reactivemongo.play.json._
  import reactivemongo.play.json.collection._

  def database = connection.database("acolyte")

  def gridFS: GridFS[JSONSerializationPack.type] =
    GridFS[JSONSerializationPack.type](
      _pack = JSONSerializationPack,
      db = Await.result(database, 10.seconds),
      prefix = "fs")

  def asyncGridFS: Future[GridFS[JSONSerializationPack.type]] =
    database.map { _db =>
      GridFS[JSONSerializationPack.type](
        _pack = JSONSerializationPack,
        db = _db,
        prefix = "fs")
    }
}

