import acolyte.jdbc.QueryResult
import acolyte.jdbc.RowLists.rowList3
import acolyte.jdbc.AcolyteDSL.withQueryResult
import acolyte.jdbc.Implicits._

object PersistenceSpec extends org.specs2.mutable.Specification {
  "Persistence" title

  val formTable = rowList3( // Table schema
    classOf[String] -> "id",
    classOf[Int] -> "level",
    classOf[String] -> "label")

  val formFixtures = formTable
    .append("section-1", 1, "Title of section #1") // Append rows
    .append("s1-opt1", 2, "option 1")
    .append("s1-opt2", 2, "option 2")
    .append("s1-opt2a", 3, "sub-option 2a")
    .append("s1-opt2b", 3, "sub-option 2b")
    .append("s1-opt3", 2, "option 3")
    .append("section-2", 1, "Section #2 title")
    .append("s2-opt1", 2, "option 1")
    .append("s2-opt2", 2, "option 2")

  val expectedForm = Form(sections = List(
    Section(title = "Title of section #1",
      options = List(
        SectionOption("option 1"),
        SectionOption("option 2", List(
          SubOption("sub-option 2a"),
          SubOption("sub-option 2b"))),
        SectionOption("option 3"))),
    Section(title = "Section #2 title",
      options = List(
        SectionOption("option 1"),
        SectionOption("option 2")))))

  "Form" should {
    "not be found if data is missing" in withQueryResult(QueryResult.Nil) {
      // Acolyte pushes empty form data into used JDBC connection
      implicit con ⇒ Persistence.form aka "loaded form" must beRight(None)
    }

    "be successfully loaded from valid data" in withQueryResult(formFixtures) {
      implicit con ⇒
        Persistence.form aka "loaded form" must beRight(Some(expectedForm))
    }
  }

  "Form error" should {
    "be detected when first row is an option" in withQueryResult(
      // Inject a single option row
      formTable :+ ("opt", 2, "option")) { implicit con ⇒
        Persistence.form must beLeft { err: String ⇒
          err aka "error" must startWith("Unexpected item: SectionOption")
        }
      }

    "be detected when first row is a sub-option" in withQueryResult(
      // Inject a single sub-option row
      formTable :+ ("sub", 3, "sub-option")) { implicit con ⇒
        Persistence.form must beLeft { err: String ⇒
          err aka "error" must startWith("Unexpected item: SubOption")
        }
      }

    "be detected when first row of section is a sub-option" in withQueryResult(
      // Inject section row followed by a sub-option one
      formTable :+ ("sec", 1, "section") :+ ("sub", 3, "sub-option")) {
        implicit con ⇒
          Persistence.form must beLeft { err: String ⇒
            err aka "error" must startWith("Unexpected item: SubOption")
          }
      }
  }
}
