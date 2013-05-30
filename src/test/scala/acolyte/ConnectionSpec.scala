package acolyte

import java.sql.{ SQLException, SQLFeatureNotSupportedException }

import org.specs2.mutable.Specification

object ConnectionSpec extends Specification with ConnectionFixtures {
  "Connection specification" title

  "Connection constructor" should {
    "not accept null URL" in {
      connection(url = null, props = null, handler = "handler").
        aka("connection") must throwA[IllegalArgumentException](
          message = "Invalid JDBC URL")
    }

    "not accept null handler" in {
      connection(url = jdbcUrl, props = null, handler = null).
        aka("connection") must throwA[IllegalArgumentException](
          message = "Invalid Acolyte handler")
    }

    "return not-null instance for valid information" in {
      Option(connection(url = jdbcUrl, props = null, handler = "handler")).
        aka("connection") must beSome.which { conn ⇒
          (conn.getAutoCommit aka "auto-commit" must beFalse).
            and(conn.isReadOnly aka "read-only" must beFalse).
            and(conn.isClosed aka "closed" must beFalse).
            and(conn.isValid(0) aka "validity" must beTrue).
            and(conn.getWarnings aka "warnings" must beNull).
            and(conn.getTransactionIsolation.aka("transaction isolation").
              mustEqual(java.sql.Connection.TRANSACTION_NONE)).
            and(conn.getTypeMap aka "type-map" mustEqual emptyTypeMap).
            and(conn.getClientInfo aka "client info" mustEqual emptyClientInfo)

        }
    }
  }

  "Type-map setter" should {
    "refuse null mapping" in {
      defaultCon.setTypeMap(null).
        aka("setter") must throwA[SQLException](
          message = "Invalid type-map")

    }
  }

  "Client info setter" should {
    "refuse null properties" in {
      defaultCon.setClientInfo(null).
        aka("setter") must throwA[java.lang.IllegalArgumentException]

    }
  }

  "Network timeout" should {
    "not be settable" in {
      defaultCon.setNetworkTimeout(null, 0).
        aka("timeout setting") must throwA[SQLFeatureNotSupportedException]

    }

    "not be readable" in {
      defaultCon.getNetworkTimeout.
        aka("getter") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Closed connection" should {
    "be marked" in {
      lazy val c = defaultCon
      c.close()

      c.isClosed aka "closed" must beTrue
    }

    "not be closed again" in {
      lazy val c = defaultCon
      c.close()

      c.close() aka "closing" must throwA[SQLException](
        message = "Connection is already closed")
    }

    "not be valid" in {
      lazy val c = defaultCon
      c.close()

      c.isValid(0) aka "validity" must beFalse
    }

    "not be rollbackable" in {
      lazy val c = defaultCon
      c.close()

      c.rollback() aka "rollback" must throwA[SQLException](
        message = "Connection is closed")

    }
  }

  "Rollback" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      (c.getAutoCommit aka "auto-commit" must beTrue).
        and(c.rollback() aka "rollback" must throwA[SQLException](
          message = "Auto-commit is enabled"))

    }
  }
}

sealed trait ConnectionFixtures {
  val jdbcUrl = "jdbc:acolyte:test"
  val emptyTypeMap = new java.util.HashMap[String, Class[_]]()
  val emptyClientInfo = new java.util.Properties()

  def defaultCon = connection(jdbcUrl, null, "handler")

  def connection(url: String, props: java.util.Properties, handler: Any) =
    new acolyte.Connection(url, props, handler)
}
