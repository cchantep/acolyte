package acolyte

import java.util.{ArrayList=>JList}

import org.specs2.mutable.Specification

object RowSpec extends Specification {
  "Row" title

  "Cell(s)" should {
    "be expected one for unnamed Row1[String]" in {
      lazy val ls = { val l = new JList[String](); l.add("str"); l }

      Rows.row1("str").cells aka "cells" mustEqual ls
    }

    "be expected one for unnamed Row2[String, Int]" in {
      lazy val ls = { val l = new JList[Any](); l.add("str"); l.add(4); l }

      Rows.row2("str", 4).cells aka "cells" mustEqual ls
    }
  }
}
