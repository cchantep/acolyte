package acolyte

import java.sql.{ Date, SQLException }

import org.specs2.mutable.Specification

object AcolyteSpec extends Specification {
  "Acolyte" title

  sequential

  "Java use case #1" should {
    val con = usecase.JavaUseCases.useCase1()

    "return 2 for DELETE statement" in {
      con.prepareStatement("DELETE * FROM table").
        executeUpdate aka "update count" mustEqual 2

    }

    "return 1 for other update statement" in {
      lazy val s = con.prepareStatement(
        "INSERT INTO table('id', 'name') VALUES (?, ?)")

      s.setString(1, "idVal");
      s.setString(2, "idName")

      s.executeUpdate aka "update count" mustEqual 1
    }

    "return empty resultset for SELECT query" in {
      con.createStatement().executeQuery("SELECT * FROM table").
        aka("resultset") mustEqual RowLists.
        rowList1(classOf[String]).resultSet()

    }

    "return resultset of 2 rows" >> {
      lazy val s = {
        val st = con.prepareStatement("EXEC that_proc(?)")
        st.setString(1, "test")
        st
      }
      lazy val rs = s.executeQuery

      "with expected 3 columns on first row" in {
        (rs.next aka "has first row" must beTrue).
          and(rs.getString(1) aka "1st row/1st col (by index)" mustEqual "str").
          and(rs.getString("String").
            aka("1st row/1st col (by label)") mustEqual "str").
          and(rs.getFloat(2) aka "1st row/2nd col" mustEqual 1.2f).
          and(rs.getDate(3).
            aka("1st row/2rd col (by index)") mustEqual new Date(1l)).
          and(rs.getDate("Date").
            aka("1st row/2rd col (by label)") mustEqual new Date(1l))

      }

      "with expected 3 columns on second row" in {
        (rs.next aka "has second row" must beTrue).
          and(rs.getString(1) aka "2nd row/1st col (by index)" mustEqual "val").
          and(rs.getString("String").
            aka("2nd row/1st col (by label)") mustEqual "val").
          and(rs.getFloat(2) aka "2nd row/2nd col" mustEqual 2.34f).
          and(rs.getDate(3).
            aka("2nd row/2rd col (by index)") mustEqual new Date(2l)).
          and(rs.getDate("Date").
            aka("2nd row/2rd col (by label)") mustEqual new Date(2l))

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
        (rs.next aka "has first row" must beTrue).
          and(rs.getString(1).
            aka("1st row/1st col (by index)") mustEqual "text").
          and(rs.getFloat(2).
            aka("1st row/2nd col (by index)") mustEqual 2.3f).
          and(rs.getDate(3).
            aka("1st row/2rd col (by index)") mustEqual new Date(3l)).
          and(rs.getString("str").
            aka("1st row/1st col (by label)") mustEqual "text").
          and(rs.getFloat("f").
            aka("1st row/2nd col (by label)") mustEqual 2.3f).
          and(rs.getDate("date").
            aka("1st row/2rd col (by label)") mustEqual new Date(3l))

      }

      "with expected 3 columns on second row" in {
        (rs.next aka "has second row" must beTrue).
          and(rs.getString(1).
            aka("2nd row/1st col (by index)") mustEqual "label").
          and(rs.getFloat(2).
            aka("2nd row/2nd col (by index)") mustEqual 4.56f).
          and(rs.getDate(3).
            aka("2nd row/2rd col (by index)") mustEqual new Date(4l)).
          and(rs.getString("str").
            aka("2nd row/1st col (by label)") mustEqual "label").
          and(rs.getFloat("f").
            aka("2nd row/2nd col (by label)") mustEqual 4.56f).
          and(rs.getDate("date").
            aka("2nd row/2rd col (by label)") mustEqual new Date(4l))

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

      (s.executeQuery.next aka "has first row" must beFalse).
        and(s.getWarnings.getMessage aka "reason" mustEqual "Warn EXEC")
    }

    "update nothing" in {
      lazy val s = con.prepareStatement("UPDATE x")

      s.executeUpdate aka "updated count" mustEqual 0
    }

    "raise SQL warning on DELETE execution" in {
      lazy val s = con.prepareStatement("DELETE y")

      (s.executeUpdate aka "updated count" mustEqual 0).
        and(s.getWarnings.getMessage aka "reason" mustEqual "Warn DELETE")
    }
  }

  "Java use case #4" should {
    val con = usecase.JavaUseCases.useCase4

    "return expected boolean result" in {
      lazy val s = con.prepareStatement("SELECT * FROM table")
      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue).
        and(rs.getBoolean(1) aka "single column" must beTrue)

    }
  }
}
