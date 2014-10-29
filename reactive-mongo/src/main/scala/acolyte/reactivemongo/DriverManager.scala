package acolyte.reactivemongo

import reactivemongo.api.{ DB, MongoConnection, MongoDriver }

/** Driver manager */
trait DriverManager {
  /** Initializes driver. */
  def open: MongoDriver

  /** Releases driver if necessary. */
  def releaseIfNecessary(driver: MongoDriver): Boolean
}

/** Driver manage companion. */
object DriverManager {
  import akka.actor.ActorSystem

  /** Manager instance based on connection handler. */
  implicit object Default extends DriverManager {
    def open = new MongoDriver(Some(Akka.actorSystem()))

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
  import reactivemongo.core.actors.MonitorActor
  import reactivemongo.api.MongoConnectionOptions

  /** Manager instance based on connection handler. */
  implicit object HandlerConnectionManager
      extends ConnectionManager[ConnectionHandler] {

    def open(driver: MongoDriver, handler: ConnectionHandler) = {
      val sys = driver.system
      val actor = sys.actorOf(Props(classOf[Actor], handler))
      val monitor = sys.actorOf(Props(new MonitorActor(actor)))
      new MongoConnection(sys, actor, monitor, MongoConnectionOptions())
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
