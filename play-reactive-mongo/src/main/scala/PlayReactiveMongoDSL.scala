package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }
import play.modules.reactivemongo.ReactiveMongoApi

object PlayReactiveMongoDSL {
  /**
   * @param drv the MongoDB driver
   * @param con the MongoDB connection
   */
  def mongoApi(drv: MongoDriver, con: MongoConnection)(implicit ec: ExecutionContext): ReactiveMongoApi = new ReactiveMongoApi {
    import reactivemongo.api._, gridfs._
    import reactivemongo.play.json._
    import reactivemongo.play.json.collection._

    val driver = drv
    val connection = con

    def database = connection.database("acolyte")

    def asyncGridFS: Future[GridFS[JSONSerializationPack.type]] =
      database.map(GridFS[JSONSerializationPack.type](_))

    def db: DefaultDB = connection.db("acolyte")

    def gridFS = GridFS[JSONSerializationPack.type](db)
  }
}
