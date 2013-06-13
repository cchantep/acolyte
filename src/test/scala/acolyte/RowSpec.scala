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

    "be expected one for named Row1[String]" in {
      val r = row1("str", "name")

      (r.list.aka("cells") mustEqual List("str")).
        and(r.opt("name") must beSome("str"))

    }

    "be expected one for unnamed Row2[String, Int]" in {
      row2("str", 4).list aka "cells" mustEqual List("str", 4)
    }

    "be expected one for named Row2[String, Int]" in {
      val r = row2("str", "first", 6, "second")

      (r.list aka "cells" mustEqual List("str", 6)).
        and(r.opt("first") must beSome("str")).
        and(r.opt("second") must beSome(6))

    }
  }
}
