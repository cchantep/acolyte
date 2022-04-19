package acolyte.jdbc

import java.sql.SQLException

import acolyte.jdbc.{ ExecutedParameter => XP }

object ExecutionSpec extends org.specs2.mutable.Specification {
  "Execution".title

  "Query execution" >> {
    val (q1, q2) = (
      QueryExecution("SELECT * FROM Test WHERE id = ?", XP("x") :: Nil),
      QueryExecution("EXEC reindex")
    )

    "Query #1" should {
      "match case class pattern" in {
        q1 aka "q1" must beLike {
          case QueryExecution(
                "SELECT * FROM Test WHERE id = ?",
                XP("x") :: Nil
              ) =>
            ok
        }
      }

      "not match case class pattern" in {
        q1 aka "q1" must not(beLike {
          case QueryExecution("EXEC reindex", Nil) => ok
        })
      }

      "match statement extractor #1" in {
        val Executed = ExecutedStatement("FROM Test")

        q1 aka "q1" must beLike {
          case Executed((sql, XP("x") :: Nil)) =>
            sql aka "matching SQL" must_=== "SELECT * FROM Test WHERE id = ?"
        }
      }

      "match statement extractor #2" in {
        val Executed = ExecutedStatement("^SELECT")

        q1 aka "q1" must beLike { case Executed((_ /*sql*/, _)) => ok }
      }

      "not match statement extractor" in {
        val Executed = ExecutedStatement(" reindex$")

        q1 aka "q1" must not(beLike { case Executed((_, XP("x") :: Nil)) => ok })
      }
    }

    "Query #2" should {
      "match case class pattern" in {
        q2 aka "q2" must beLike {
          case QueryExecution("EXEC reindex", Nil) => ok
        }
      }

      "not match case class pattern" in {
        q2 aka "q2" must not(beLike {
          case QueryExecution(
                "SELECT * FROM Test WHERE id = ?",
                XP("x") :: Nil
              ) =>
            ok
        })
      }

      "match statement extractor #1" in {
        val Executed = ExecutedStatement("EXEC")

        q2 aka "q2" must beLike {
          case Executed((sql, Nil)) =>
            sql aka "matching SQL" must_=== "EXEC reindex"
        }
      }

      "match statement extractor #2" in {
        val Executed = ExecutedStatement(" reindex$")

        q2 aka "q2" must beLike { case Executed((_ /*sql*/, _)) => ok }
      }

      "not match statement extractor" in {
        val Executed = ExecutedStatement("^SELECT")

        q2 aka "q2" must not(beLike { case Executed((_, Nil)) => ok })
      }
    }
  }

  "Update execution" should {
    "represent DB error" in {
      val handler = AcolyteDSL.handleStatement.withUpdateHandler { _ =>
        throw new SQLException("Foo bar lorem")
      }

      val con = AcolyteDSL.connection(handler)
      val stmt = con.prepareStatement("UPDATE tbl SET nme = ? WHERE id = ?")

      stmt.setString(1, "test")
      stmt.setString(2, "this")

      stmt.executeUpdate() must throwA[SQLException]("Foo bar lorem")
    }
  }
}
