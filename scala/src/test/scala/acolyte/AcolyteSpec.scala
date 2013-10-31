package acolyte

import java.sql.{ Date, SQLException }

import org.specs2.mutable.Specification

object AcolyteSpec extends Specification {
  "Acolyte" title

  sequential

  "Scala use case #1" should {
    val con = ScalaUseCases.useCase1

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
        aka("resultset") mustEqual RowLists.rowList1(classOf[String]).resultSet

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
          and(rs.getString(1) aka "1st row/1st col" mustEqual "str").
          and(rs.getFloat(2) aka "1st row/2nd col" mustEqual 1.2f).
          and(rs.getDate(3) aka "1st row/2rd col" mustEqual new Date(1l))

      }

      "with expected 3 columns on second row" in {
        (rs.next aka "has second row" must beTrue).
          and(rs.getString(1) aka "2nd row/1st col" mustEqual "val").
          and(rs.getFloat(2) aka "2nd row/2nd col" mustEqual 2.34f).
          and(rs.getDate(3) aka "2nd row/2rd col" must beNull)

      }

      "no third row" in {
        rs.next aka "has third row" must beFalse
      }
    }
  }

  "Scala use case #2" should {
    val con = ScalaUseCases.useCase2

    "throw exception for update statement" in {
      con.prepareStatement("DELETE * FROM table").
        executeUpdate aka "update" must throwA[SQLException](
          message = "No update handler")

    }

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

  "Scala use case #3" should {
    val con = ScalaUseCases.useCase3

    "return expected result with 1 parameter" in {
      lazy val s = con.prepareStatement("SELECT * FROM table WHERE id = ?")
      s.setString(1, "id")

      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue).
        and(rs.getString(1) aka "str(1)" mustEqual "useCase_3a")

    }

    "return expected result with 2 parameters" in {
      lazy val s = con.
        prepareStatement("SELECT * FROM table WHERE id = ? AND type = ?")
      s.setString(1, "id")
      s.setInt(2, 3)

      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue).
        and(rs.getString(1) aka "str(1)" mustEqual "useCase_3str").
        and(rs.getInt(2) aka "int(2)" mustEqual 2).
        and(rs.getLong(3) aka "long(3)" mustEqual 3)

    }
  }

  "Scala use case #4" should {
    val con = ScalaUseCases.useCase4

    "return expected boolean result" in {
      lazy val s = con.prepareStatement("SELECT * FROM table")
      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue).
        and(rs.getBoolean(1) aka "single column" must beTrue)

    }
  }
}
