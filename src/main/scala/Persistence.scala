import anorm.{ SQL, Error ⇒ AnormError, Row, Success ⇒ AnormSuccess, ~ }
import anorm.SqlParser.{ str, int }

object Persistence {
  lazy val formRowParser = str("id") ~ int("level") ~ str("label") map {
    // Row per row parsing
    case _ ~ 1 ~ label ⇒ Section(label)
    case _ ~ 2 ~ label ⇒ SectionOption(label)
    case _ ~ 3 ~ label ⇒ SubOption(label)
  }

  /** Returns either error message, or form if there is data. */
  def form(implicit con: java.sql.Connection): Either[String, Option[Form]] = {
    val res = SQL("SELECT id, level, label FROM form_tbl").apply()
    res.headOption.fold[Either[String, Option[Form]]](Right(None)) { _ ⇒
      parseSections(res, Nil).fold(Left(_), secs ⇒ Right(Some(Form(secs))))
    }
  }

  @annotation.tailrec
  private def parseSections(rows: Stream[Row], ls: List[Section]): Either[String, List[Section]] = rows.headOption match {
    case Some(row) ⇒ formRowParser(row) match {
      case AnormError(msg) ⇒ Left(msg.toString)
      case AnormSuccess(s @ Section(_, _)) ⇒
        parseSectionOptions(rows.tail, Nil) match {
          case Left(err) ⇒ Left(err)
          case Right((rs, opts)) ⇒
            parseSections(rs, ls :+ s.copy(options = opts))
        }
      case AnormSuccess(i) ⇒ Left(s"Unexpected item: $i")
    }
    case _ ⇒ Right(ls)
  }

  @annotation.tailrec
  private def parseSectionOptions(rows: Stream[Row], opts: List[SectionOption]): Either[String, (Stream[Row], List[SectionOption])] = rows.headOption match {
    case Some(row) ⇒ formRowParser(row) match {
      case AnormError(msg) ⇒ Left(msg.toString)
      case AnormSuccess(o @ SectionOption(label, _)) ⇒
        parseSubOptions(rows.tail, Nil) match {
          case Left(err) ⇒ Left(err)
          case Right((rs, subopts)) ⇒
            parseSectionOptions(rs, opts :+ o.copy(suboptions = subopts))
        }
      case AnormSuccess(Section(_, _)) ⇒ /* next section */ Right(rows -> opts)
      case AnormSuccess(i)             ⇒ Left(s"Unexpected item: $i")
    }
    case _ ⇒ Right(Stream() -> opts)
  }

  @annotation.tailrec
  private def parseSubOptions(rows: Stream[Row], opts: List[SubOption]): Either[String, (Stream[Row], List[SubOption])] = rows.headOption match {
    case Some(row) ⇒ formRowParser(row) match {
      case AnormError(msg) ⇒ Left(msg.toString)
      case AnormSuccess(o @ SubOption(label)) ⇒
        parseSubOptions(rows.tail, opts :+ o)
      case AnormSuccess(i) ⇒ Right(rows -> opts)
    }
    case _ ⇒ Right(Stream() -> opts)
  }
}
