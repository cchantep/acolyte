package acolyte

import org.specs2.mutable.Specification

import acolyte.Row._
import acolyte.Acolyte._

object RowSpec extends Specification {
  "Row" title

  "Cell(s)" should {
    "be expected one for unnamed Row1[String]" in {
      row1("str").list aka "cells" mustEqual List("str")
    }

    "be expected one for unnamed Row2[String, Int]" in {
      row2("str", 4).list aka "cells" mustEqual List("str", 4)
    }
  }
}
