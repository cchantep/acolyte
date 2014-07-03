package acolyte.jdbc

import acolyte.jdbc.RowLists.stringList
import acolyte.jdbc.Implicits._

object RowSpec extends org.specs2.mutable.Specification {
  "Row" title

  "Null single value" should {
    lazy val list = (stringList :+ null)
    lazy val expectedRow = Rows.row1[String](null)

    "be inferred on :+" in {
      val rows = list.getRows
      val rs = list.resultSet
      rs.next()

      (rows.size aka "row count" mustEqual 1).
        and(rows.get(0) aka "row" mustEqual expectedRow).
        and(rs.getObject(1) aka "single column" must beNull)
    }
  }
}
