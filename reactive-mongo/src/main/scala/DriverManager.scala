package acolyte.reactivemongo

import reactivemongo.api.{ DB, MongoConnection, MongoDriver }

/** Driver manager */
trait DriverManager {
  /** Initializes driver. */
  def open(): MongoDriver

  /** Releases driver if necessary. */
  def releaseIfNecessary(driver: MongoDriver): Boolean
}

/** Driver manage companion. */
object DriverManager {
  import scala.concurrent.duration._

  private class Default(timeout: FiniteDuration) extends DriverManager {
    def open() = MongoDriver()

    /** Releases driver if necessary. */
    def releaseIfNecessary(driver: MongoDriver): Boolean = try {
      driver.close(timeout)
      true
    } catch {
      case e: Throwable ⇒
        e.printStackTrace()
        false
    }

    override lazy val toString = s"DriverManager(timeout = $timeout)"
  }

  implicit val Default: DriverManager = new Default(2.seconds)

  def withTimeout(timeout: FiniteDuration): DriverManager =
    new Default(timeout)

  def identity(existing: MongoDriver): DriverManager = new DriverManager {
    def open() = existing
    def releaseIfNecessary(driver: MongoDriver): Boolean = false
  }
}

/** Connection manager */
trait ConnectionManager[T] {
  /** Initializes connection. */
  def open(driver: MongoDriver, param: T): MongoConnection

  /** Releases connection if necessary. */
  def releaseIfNecessary(connection: MongoConnection): Boolean
}

/** Connection manage companion. */
object ConnectionManager {
  import akka.actor.Props
  import reactivemongo.api.MongoConnectionOptions

  /** Manager instance based on connection handler. */
  implicit object HandlerConnectionManager
      extends ConnectionManager[ConnectionHandler] {

    def open(driver: MongoDriver, handler: ConnectionHandler) = {
      val sys = driver.system
      val actorRef = sys.actorOf(Props(classOf[Actor], handler))
      new MongoConnection(sys, actorRef, MongoConnectionOptions())
    }

    /** Releases connection if necessary. */
    def releaseIfNecessary(connection: MongoConnection): Boolean = try {
      connection.close()
      true
    } catch {
      case e: Throwable ⇒
        e.printStackTrace()
        false
    }
  }

  /** Manager instance based on already initialized connection. */
  implicit object IdentityConnectionManager extends ConnectionManager[MongoConnection] {
    def open(driver: MongoDriver, connection: MongoConnection) = connection

    /** Releases connection if necessary. */
    def releaseIfNecessary(connection: MongoConnection): Boolean = false
  }
}
