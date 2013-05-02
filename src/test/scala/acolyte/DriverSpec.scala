package acolyte

import java.util.ServiceLoader

import java.sql.{ Driver ⇒ JdbcDriver, DriverManager }

import scala.reflect.ClassTag

import org.specs2.mutable.Specification

object DriverSpec extends Specification with DriverUtils {
  "Acolyte driver" title

  "Driver class" should {
    "be assignable as java.sql.Driver" in {
      classOf[JdbcDriver].
        aka("JDBC interface") must beAssignableFrom[acolyte.Driver]

    }

    "be auto-registered using SPI mechanism" in {
      isRegistered[acolyte.Driver] aka "SPI registration" must beTrue
    }
  }

  "Driver manager" should {
    s"return Acolyte driver for $jdbcUrl" in {
      DriverManager.getDriver(jdbcUrl).
        aka("JDBC driver") must haveClass[acolyte.Driver]

    }
  }

  "Driver" should {
    "accept valid JDBC URL" in {
      new acolyte.Driver().acceptsURL(jdbcUrl) must beTrue
    }
  }

  val jdbcUrl = "jdbc:acolyte:test"
}

sealed trait DriverUtils {
  private val driversLoaded = {
    // Workaround for SBT-like classloaders
    val en = DriverManager.getDrivers
    while (en.hasMoreElements) en.nextElement
    true
  }

  def isRegistered[T: ClassTag]: Boolean = {
    val driverClass = implicitly[ClassTag[T]].runtimeClass

    @annotation.tailrec
    def look(it: java.util.Iterator[JdbcDriver], found: Boolean): Boolean = {
      if (!it.hasNext) found
      else {
        if (driverClass equals it.next.getClass) true
        else look(it, found)
      }
    }

    val driverSpi: ServiceLoader[JdbcDriver] =
      ServiceLoader.load(classOf[JdbcDriver])

    look(driverSpi.iterator, false)
  }
}

