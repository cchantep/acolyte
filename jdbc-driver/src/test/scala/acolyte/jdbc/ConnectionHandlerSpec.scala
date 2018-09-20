package acolyte.jdbc

import org.specs2.mutable.Specification

object ConnectionHandlerSpec extends Specification {
  "Connection handler" title

  "Default handler" should {
    "refuse null statement handler" in {
      new ConnectionHandler.Default(null).
        aka("ctor") must throwA[IllegalArgumentException]("Statement handler")

    }

    "refuse null resource handler" in {
      new ConnectionHandler.Default(null, null).
        aka("ctor") must throwA[IllegalArgumentException]

    }
  }
}
