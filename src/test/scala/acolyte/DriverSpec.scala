package acolyte

import java.util.ServiceLoader

import java.sql.{ Driver ⇒ JdbcDriver, DriverManager }

import scala.reflect.ClassTag

import org.specs2.mutable.Specification

import acolyte.test.EmptyConnectionHandler

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

    "not open connection without properties" in {
      driver.connect(jdbcUrl, null).
        aka("connect") must throwA[IllegalArgumentException](
          message = "Invalid properties")

    }

    "not open connection without handler" in {
      directConnect(jdbcUrl).
        aka("connection") must throwA[IllegalArgumentException](
          message = "Invalid properties")

    }

    "not open connection with invalid handler" in {
      lazy val props = {
        val p = new java.util.Properties()
        p.put("connection.handler", "test")
        p
      }

      driver.connect(jdbcUrl, props).
        aka("connection") must throwA[IllegalArgumentException](
          message = "Invalid handler: ")

    }

    "successfully return connection for valid information" in {
      directConnect(
        url = jdbcUrl,
        props = null,
        handler = defaultHandler) aka "connection" must not beNull
    }
  }

  "Properties factory" should {
    "refuse null handler" in {
      (acolyte.Driver.properties(null.asInstanceOf[ConnectionHandler]).
        aka("factory") must throwA[IllegalArgumentException]).
        and(acolyte.Driver.properties(null.asInstanceOf[StatementHandler]).
          aka("factory") must throwA[IllegalArgumentException])

    }

    "create expected instance from connection handler" in {
      lazy val props = acolyte.Driver.properties(defaultHandler)

      (props.size aka "size" mustEqual 1).
        and(props.get("connection.handler").
          aka("handler") mustEqual defaultHandler)
    }

    "create expected instance from statement handler" in {
      val h = new RuleStatementHandler()
      lazy val props = acolyte.Driver.properties(h)

      (props.size aka "size" mustEqual 1).
        and(props.get("connection.handler").asInstanceOf[ConnectionHandler].
          getStatementHandler aka "handler" mustEqual h)
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
