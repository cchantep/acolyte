import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument

case class UserInfo(name: String, description: Option[String] = None)

object Persistence {
  /** Returns names and descriptions for all users. */
  def all(implicit c: BSONCollection, x: ExecutionContext): Future[List[UserInfo]] = {
    @annotation.tailrec def parse(docs: List[BSONDocument], info: List[UserInfo]): Future[List[UserInfo]] = docs match {
      case d :: ds ⇒ d.getAs[String]("name") match {
        case Some(n) ⇒
          parse(ds, UserInfo(n, d.getAs[String]("description")) :: info)
        case _ ⇒ Future.failed(new RuntimeException("Missing 'name' property"))
      }
      case _ ⇒ Future.successful(info)
    }

    for {
      ls ← c.find(BSONDocument(),
        BSONDocument("name" -> true, "description" -> true)).
        cursor[BSONDocument].collect[List]()
      us ← parse(ls, Nil)
    } yield us
  }
}
