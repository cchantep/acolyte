package acolyte

import org.specs2.mutable.Specification

import acolyte.RowList.Column.defineCol

object ColumnSpec extends Specification {
  "Column" title

  "Definition" should {
    "refuse null class" in {
      defineCol(null, "col") aka "define" must throwA[IllegalArgumentException](
        message = "No column class")

    }

    "refuse empty name" in {
      (defineCol(classOf[String], null).
        aka("null name") must throwA[IllegalArgumentException](
          message = "Invalid column name: null")).
          and(defineCol(classOf[String], "").
            aka("empty name") must throwA[IllegalArgumentException](
              message = "Invalid column name: "))

    }

    "be successful" in {
      defineCol(classOf[Int], "int") aka "define" must beLike {
        case col ⇒ (col.columnClass aka "class" mustEqual classOf[Int]).
          and(col.name aka "name" mustEqual "int")

      }
    }
  }
}
