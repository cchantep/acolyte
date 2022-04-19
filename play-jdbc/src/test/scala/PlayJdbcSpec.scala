package acolyte.jdbc.play

import anorm._

import play.api.db.Database

class PlaySpec extends org.specs2.mutable.Specification {
  "Acolyte/Play JDBC".title

  sequential

  "Anorm use case #1" should {
    val runner1 = PlayJdbcUseCases.useCase1

    "return a DB related string" in runner1 { (db: Database) =>
      db.withConnection { implicit con =>
        SQL"SELECT test".as(SqlParser.scalar[String].single) must_== "foo"
      }
    }
  }

  "Anorm use case #2" should {
    @volatile var count = 0
    val runner2 = PlayJdbcUseCases.useCase2 { count = count + 1 }

    "should increment count on commit" in runner2 { (db: Database) =>
      db.withConnection { implicit con =>
        con.setAutoCommit(false)
        SQL"insert into foo values (1, 'foo value')"
          .executeInsert() must beSome(1) and {
          SQL"insert into bar values (1, 'bar value')"
            .executeInsert() must beSome(1)
        } and {
          con.commit() must not(throwA[Exception])
        } and {
          count must_=== 1
        }
      }
    }
  }

  "Anorm use case #3" should {
    @volatile var count = 1
    val runner3 = PlayJdbcUseCases.useCase3 { count = count - 1 }

    "should decrement count on rollback" in runner3 { (db: Database) =>
      db.withConnection { implicit con =>
        con.setAutoCommit(false)
        SQL"insert into foo values (1, 'foo value')"
          .executeInsert() must beSome(1) and {
          SQL"insert into bar values (1, 'bar value')"
            .executeInsert() must throwA[java.sql.SQLException](
            "Simulating on error."
          )
        } and {
          con.rollback() must not(throwA[Exception])
        } and {
          count must_=== 0
        }
      }
    }
  }
}
