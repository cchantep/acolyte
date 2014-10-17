import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{
  BSONArray,
  BSONDocument,
  BSONDocumentReader,
  BSONDocumentWriter,
  BSONValue
}
import reactivemongo.core.commands.{ Count, LastError }

object Persistence {
  implicit object UserInfoReader extends BSONDocumentReader[UserInfo] {
    def read(bson: BSONDocument): UserInfo =
      bson.getAs[String]("name").fold[UserInfo](
        sys error "Missing 'name' property")(
          UserInfo(_, bson.getAs[String]("description")))
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(u: User): BSONDocument = {
      val base = BSONDocument("name" -> u.name, "password" -> u.password)
      val extra = u.description.fold(BSONDocument()) {
        d ⇒ BSONDocument("description" -> d)
      }
      val roles = if (u.roles.isEmpty) BSONDocument()
      else BSONDocument("roles" -> BSONArray(u.roles))

      base ++ extra ++ roles
    }
  }

  /** Returns names and descriptions for all users. */
  def all(implicit c: BSONCollection, x: ExecutionContext): Future[List[UserInfo]] = c.find(BSONDocument(),
    BSONDocument("name" -> true, "description" -> true)).
    cursor[UserInfo].collect[List]()

  /**
   * Saves given `user`: creates it if doesn't already exist, or update it.
   * @return Information about saved user
   */
  def save(user: User)(implicit c: BSONCollection, x: ExecutionContext): Future[UserInfo] = {
    val selector = BSONDocument("name" -> user.name)
    for {
      exists ← c.find(selector).cursor[BSONDocument].collect[List]()
      _ ← exists.headOption.fold[Future[LastError]](
        c.save(user))(_ ⇒ c.update(selector, user))
    } yield UserInfo(user.name, user.description)
  }

  /** Returns count of user matching specified `role`. */
  def countRole(role: String)(implicit db: DB, x: ExecutionContext): Future[Int] = db.command(Count("users", Some(BSONDocument(
    "roles" -> BSONDocument("$in" -> BSONArray(role))))))
  // db.users.count({ "roles": { "$in": [role] } })
}

case class UserInfo(name: String, description: Option[String] = None)

case class User(
  name: String,
  password: String,
  description: Option[String],
  roles: List[String])
