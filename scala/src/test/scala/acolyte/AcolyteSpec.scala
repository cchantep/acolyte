package acolyte

import java.sql.{ Date, SQLException }

object AcolyteSpec extends org.specs2.mutable.Specification {
  "Acolyte" title

  sequential

  "Scala use case #1" should {
    val con = ScalaUseCases.useCase1

    case class Foo(sql: String, ps: Seq[String])

    trait PartialHandler {
      def apply(x: Foo): Option[String]
      def :+(next: PartialHandler): Seq[PartialHandler] = Seq(this, next)
    }

    object PartialHandler {
      def matchStatement(e: String)(f: Foo ⇒ String) = new PartialHandler {
        lazy val re = e.r
        def apply(x: Foo) = re.findFirstIn(x.sql).map(_ ⇒ f(x))
      }
    }

    final case class RegexMatcher(e: String)(f: Foo ⇒ String)
        extends PartialHandler {

      lazy val re = e.r
      def apply(x: Foo) = re.findFirstIn(x.sql).map(_ ⇒ f(x))
    }

    def handle(f: Foo, h: PartialHandler): String = handle(f, Seq(h))

    @annotation.tailrec
    def handle(f: Foo, hs: Traversable[PartialHandler]): String = {
      hs.headOption match {
        case Some(h) ⇒ h(f) match {
          case Some(r) ⇒ r
          case _       ⇒ handle(f, hs.tail)
        }
        case _ ⇒ sys.error(s"Fail: $f")
      }
    }

    handle(Foo("test", Nil), RegexMatcher(".*")(_ ⇒ "x"))

    "return 2 for DELETE statement" in {
      con.prepareStatement("DELETE * FROM table").
        executeUpdate aka "update count" mustEqual 2

    }

    "return 1 for other update statement" in {
      lazy val s = con.prepareStatement(
        "INSERT INTO table('id', 'name') VALUES (?, ?)")

      s.setString(1, "idVal");
      s.setString(2, "idName")

      s.executeUpdate aka "update count" mustEqual 1
    }

    "return empty resultset for SELECT query" in {
      con.createStatement().executeQuery("SELECT * FROM table").
        aka("resultset") mustEqual RowLists.rowList1(classOf[String]).resultSet

    }

    "return resultset of 2 rows" >> {
      lazy val s = {
        val st = con.prepareStatement("EXEC that_proc(?)")
        st.setString(1, "test")
        st
      }
      lazy val rs = s.executeQuery

      "with expected 3 columns on first row" in {
        (rs.next aka "has first row" must beTrue).
          and(rs.getString(1) aka "1st row/1st col" mustEqual "str").
          and(rs.getFloat(2) aka "1st row/2nd col" mustEqual 1.2f).
          and(rs.getDate(3) aka "1st row/2rd col" mustEqual new Date(1l))

      }

      "with expected 3 columns on second row" in {
        (rs.next aka "has second row" must beTrue).
          and(rs.getString(1) aka "2nd row/1st col" mustEqual "val").
          and(rs.getFloat(2) aka "2nd row/2nd col" mustEqual 2.34f).
          and(rs.getDate(3) aka "2nd row/2rd col" must beNull)

      }

      "no third row" in {
        rs.next aka "has third row" must beFalse
      }
    }
  }

