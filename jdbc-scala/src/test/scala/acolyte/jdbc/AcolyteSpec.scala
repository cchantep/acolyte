package acolyte.jdbc

import java.sql.{ Date, SQLException, SQLWarning }

class AcolyteSpec extends org.specs2.mutable.Specification {
  "Acolyte".title

  sequential

  "Scala use case #1" should {
    val con = ScalaUseCases.useCase1

    "return 2 for DELETE statement" in {
      con
        .prepareStatement("DELETE * FROM table")
        .executeUpdate aka "update count" must_=== 2

    }

    "return 1 for other update statement" in {
      lazy val s =
        con.prepareStatement("INSERT INTO table('id', 'name') VALUES (?, ?)")

      s.setString(1, "idVal");
      s.setString(2, "idName")

      s.executeUpdate aka "update count" must_=== 1
    }

    "return empty resultset for SELECT query" in {
      con
        .createStatement()
        .executeQuery("SELECT * FROM table")
        .aka("resultset") must_=== RowLists.rowList1(classOf[String]).resultSet

    }

    "return resultset of 2 rows" >> {
      lazy val s = {
        val st = con.prepareStatement("EXEC that_proc(?)")
        st.setString(1, "test")
        st
      }
      lazy val rs = s.executeQuery

      "with expected 3 columns on first row" in {
        (rs.next aka "has first row" must beTrue)
          .and(rs.getString(1) aka "1st row/1st col" must_=== "str")
          .and(rs.getFloat(2) aka "1st row/2nd col" must_=== 1.2F)
          .and(rs.getDate(3) aka "1st row/2rd col" must_=== new Date(1L))

      }

      "with expected 3 columns on second row" in {
        (rs.next aka "has second row" must beTrue)
          .and(rs.getString(1) aka "2nd row/1st col" must_=== "val")
          .and(rs.getFloat(2) aka "2nd row/2nd col" must_=== 2.34F)
          .and(rs.getDate(3) aka "2nd row/2rd col" must beNull)

      }

      "no third row" in {
        rs.next aka "has third row" must beFalse
      }
    }
  }

  "Scala use case #2" should {
    val con = ScalaUseCases.useCase2

    "throw exception for update statement" in {
      con
        .prepareStatement("DELETE * FROM table")
        .executeUpdate aka "update" must throwA[SQLException](
        message = "No update handler"
      )

    }

    "return empty resultset for SELECT query" in {
      lazy val s = {
        val st = con.prepareStatement("SELECT * FROM table")
        st.setString(1, "test")
        st
      }
      lazy val rs = s.executeQuery

      "with expected 3 columns on first row" in {
        (rs.next aka "has first row" must beTrue)
          .and(
            rs.getString(1).aka("1st row/1st col (by index)") must_=== "text"
          )
          .and(rs.getFloat(2).aka("1st row/2nd col (by index)") must_=== 2.3F)
          .and(
            rs.getDate(3).aka("1st row/2rd col (by index)") must_=== new Date(
              3L
            )
          )
          .and(
            rs.getString("str")
              .aka("1st row/1st col (by label)") must_=== "text"
          )
          .and(rs.getFloat("f").aka("1st row/2nd col (by label)") must_=== 2.3F)
          .and(
            rs.getDate("date")
              .aka("1st row/2rd col (by label)") must_=== new Date(3L)
          )

      }

      "with expected 3 columns on second row" in {
        (rs.next aka "has second row" must beTrue)
          .and(
            rs.getString(1).aka("2nd row/1st col (by index)") must_=== "label"
          )
          .and(rs.getFloat(2).aka("2nd row/2nd col (by index)") must_=== 4.56F)
          .and(
            rs.getDate(3).aka("2nd row/2rd col (by index)") must_=== new Date(
              4L
            )
          )
          .and(
            rs.getString("str")
              .aka("2nd row/1st col (by label)") must_=== "label"
          )
          .and(
            rs.getFloat("f").aka("2nd row/2nd col (by label)") must_=== 4.56F
          )
          .and(
            rs.getDate("date")
              .aka("2nd row/2rd col (by label)") must_=== new Date(4L)
          )

      }

      "no third row" in {
        rs.next aka "has third row" must beFalse
      }
    }
  }

