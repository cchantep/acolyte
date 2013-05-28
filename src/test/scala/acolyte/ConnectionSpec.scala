package acolyte

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
            and(conn.getWarnings aka "warnings" must beNull).
            and(conn.getTransactionIsolation.aka("transaction isolation").
              mustEqual(java.sql.Connection.TRANSACTION_NONE)).
            and(conn.getTypeMap aka "type-map" mustEqual emptyTypeMap)
        }
    }
  }

  "Type-map setter" should {
    "refuse null mapping" in {
      connection(jdbcUrl, null, "handler").setTypeMap(null).
        aka("setter") must throwA[java.sql.SQLException](
          message = "Invalid type-map")

    }
  }
}

sealed trait ConnectionFixtures {
  val jdbcUrl = "jdbc:acolyte:test"
  val emptyTypeMap = new java.util.HashMap[String, Class[_]]()

  def connection(url: String, props: java.util.Properties, handler: Any) =
    new acolyte.Connection(url, props, handler)
}
