package acolyte.reactivemongo

import reactivemongo.api.{ DB, MongoDriver }

/** Driver manager */
trait DriverManager[T] {
  /** Initializes driver. */
  def open(param: T): MongoDriver

  /** Releases driver if necessary. */
  def releaseIfNecessary(driver: MongoDriver): Boolean
}

/** Driver manage companion. */
object DriverManager {

  /** Manager instance based on connection handler. */
  implicit object HandlerDriverManager
      extends DriverManager[ConnectionHandler] {

    def open(handler: ConnectionHandler) =
      new MongoDriver(Some(Akka actorSystem handler))

    /** Releases driver if necessary. */
    def releaseIfNecessary(driver: MongoDriver): Boolean = try {
      driver.close()
      true
    } catch {
      case e: Throwable ⇒
        e.printStackTrace()
        false
    }
  }

  /** Manager instance based on already initialized driver. */
  implicit object IdentityDriverManager extends DriverManager[MongoDriver] {
    def open(driver: MongoDriver) = driver

    /** Releases driver if necessary. */
    def releaseIfNecessary(driver: MongoDriver): Boolean = false
  }
}
