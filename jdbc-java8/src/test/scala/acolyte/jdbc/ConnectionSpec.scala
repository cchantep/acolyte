package acolyte.jdbc

import acolyte.jdbc.AcolyteDSL.handleStatement

object ConnectionSpec extends org.specs2.mutable.Specification {
  "Connection" title

  "Properties" should {
    "be empty" in {
      AcolyteDSL.connection(handleStatement).getProperties.isEmpty must beTrue
    }

    "be set" in {
      val props = new java.util.Properties()
      props.put("_test", "_val")
      
      AcolyteDSL.connection(handleStatement, props).
        getProperties.get("_test") aka "property" mustEqual "_val"

    }
  }
}
