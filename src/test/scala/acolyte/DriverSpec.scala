package acolyte

import java.util.ServiceLoader

import java.sql.{ Driver ⇒ JdbcDriver, DriverManager }

import scala.reflect.ClassTag

import org.specs2.mutable.Specification

object DriverSpec extends Specification with DriverUtils with DriverFixtures {
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
      driver.acceptsURL(jdbcUrl) must beTrue
    }

    "return null connection for unsupported URL" in {
      directConnect("https://github.com/cchantep/acolyte/").
        aka("JDBC connection") must beNull

    }

    "not be JDBC compliant" in {
      driver.jdbcCompliant aka "compliance" must beFalse
    }

    "support no meta-property" in {
      driver.getPropertyInfo(jdbcUrl, null).
        aka("meta-properties") mustEqual noMetaProps

    }

    "not open connection without handler" in {
      directConnect(jdbcUrl).
        aka("connection") must throwA[java.lang.IllegalStateException](
          message = "No connection handler")

    }

    "successfully return connection for valid information" in {
      directConnect(
        url = jdbcUrl,
        props = null,
        handler = "handler") aka "connection" must not beNull
    }
  }
}

sealed trait DriverFixtures {
  val jdbcUrl = "jdbc:acolyte:test"
  val noMetaProps = Array[java.sql.DriverPropertyInfo]()
}

sealed trait DriverUtils {
  import java.util.{ Properties ⇒ JProps }
  import scala.collection.JavaConversions

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

  def driver = new acolyte.Driver()

  def directConnect(url: String, props: Map[String, String] = Map(), handler: Any = null) = {
    val properties = new JProps()
    val d = driver

    d.setHandler(handler)

    d.connect(url, Option(props) match {
      case Some(map) ⇒
        properties.putAll(JavaConversions mapAsJavaMap props)
        properties
      case _ ⇒ null
    })
  }
}
