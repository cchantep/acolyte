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
  }
}

sealed trait ConnectionFixtures {
  val jdbcUrl = "jdbc:acolyte:test"

  def connection(url: String, props: java.util.Properties, handler: Any) =
    new acolyte.Connection(url, props, handler)
}
