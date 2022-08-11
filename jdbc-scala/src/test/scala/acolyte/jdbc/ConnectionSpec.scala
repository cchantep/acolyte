package acolyte.jdbc

import acolyte.jdbc.ScalaCompositeHandler.{ empty => EmptyHandler }

object ConnectionSpec extends org.specs2.mutable.Specification {
  "Connection".title

  "Properties" should {
    "be empty" in {
      AcolyteDSL.connection(EmptyHandler).getProperties.isEmpty must beTrue
    }

    "be set" in {
      AcolyteDSL
        .connection(EmptyHandler, "_test" -> "_val")
        .getProperties
        .get("_test") aka "property" must_=== "_val"

    }
  }

  "Transaction" should {
    "handle commit" in {
      @volatile var commit = 0
      val con = AcolyteDSL.connection(
        EmptyHandler,
        AcolyteDSL.handleTransaction(whenCommit = { _ => commit += 1 })
      )

      con.commit() must not(throwA[Exception]) and {
        commit must_=== 1
      }
    }

    "handle rollback" in {
      val rollbacked = scala.concurrent.Promise[Connection]()

      val con = AcolyteDSL.connection(
        EmptyHandler,
        AcolyteDSL.handleTransaction(whenRollback = rollbacked.success)
      )

      con.rollback() must not(throwA[Exception]) and {
        rollbacked.isCompleted must beTrue
      }
    }
  }

  "Debug" should {
    "be successful" in {
      val output = Seq.newBuilder[String]

      AcolyteDSL.debuging(x => { output += x.toString; () }) { con =>
        val stmt = con.prepareStatement("SELECT * FROM Test WHERE id = ?")

        try {
          stmt.setString(1, "foo")
          stmt.executeQuery()
        } catch {
          case _: java.sql.SQLException => ()
        } finally {
          stmt.close()
        }
      }

      output.result() must_=== Seq(
        "QueryExecution(SELECT * FROM Test WHERE id = ?,List(Param(foo, VARCHAR)))"
      )
    }
  }
}
