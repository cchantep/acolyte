package acolyte.reactivemongo

import scala.concurrent.ExecutionContext

import reactivemongo.api.AsyncDriver
import reactivemongo.acolyte.{ MongoConnection, ActorSystem }

/** Driver manager */
@annotation.implicitNotFound("Cannot find `acolyte.reactivemongo.DriverManager` (default one requires an `ExecutionContext`)")
trait DriverManager {
  /** Initializes driver. */
  def open(): AsyncDriver

  /** Releases driver if necessary. */
  def releaseIfNecessary(driver: AsyncDriver): Boolean
}

/** Driver manage companion. */
object DriverManager {
  import scala.concurrent.duration._

  private class Default(timeout: FiniteDuration)(implicit ec: ExecutionContext) extends DriverManager {
    def open() = AsyncDriver()

    /** Releases driver if necessary. */
    def releaseIfNecessary(driver: AsyncDriver): Boolean = try {
      driver.close(timeout)
      true
    } catch {
      case e: Throwable ⇒
        e.printStackTrace()
        false
    }

    override lazy val toString = s"DriverManager(timeout = $timeout)"
  }

  implicit def Default(implicit ec: ExecutionContext): DriverManager = new Default(5.seconds)

  def withTimeout(timeout: FiniteDuration)(implicit ec: ExecutionContext): DriverManager = new Default(timeout)

  def identity(existing: AsyncDriver): DriverManager = new DriverManager {
    def open() = existing

    def releaseIfNecessary(driver: AsyncDriver): Boolean = false

    override lazy val toString = "NoOpDriverManager"
  }
}

/** Connection manager */
trait ConnectionManager[T] {
  /** Initializes connection. */
  def open(driver: AsyncDriver, param: T): MongoConnection

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

    import scala.concurrent.Await
    import scala.concurrent.duration._

    def open(driver: AsyncDriver, handler: ConnectionHandler) = {
      val sys = ActorSystem(driver)
      val actorRef = sys.actorOf(Props(classOf[Actor], handler))

      MongoConnection(
        s"supervisor-${System identityHashCode actorRef}",
        s"connection-${System identityHashCode actorRef}",
        sys, actorRef, MongoConnectionOptions())
    }

    /** Releases connection if necessary. */
    def releaseIfNecessary(connection: MongoConnection): Boolean = try {
      Await.result(connection.close()(10.seconds), 10.seconds)
      true
    } catch {
      case e: Throwable ⇒
        e.printStackTrace()
        false
    }
  }

  /** Manager instance based on already initialized connection. */
  implicit object IdentityConnectionManager extends ConnectionManager[MongoConnection] {
    def open(driver: AsyncDriver, connection: MongoConnection) = connection

    /** Releases connection if necessary. */
    def releaseIfNecessary(connection: MongoConnection): Boolean = false
  }
}
