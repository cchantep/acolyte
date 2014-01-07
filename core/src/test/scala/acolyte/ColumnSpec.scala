package acolyte

import org.specs2.mutable.Specification

import acolyte.RowList.Column

object ColumnSpec extends Specification {
  "Column" title

  "Definition" should {
    "refuse null class" in {
      Column(null, "col") aka "define" must throwA[IllegalArgumentException](
        message = "No column class")

    }

    "refuse empty name" in {
      (Column(classOf[String], null).
        aka("null name") must throwA[IllegalArgumentException](
          message = "Invalid column name: null")).
          and(Column(classOf[String], "").
            aka("empty name") must throwA[IllegalArgumentException](
              message = "Invalid column name: "))

    }

    "be successful" in {
      Column(classOf[Int], "int") aka "define" must beLike {
        case col ⇒ (col.columnClass aka "class" mustEqual classOf[Int]).
          and(col.name aka "name" mustEqual "int")

      }
    }
  }
}
