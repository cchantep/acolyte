package acolyte.jdbc

import AcolyteDSL.handleStatement

object CompositeHandlerSpec extends org.specs2.mutable.Specification {
  "Composite statement handler (scala)" title

  "Query detection" should {
    "not match without any pattern" in {
      handleStatement isQuery "TEST" aka "detection" must beFalse
    }
  }
}
