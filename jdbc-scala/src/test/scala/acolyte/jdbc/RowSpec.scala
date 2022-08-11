package acolyte.jdbc

import acolyte.jdbc.Implicits._
import acolyte.jdbc.RowLists.stringList

object RowSpec extends org.specs2.mutable.Specification {
  "Row".title

  "Null single value" should {
    lazy val list = stringList :+ null

    "be inferred on :+" in {
      val rows = list.getRows
      val rs = list.resultSet
      rs.next()

      (rows.size aka "row count" must_=== 1)
        .and(rs.getObject(1) aka "single column" must beNull)
    }
  }
}
