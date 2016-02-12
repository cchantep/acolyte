package acolyte.jdbc.play

import play.api.db.Database
import anorm._

import acolyte.jdbc.Implicits._

class PlaySpec extends org.specs2.mutable.Specification {
  "Acolyte/Play JDBC" title

  sequential

  "Anorm use case #1" should {
    val runner1 = PlayJdbcDSL.withPlayDBResult("foo")

    "return a DB related string" in runner1 { db: Database ⇒
      db.withConnection { implicit con ⇒
        SQL"SELECT test".as(SqlParser.scalar[String].single) must_== "foo"
      }
    }
  }
}
