package acolyte

import acolyte.{ ExecutedParameter ⇒ XP }

object ExecutionSpec extends org.specs2.mutable.Specification {
  "Execution" title

  "Query execution" >> {
    val (q1, q2) = (
      QueryExecution("SELECT * FROM Test WHERE id = ?", XP("x") :: Nil),
      QueryExecution("EXEC reindex"))

    "Query #1" should {
      "match case class pattern" in {
        q1 aka "q1" must beLike {
          case QueryExecution("SELECT * FROM Test WHERE id = ?",
            XP("x") :: Nil) ⇒ ok
        }
      }

      "not match case class pattern" in {
        q1 aka "q1" must not(beLike {
          case QueryExecution("EXEC reindex", Nil) ⇒ ok
        })
      }

      "match statement extractor #1" in {
        q1 aka "q1" must beLike {
          case ~(ExecutedStatement("FROM Test"), (sql, XP("x") :: Nil)) ⇒
            sql aka "matching SQL" must_== "SELECT * FROM Test WHERE id = ?"
        }
      }

      "match statement extractor #2" in {
        q1 aka "q1" must beLike {
          case ~(ExecutedStatement("^SELECT"), (sql, _)) ⇒ ok
        }
      }

      "not match statement extractor" in {
        q1 aka "q1" must not(beLike {
          case ~(ExecutedStatement(" reindex$"), (_, XP("x") :: Nil)) ⇒ ok
        })
      }
    }

    "Query #2" should {
      "match case class pattern" in {
        q2 aka "q2" must beLike {
          case QueryExecution("EXEC reindex", Nil) ⇒ ok
        }
      }

      "not match case class pattern" in {
        q2 aka "q2" must not(beLike {
          case QueryExecution("SELECT * FROM Test WHERE id = ?",
            XP("x") :: Nil) ⇒ ok
        })
      }

      "match statement extractor #1" in {
        q2 aka "q2" must beLike {
          case ~(ExecutedStatement("EXEC"), (sql, Nil)) ⇒
            sql aka "matching SQL" must_== "EXEC reindex"
        }
      }

      "match statement extractor #2" in {
        q2 aka "q2" must beLike {
          case ~(ExecutedStatement(" reindex$"), (sql, _)) ⇒ ok
        }
      }

      "not match statement extractor" in {
        q2 aka "q2" must not(beLike {
          case ~(ExecutedStatement("^SELECT"), (_, Nil)) ⇒ ok
        })
      }
    }
  }
}
