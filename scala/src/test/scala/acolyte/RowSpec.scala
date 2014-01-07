package acolyte

import acolyte.RowLists.stringList
import acolyte.Implicits._

object RowSpec extends org.specs2.mutable.Specification {
  "Row" title

  "Cell(s)" should {
    "be expected one for unnamed Row1[String]" in {
      Rows.row1("str").list aka "cells" mustEqual List("str")
    }

    "be expected one for unnamed Row2[String, Int]" in {
      Rows.row2("str", 4).list aka "cells" mustEqual List("str", 4)
    }
  }

  "Null single value" should {
    lazy val list = (stringList :+ null)
    lazy val expectedRow = new Row.Row1[String](null)

    "be inferred on :+" in {
      val rows = list.getRows
      val rs = list.resultSet
      rs.next()

      println(s"#rows=${list.getRows}")
      (rows.size aka "row count" mustEqual 1).
        and(rows.get(0) aka "row" mustEqual expectedRow).
        and(rs.getObject(1) aka "single column" must beNull)
    }
  }
}
