package acolyte

import org.specs2.mutable.Specification

import acolyte.Row._
import acolyte.Acolyte._

object RowSpec extends Specification {
  "Row" title

  "Cell(s)" should {
    "be expected for Row1[String]" in {
      (row1("str").tuples.
        aka("cells") mustEqual List[(String, String)]("str" -> null)).
        and(row1("str", "name").tuples.
          aka("cells") mustEqual List[(String, String)]("str" -> "name"))

    }

    "be expected for Row2[String, Int]" in {
      (row2("str", 4).tuples.
        aka("cells") mustEqual List[(Any, String)]("str" -> null, 4 -> null)).
        and(row2("str", "first", 6, "second").tuples.
          aka("cells") mustEqual List[(Any, String)]("str" -> "first",
            6 -> "second"))

    }
  }
}
