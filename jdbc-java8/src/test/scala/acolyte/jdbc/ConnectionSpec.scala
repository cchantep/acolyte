package acolyte.jdbc

import acolyte.jdbc.AcolyteDSL.{ handleStatement, prop }

object ConnectionSpec extends org.specs2.mutable.Specification {
  "Connection".title

  "Properties" should {
    "be empty" in {
      AcolyteDSL.connection(handleStatement).getProperties.isEmpty must beTrue
    }

    "be set" in {
      AcolyteDSL
        .connection(handleStatement, prop("_test", "_val"))
        .getProperties
        .get("_test") aka "property" must_=== "_val"

    }
  }
}
