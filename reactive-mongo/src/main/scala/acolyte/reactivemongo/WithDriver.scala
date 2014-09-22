package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }

/** Functions to work with driver. */
trait WithDriver {
  /**
   * Works with Mongo driver configured with Acolyte handlers.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param driverParam Driver manager parameter (see [[DriverManager]])
   * @param f Function applied to initialized driver
   *
   * {{{
   * // handler: ConnectionHandler
   * val s: Future[String] = withDriver(handler) { d =>
   *   val initedDriver: MongoDriver = d
   *   "Result"
   * }
   *
   * val i: Future[Int] = withDriver(alreadyInitedDriver) { d =>
   *   val unchangedDriver: MongoDriver = d
   *   1 // Result
   * }
   * }}}
   * @see [[withFlatDriver]]
   */
  def withDriver[A, B](driverParam: ⇒ A)(f: MongoDriver ⇒ B)(implicit m: DriverManager[A], c: ExecutionContext): Future[B] = for {
    driver ← Future(m.open(driverParam))
    result ← {
      val res = Future(f(driver))
      res.onComplete(_ ⇒ m.releaseIfNecessary(driver))
      res
    }
  } yield result

  /**
   * Works with Mongo driver configured with Acolyte handlers.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param driverParam Driver manager parameter (see [[DriverManager]])
   * @param f Function applied to initialized driver (returning a future)
   *
   * {{{
   * // handler: ConnectionHandler
   * val s: Future[String] = withFlatDriver(handler) { d =>
   *   val initedDriver: MongoDriver = d
   *   Future.successful("Result")
   * }
   *
   * val i: Future[Int] = withFlatDriver(alreadyInitedDriver) { d =>
   *   val unchangedDriver: MongoDriver = d
   *   Future(1 + 2) // Result
   * }
   * }}}
   * @see [[withDriver]]
   */
  def withFlatDriver[A, B](driverParam: ⇒ A)(f: MongoDriver ⇒ Future[B])(implicit m: DriverManager[A], c: ExecutionContext): Future[B] = for {
    driver ← Future(m.open(driverParam))
    result ← {
      val res = f(driver)
      res.onComplete(_ ⇒ m.releaseIfNecessary(driver))
      res
    }
  } yield result

  /**
   * Works with connection with options appropriate for a driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Connection is closed after the result `Future` is completed.
   *
   * @param driverParam Driver manager parameter (see [[DriverManager]])
   * @param f Function applied to initialized connection
   *
   * {{{
   * import reactivemongo.api.MongoConnection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL.withConnection(handler) { con =>
   *   val c: MongoConnection = con
   *   "Result"
   * }
   * }}}
   * @see [[withFlatDriver]]
   * @see [[withFlatConnection]]
   */
  def withConnection[A, B](driverParam: ⇒ A)(f: MongoConnection ⇒ B)(implicit m: DriverManager[A], c: ExecutionContext): Future[B] =
    withFlatDriver(m.open(driverParam)) { d ⇒
      for {
        con ← Future(d.connection(List("acolyte")))
        res ← {
          val result = Future(f(con))
          result.onComplete(_ ⇒ con.close())
          result
        }
      } yield res
    }

  /**
   * Works with connection with options appropriate for a driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   *
   * @param driverParam Driver manager parameter (see [[DriverManager]])
   * @param f Function applied to initialized connection
   *
   * {{{
   * import reactivemongo.api.MongoConnection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL.withFlatConnection(handler) { con =>
   *   val c: MongoConnection = con
   *   Future.successful("Result")
   * }
   * }}}
   * @see [[withFlatDriver]]
   * @see [[withConnection]]
   */
  def withFlatConnection[A, B](driverParam: ⇒ A)(f: MongoConnection ⇒ Future[B])(implicit m: DriverManager[A], c: ExecutionContext): Future[B] =
    withFlatDriver(driverParam) { d ⇒
      for {
        con ← Future(d.connection(List("acolyte")))
        res ← {
          val result = f(con)
          result.onComplete(_ ⇒ con.close())
          result
        }
      } yield res
    }
}
