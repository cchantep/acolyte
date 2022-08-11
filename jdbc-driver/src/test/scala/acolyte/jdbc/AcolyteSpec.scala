package acolyte.jdbc

import java.sql.Date

import org.specs2.mutable.Specification

object AcolyteSpec extends Specification {
  "Acolyte".title

  sequential

  "Java use case #1" should {
    val con = usecase.JavaUseCases.useCase1()

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
        .aka("resultset") must_=== RowLists
        .rowList1(classOf[String])
        .resultSet()

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
          .and(rs.getString(1) aka "1st row/1st col (by index)" must_=== "str")
          .and(
            rs.getString("String")
              .aka("1st row/1st col (by label)") must_=== "str"
          )
          .and(rs.getFloat(2) aka "1st row/2nd col" must_=== 1.2F)
          .and(
            rs.getDate(3).aka("1st row/2rd col (by index)") must_=== new Date(
              1L
            )
          )
          .and(
            rs.getDate("Date")
              .aka("1st row/2rd col (by label)") must_=== new Date(1L)
          )

      }

      "with expected 3 columns on second row" in {
        (rs.next aka "has second row" must beTrue)
          .and(rs.getString(1) aka "2nd row/1st col (by index)" must_=== "val")
          .and(
            rs.getString("String")
              .aka("2nd row/1st col (by label)") must_=== "val"
          )
          .and(rs.getFloat(2) aka "2nd row/2nd col" must_=== 2.34F)
          .and(rs.getDate(3) aka "2nd row/2rd col (by index)" must beNull)
          .and(rs.getDate("Date") aka "2nd row/2rd col (by label)" must beNull)

      }

      "no third row" in {
        rs.next aka "has third row" must beFalse
      }
    }
  }

  "Java use case #2" should {
    val con = usecase.JavaUseCases.useCase2()

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

  "Java use case #3" should {
    val con = usecase.JavaUseCases.useCase3

    "return empty result for SELECT query" in {
      lazy val s = con.prepareStatement("SELECT *")

      s.executeQuery.next aka "has first row" must beFalse
    }

    "return SQL warning for EXEC query" in {
      lazy val s = con.prepareStatement("EXEC proc")

      (s.executeQuery.next aka "has first row" must beFalse)
        .and(s.getWarnings.getMessage aka "reason" must_=== "Warn EXEC")
    }

    "update nothing" in {
      lazy val s = con.prepareStatement("UPDATE x")

      s.executeUpdate aka "updated count" must_=== 0
    }

    "raise SQL warning on DELETE execution" in {
      lazy val s = con.prepareStatement("DELETE y")

      (s.executeUpdate aka "updated count" must_=== 0)
        .and(s.getWarnings.getMessage aka "reason" must_=== "Warn DELETE")
    }
  }

  "Java use case #4" should {
    val con = usecase.JavaUseCases.useCase4

    "return expected boolean result" in {
      lazy val s = con.prepareStatement("SELECT * FROM table")
      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue)
        .and(rs.getBoolean(1) aka "single column" must beTrue)

    }
  }

  "Java use case #5" should {
    val con = usecase.JavaUseCases.useCase5

    "return expected boolean result" in {
      lazy val s = con.prepareStatement("INSERT INTO table(x) VALUE('y')")
      s.executeUpdate()

      val keys = s.getGeneratedKeys

      (keys.next aka "has generated key" must beTrue)
        .and(keys.getInt(1) aka "first key" must_=== 100)
        .and(keys.next aka "has second key" must beFalse)

    }
  }
}
