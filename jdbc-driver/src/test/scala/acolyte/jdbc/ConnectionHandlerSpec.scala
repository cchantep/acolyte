package acolyte.jdbc

import org.specs2.mutable.Specification

object ConnectionHandlerSpec extends Specification {
  "Connection handler".title

  "Default handler" should {
    "refuse null statement handler" in {
      new ConnectionHandler.Default(null)
        .aka("ctor") must throwA[IllegalArgumentException]("Statement handler")

    }

    "refuse null resource handler" in {
      new ConnectionHandler.Default(null, null)
        .aka("ctor") must throwA[IllegalArgumentException]

    }

    "update the resource handler" in {
      val conHandler1 =
        new ConnectionHandler.Default(test.EmptyStatementHandler)

      val resHandler2 = new ResourceHandler.Default()
      val conHandler2 = conHandler1.withResourceHandler(resHandler2)

      conHandler1.hashCode must not(beEqualTo(conHandler2.hashCode)) and {
        conHandler2.getResourceHandler.hashCode must not(
          beEqualTo(conHandler1.getResourceHandler.hashCode)
        )
      }
    }
  }
}