  "Scala use case #2" should {
    val con = ScalaUseCases.useCase2

    "throw exception for update statement" in {
      con.prepareStatement("DELETE * FROM table").
        executeUpdate aka "update" must throwA[SQLException](
          message = "No update handler")

    }

    "return empty resultset for SELECT query" in {
      lazy val s = {
        val st = con.prepareStatement("SELECT * FROM table")
        st.setString(1, "test")
        st
      }
      lazy val rs = s.executeQuery

      "with expected 3 columns on first row" in {
        (rs.next aka "has first row" must beTrue).
          and(rs.getString(1).
            aka("1st row/1st col (by index)") mustEqual "text").
          and(rs.getFloat(2).
            aka("1st row/2nd col (by index)") mustEqual 2.3f).
          and(rs.getDate(3).
            aka("1st row/2rd col (by index)") mustEqual new Date(3l)).
          and(rs.getString("str").
            aka("1st row/1st col (by label)") mustEqual "text").
          and(rs.getFloat("f").
            aka("1st row/2nd col (by label)") mustEqual 2.3f).
          and(rs.getDate("date").
            aka("1st row/2rd col (by label)") mustEqual new Date(3l))

      }

      "with expected 3 columns on second row" in {
        (rs.next aka "has second row" must beTrue).
          and(rs.getString(1).
            aka("2nd row/1st col (by index)") mustEqual "label").
          and(rs.getFloat(2).
            aka("2nd row/2nd col (by index)") mustEqual 4.56f).
          and(rs.getDate(3).
            aka("2nd row/2rd col (by index)") mustEqual new Date(4l)).
          and(rs.getString("str").
            aka("2nd row/1st col (by label)") mustEqual "label").
          and(rs.getFloat("f").
            aka("2nd row/2nd col (by label)") mustEqual 4.56f).
          and(rs.getDate("date").
            aka("2nd row/2rd col (by label)") mustEqual new Date(4l))

      }

      "no third row" in {
        rs.next aka "has third row" must beFalse
      }
    }
  }

  "Scala use case #3" should {
    val con = ScalaUseCases.useCase3

    "return expected result with 1 parameter" in {
      lazy val s = con.prepareStatement("SELECT * FROM table WHERE id = ?")
      s.setString(1, "id")

      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue).
        and(rs.getString(1) aka "str(1)" mustEqual "useCase_3a")

    }

    "return expected result with 2 parameters" in {
      lazy val s = con.
        prepareStatement("SELECT * FROM table WHERE id = ? AND type = ?")
      s.setString(1, "id")
      s.setInt(2, 3)

      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue).
        and(rs.getString(1) aka "str(1)" mustEqual "useCase_3str").
        and(rs.getInt(2) aka "int(2)" mustEqual 2).
        and(rs.getLong(3) aka "long(3)" mustEqual 3)

    }

    "raise warning" in {
      lazy val s = con.prepareStatement("SELECT dummy")
      s.executeQuery

      Option(s.getWarnings) aka "warning" must beSome.which { w ⇒
        w.getMessage aka "message" mustEqual "Now you're warned"
      }
    }
  }

  "Scala use case #4" should {
    val con = ScalaUseCases.useCase4

    "return expected boolean result" in {
      lazy val s = con.prepareStatement("SELECT * FROM table")
      lazy val rs = s.executeQuery

      (rs.next aka "has first row" must beTrue).
        and(rs.getBoolean(1) aka "single column" must beTrue)

    }
  }

  "Single-case query context" should {
    def query(sql: String, c: java.sql.Connection): java.sql.ResultSet = {
      val rs = c.prepareStatement(sql).executeQuery
      rs.next(); rs
    }

    lazy val res1: QueryResult =
      RowLists.rowList2(classOf[String], classOf[Float]).
        append("test", 3.45f).asResult

    "always return provided result" >> {
      "for SELECT" in {
        val str: String = Acolyte.withQueryResult(res1) { c ⇒
          val rs = query("SELECT * FROM table", c)
          s"${rs.getString(1)} -> ${rs.getFloat(2) + 1f}"
        }

        str aka "from query result" mustEqual "test -> 4.45"
      }

      "for EXEC" in Acolyte.withQueryResult(res1) { c ⇒
        query("EXEC proc", c) aka "proc result" must beLike {
          case rs ⇒
            (rs.getString(1) aka "col #1" mustEqual "test").
              and(rs.getFloat(2) aka "col #2" mustEqual 3.45f)
        }
      }
    }
  }
}
