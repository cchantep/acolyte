package acolyte

import java.sql.Date

import org.specs2.mutable.Specification

object AcolyteSpec extends Specification {
  "Acolyte" title

  sequential

  "Scala use case #1" should {
    val con = ScalaUseCases.useCase1()

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
          and(rs.getDate(3) aka "2nd row/2rd col" mustEqual new Date(2l))

      }

      "no third row" in {
        rs.next aka "has third row" must beFalse
      }
    }
  }
}
