package acolyte

import java.sql.SQLException

import org.specs2.mutable.Specification

import acolyte.Row._
import acolyte.Acolyte._

object RowListSpec extends Specification {
  "Row list" title

  "Creation" should {
    "not accept null list" in {
      new RowList(null) aka "ctor" must throwA[IllegalArgumentException]
    }
  }

  "Result set fetch size" should {
    "be immutable" in {
      rowList[Row.Row1[String]].resultSet.setFetchSize(1).
        aka("setter") must throwA[UnsupportedOperationException]

    }

    "be 1" in {
      (rowList[Row1[String]].append(row1("str")).
        resultSet.getFetchSize aka "size" mustEqual 1).
        and(rowList[Row2[String, Float]].append(row2("str", 1.23.toFloat)).
          resultSet.getFetchSize aka "size" mustEqual 1)

    }

    "be 2" in {
      (rowList[Row1[String]].append(row1("a")).append(row1("b")).
        resultSet.getFetchSize aka "size" mustEqual 2)

    }
  }

  "Object column by index" should {
    "not be read when not on a row" in {
      rowList[Row1[String]].append(row1("str")).resultSet.
        getObject(1) aka "getObject" must throwA[SQLException](
          message = "Not on a row")

    }

    "be expected one" in {
      lazy val rs = (rowList[Row1[Long]] :+ row1(123.toLong)).resultSet
      rs.next

      rs.getObject(1) aka "cell1" mustEqual 123.toLong
    }

    "be null" in {
      lazy val rs = rowList[Row1[Float]].
        append(row1(null.asInstanceOf[Float], "name")).resultSet

      rs.next

      rs.getObject(1) aka "cell1" must beNull
    }

    "not be read with invalid index" in {
      lazy val rs = rowList[Row1[Long]].append(row1(123.toLong)).resultSet
      rs.next

      rs.getObject(2) aka "getObject" must throwA[SQLException](
        message = "Invalid column index: 2")

    }
  }

  "Object column by name" should {
    "not be read when not on a row" in {
      rowList[Row1[String]].append(row1("str", "n")).resultSet.
        getObject("n") aka "getObject" must throwA[SQLException](
          message = "Not on a row")

    }

    "be expected one" in {
      lazy val rs = (rowList[Row1[Long]] :+ row1(123.toLong, "l")).resultSet
      rs.next

      rs.getObject("l") aka "cell1" mustEqual 123.toLong
    }

    "be null" in {
      lazy val rs = rowList[Row1[Float]].
        append(row1(null.asInstanceOf[Float], "name")).resultSet

      rs.next

      rs.getObject("name") aka "cell1" must beNull
    }

    "not be read with invalid name" in {
      lazy val rs = rowList[Row1[Long]].append(row1(123.toLong)).resultSet
      rs.next

      (rs.getObject(null).
        aka("getObject") must throwA[SQLException]("Invalid label: null")).
        and(rs.getObject("label").
          aka("getObject") must throwA[SQLException]("Invalid label: label"))
    }
  }

  "String column from result set" should {
    "not be read by index when not on a row" in {
      (rowList[Row1[String]].append(row1("str")).resultSet.
        getString(1) aka "getString" must throwA[SQLException](
          message = "Not on a row")).
          and(rowList[Row1[String]].append(row1("str", "n")).resultSet.
            getString("n") aka "getString" must throwA[SQLException](
              message = "Not on a row"))

    }

    "be expected one" in {
      val rs = rowList[Row1[String]].:+(row1("str", "n")).resultSet
      rs.next

      (rs.getString(1) aka "string by index" mustEqual "str").
        and(rs.getString("n") aka "string by name" mustEqual "str")
    }

    "be null" in {
      val rs = (rowList[Row1[String]] :+ row1(null.asInstanceOf[String], "n")).
        resultSet

      rs.next

      (rs.getString(1) aka "string" must beNull).
        and(rs.getString("n") aka "string" must beNull)
    }
  }
}
