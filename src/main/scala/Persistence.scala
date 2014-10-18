import anorm._
import anorm.{ Error ⇒ AnormError, Success ⇒ AnormSuccess }
import anorm.SqlParser.{ str, int }

object Persistence {
  lazy val formRowParser = str("id") ~ int("level") ~ str("label") map {
    // Row per row parsing
    case _ ~ 1 ~ label ⇒ Section(label)
    case _ ~ 2 ~ label ⇒ SectionOption(label)
    case _ ~ 3 ~ label ⇒ SubOption(label)
  }

  /** Returns either error message, or form if there is data. */
  def form(implicit con: java.sql.Connection): Either[String, Option[Form]] =
    SQL"SELECT id, level, label FROM form_tbl".
      withResult(parseSections(_, Nil)).fold(
        x ⇒ Left(x.map(_.getMessage).mkString),
        _.right.map[Option[Form]](_ match {
          case x :: xs ⇒ Some(Form(x :: xs))
          case _       ⇒ None
        }))

  @annotation.tailrec
  private def parseSections(cur: Option[Cursor], ls: List[Section]): Either[String, List[Section]] = cur match {
    case Some(c) ⇒ formRowParser(c.row) match {
      case AnormError(msg) ⇒ Left(msg.toString)
      case AnormSuccess(s @ Section(_, _)) ⇒
        parseSectionOptions(c.next, Nil) match {
          case Left(err) ⇒ Left(err)
          case Right((rs, opts)) ⇒
            parseSections(rs, ls :+ s.copy(options = opts))
        }
      case AnormSuccess(i) ⇒ Left(s"Unexpected item: $i")
    }
    case _ ⇒ Right(ls)
  }

  @annotation.tailrec
  private def parseSectionOptions(cur: Option[Cursor], opts: List[SectionOption]): Either[String, (Option[Cursor], List[SectionOption])] = cur match {
    case Some(c) ⇒ formRowParser(c.row) match {
      case AnormError(msg) ⇒ Left(msg.toString)
      case AnormSuccess(o @ SectionOption(label, _)) ⇒
        parseSubOptions(c.next, Nil) match {
          case Left(err) ⇒ Left(err)
          case Right((rs, subopts)) ⇒
            parseSectionOptions(rs, opts :+ o.copy(suboptions = subopts))
        }
      case AnormSuccess(Section(_, _)) ⇒ /* next section */ Right(cur -> opts)
      case AnormSuccess(i)             ⇒ Left(s"Unexpected item: $i")
    }
    case _ ⇒ Right(None -> opts)
  }

  @annotation.tailrec
  private def parseSubOptions(cur: Option[Cursor], opts: List[SubOption]): Either[String, (Option[Cursor], List[SubOption])] = cur match {
    case Some(c) ⇒ formRowParser(c.row) match {
      case AnormError(msg) ⇒ Left(msg.toString)
      case AnormSuccess(o @ SubOption(label)) ⇒
        parseSubOptions(c.next, opts :+ o)
      case AnormSuccess(i) ⇒ Right(cur -> opts)
    }
    case _ ⇒ Right(None -> opts)
  }
}
