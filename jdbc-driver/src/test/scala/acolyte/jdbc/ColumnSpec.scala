package acolyte.jdbc

import org.specs2.mutable.Specification

import acolyte.jdbc.RowList.{ Column ⇒ Col }

object ColumnSpec extends Specification {
  "Column" title

  "Definition" should {
    "refuse null class" in {
      Col(null, "col") aka "define" must throwA[IllegalArgumentException](
        message = "No column class")

    }

    "refuse empty name" in {
      (Col(classOf[String], null).
        aka("null name") must throwA[IllegalArgumentException](
          message = "Invalid column name: null")).
          and(Col(classOf[String], "").
            aka("empty name") must throwA[IllegalArgumentException](
              message = "Invalid column name: "))

    }

    "be successful" >> {
      lazy val col1 = Col(classOf[Int], "int")
      lazy val col2 = new Column(classOf[Int], "int", true)

      "and not nullable" in {
        col1 aka "define" must beLike {
          case col ⇒ (col.columnClass aka "class" mustEqual classOf[Int]).
            and(col.name aka "name" mustEqual "int").
            and(col.nullable aka "nullable" must beFalse)

        }
      }

      "and nullable" in {
        col2 aka "define" must beLike {
          case col ⇒ (col.columnClass aka "class" mustEqual classOf[Int]).
            and(col.name aka "name" mustEqual "int").
            and(col.nullable aka "nullable" must beTrue)

        }
      }

      "and updated as not nullable" in {
        col2.withNullable(false) must beLike {
          case col ⇒ (col.columnClass aka "class" mustEqual classOf[Int]).
            and(col.name aka "name" mustEqual "int").
            and(col.nullable aka "nullable" must beFalse)
        }
      }
    }
  }
}
