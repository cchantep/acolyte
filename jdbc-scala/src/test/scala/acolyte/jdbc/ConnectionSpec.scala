package acolyte.jdbc

import acolyte.jdbc.ScalaCompositeHandler.{ empty ⇒ EmptyHandler }

object ConnectionSpec extends org.specs2.mutable.Specification {
  "Connection" title

  "Properties" should {
    "be empty" in {
      AcolyteDSL.connection(EmptyHandler).getProperties.isEmpty must beTrue
    }

    "be set" in {
      AcolyteDSL.connection(EmptyHandler, "_test" → "_val").
        getProperties.get("_test") aka "property" mustEqual "_val"

    }
  }

  "Debug" should {
    "be successful" in {
      val output = Seq.newBuilder[String]

      AcolyteDSL.debuging(x ⇒ { output += x.toString; () }) { con ⇒
        val stmt = con.prepareStatement("SELECT * FROM Test WHERE id = ?")

        try {
          stmt.setString(1, "foo")
          stmt.executeQuery()
        } catch {
          case _: java.sql.SQLException ⇒ ()
        } finally {
          stmt.close()
        }
      }

      output.result() must_== Seq("QueryExecution(SELECT * FROM Test WHERE id = ?,List(Param(foo, VARCHAR)))")
    }
  }
}
