package acolyte

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
}