  "Scala use case #3" should {
    val con = ScalaUseCases.useCase3

    "return expected result with 1 parameter" in {
      lazy val s = con.prepareStatement("SELECT * FROM table WHERE id = ?")
      s.setString(1, "id")

      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue)
        .and(rs.getString(1) aka "str(1)" must_=== "useCase_3a")

    }

    "return expected result with 2 parameters" in {
      lazy val s =
        con.prepareStatement("SELECT * FROM table WHERE id = ? AND type = ?")
      s.setString(1, "id")
      s.setInt(2, 3)

      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue)
        .and(rs.getString(1) aka "str(1)" must_=== "useCase_3str")
        .and(rs.getInt(2) aka "int(2)" must_=== 2)
        .and(rs.getLong(3) aka "long(3)" must_=== 3)

    }

    "raise warning" in {
      lazy val s = con.prepareStatement("SELECT dummy")
      s.executeQuery

      Option(s.getWarnings) aka "warning" must beSome[SQLWarning].which {
        _.getMessage aka "message" must_=== "Now you're warned"
      }
    }
  }

  "Scala use case #4" should {
    val con = ScalaUseCases.useCase4

    "return expected boolean result" in {
      lazy val s = con.prepareStatement("SELECT * FROM table")
      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue)
        .and(rs.getBoolean(1) aka "single column" must beTrue)

    }
  }

  "Single-case query context" should {
    def query(sql: String, c: java.sql.Connection): java.sql.ResultSet = {
      val rs = c.prepareStatement(sql).executeQuery
      rs.next(); rs
    }

    lazy val res1: QueryResult =
      RowLists
        .rowList2(classOf[String], classOf[Float])
        .append("test", 3.45F)
        .asResult

    "always return provided result" >> {
      "for SELECT" in {
        val str: String = AcolyteDSL.withQueryResult(res1) { c =>
          val rs = query("SELECT * FROM table", c)
          s"${rs.getString(1)} -> ${rs.getFloat(2) + 1F}"
        }

        str aka "from query result" must_=== "test -> 4.45"
      }

      "for EXEC" in AcolyteDSL.withQueryResult(res1) { c =>
        query("EXEC proc", c) aka "proc result" must beLike {
          case rs =>
            (rs.getString(1) aka "col #1" must_=== "test")
              .and(rs.getFloat(2) aka "col #2" must_=== 3.45F)
        }
      }
    }
  }

  "Scala use case #5" should {
    val con = ScalaUseCases.useCase5

    "return expected boolean result" in {
      lazy val s = con.prepareStatement("INSERT INTO table(x) VALUE('y')")
      s.executeUpdate()

      val keys = s.getGeneratedKeys

      (keys.next aka "has generated key" must beTrue)
        .and(keys.getInt(1) aka "first key" must_=== 100)
        .and(keys.next aka "has second key" must beFalse)

    }
  }

  "Update result" should {
    "have generated keys" in {
      AcolyteDSL
        .updateResult(2, RowLists.intList(3, 4))
        .aka("update result") must beLike {
        case res =>
          (res.getUpdateCount aka "count" must_=== 2)
            .and(res.getGeneratedKeys.resultSet aka "keys" must beLike {
              case genKeys =>
                (genKeys.next aka "has first generated key" must beTrue)
                  .and(genKeys.getInt(1) aka "first key" must_=== 3)
                  .and(genKeys.next aka "has second generated key" must beTrue)
                  .and(genKeys.getInt(1) aka "second key" must_=== 4)
                  .and(genKeys.next aka "has third generated key" must beFalse)
            })
      }
    }
  }
}
