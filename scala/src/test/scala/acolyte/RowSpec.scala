package acolyte

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
}
