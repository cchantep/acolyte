package acolyte

import java.util.ServiceLoader

import java.sql.{ Driver ⇒ JdbcDriver, DriverManager }

import scala.reflect.ClassTag

import org.specs2.mutable.Specification

import acolyte.test.EmptyConnectionHandler

import org.apache.commons.lang3.reflect.TypeUtils

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

    "accept connection properties" in {
      val props = new java.util.Properties()
      props.put("_test", "_val")

      acolyte.Driver.register(handlerId, defaultHandler)

      (new acolyte.Driver().connect(jdbcUrl, props).getProperties.
        aka("connection 1 properties") mustEqual props).
        and(acolyte.Driver.connection(defaultHandler, props).
          getProperties aka "connection 2 properties" mustEqual props).
        and(acolyte.Driver.connection(CompositeHandler.empty, props).
          getProperties aka "connection 3 properties" mustEqual props)
    }

    "not open connection without handler" in {
      (directConnect("jdbc:acolyte:test").
        aka("connection") must throwA[IllegalArgumentException](
          message = "Invalid handler ID: null")).
          and(acolyte.Driver.connection(null.asInstanceOf[ConnectionHandler]).
            aka("direct connection 1") must throwA[IllegalArgumentException]).
          and(acolyte.Driver.connection(null.asInstanceOf[StatementHandler]).
            aka("direct connection 2") must throwA[IllegalArgumentException]).
          and(acolyte.Driver.
            connection(null.asInstanceOf[ConnectionHandler], null).
            aka("direct connection 3") must throwA[IllegalArgumentException]).
          and(acolyte.Driver.
            connection(null.asInstanceOf[StatementHandler], null).
            aka("direct connection 4") must throwA[IllegalArgumentException])

    }

    "not open connection with invalid handler" in {
      directConnect("jdbc:acolyte:test?handler=test").
        aka("connection") must throwA[IllegalArgumentException](
          message = "No matching handler: test")

    }

    "successfully return connection for valid information" in {
      acolyte.Driver.register(handlerId, defaultHandler)

      directConnect(
        url = jdbcUrl,
        props = null,
        handler = defaultHandler) aka "connection" must not beNull
    }
  }

  "Handler registry" should {
    "refuse null handler" in {
      (acolyte.Driver.register("id", null.asInstanceOf[ConnectionHandler]).
        aka("factory") must throwA[IllegalArgumentException]).
        and(acolyte.Driver.register("id", null.asInstanceOf[StatementHandler]).
          aka("factory") must throwA[IllegalArgumentException])

    }

    "manage successfully" in {
      val h = new CompositeHandler()
      acolyte.Driver.register("id", h)

      acolyte.Driver.unregister("id").
        getStatementHandler aka "handler" mustEqual h

    }
  }
}

sealed trait DriverFixtures {
  lazy val handlerId = s"test-${System.identityHashCode(this)}"
  lazy val jdbcUrl = "jdbc:acolyte:test?handler=%s" format handlerId
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

  val defaultHandler = EmptyConnectionHandler

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

  def directConnect(url: String, props: Map[String, String] = Map(), handler: ConnectionHandler = null) = {
    val properties = new JProps()
    val d = driver

    if (handler != null) {
      properties.put("connection.handler", handler)
    }

    d.connect(url, Option(props) match {
      case Some(map) ⇒
        properties.putAll(JavaConversions mapAsJavaMap props)
        properties
      case _ ⇒ properties
    })
  }
}
