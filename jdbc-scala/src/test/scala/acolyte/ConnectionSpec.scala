package acolyte

import acolyte.ScalaCompositeHandler.{ empty ⇒ EmptyHandler }

object ConnectionSpec extends org.specs2.mutable.Specification {
  "Connection" title

  "Properties" should {
    "be empty" in {
      Acolyte.connection(EmptyHandler).getProperties.isEmpty must beTrue
    }

    "be set" in {
      Acolyte.connection(EmptyHandler, "_test" -> "_val").
        getProperties.get("_test") aka "property" mustEqual "_val"

    }
  }
}
