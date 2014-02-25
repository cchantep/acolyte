package acolyte

object MatchComponentSpec
    extends org.specs2.mutable.Specification with MatchTest {

  "Match component" title

  "Basic Pattern matching" should {
    "match extractor: Integer(n)" in {
      patternMatching("456") aka "matching" mustEqual List("num-456")
    }

    "not match" in {
      patternMatching("@") aka "matching" mustEqual Nil
    }
  }

  "Extract with unapply" should {
    "rich match without binding: ~(IntRange(5, 10))" in {
      patternMatching("7") aka "matching" mustEqual List("5-to-10")
    }

    "rich match without binding: ~(IntRange(10, 20), i)" in {
      patternMatching("12") aka "matching" mustEqual List("range:12")
    }
  }

  "Extractor with unapplySeq" should {
    "rich match without binding: ~(Regex(re))" in {
      patternMatching("abc") aka "matching" mustEqual List("no-binding")
    }

    "rich match with one binding: ~(Regex(re), a)" in {
      patternMatching("# BCD.") aka "matching" mustEqual List("BCD")
    }

    "rich match with several bindings: ~(Regex(re), (a, b))" in {
      patternMatching("123;xyz") aka "matching" mustEqual List(
        "123;xyz", "xyz", "123")
    }
  }
}

sealed trait MatchTest {
  def patternMatching(s: String): List[String] = s match {
    case ~(IntRange(5, 10)) ⇒ List("5-to-10")
    case ~(IntRange(10, 20), i) ⇒ List(s"range:$i")
    case Integer(n) ⇒ List(s"num-$n")
    case ~(Regex("^a.*")) ⇒ List("no-binding")
    case ~(Regex("# ([A-Z]+).*"), a) ⇒ List(a)
    case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ List(str, b, a)
    case x ⇒ Nil
  }
}

/**
 * Extractor factory based on regular expression.
 *
 * {{{
 * val res: Boolean = rmatch {
 *   ~("abc") match {
 *     case ~(Regex("(.+)b([a-z]+)$"), (x, y)) => true // x == "a", y == "b"
 *     case _ => false
 *   }
 * }
 * }}}
 */
sealed case class Regex(e: String) {
  lazy val re = e.r

  /** See [[scala.util.matching.Regex.unapplySeq]]. */
  def unapplySeq(target: Any): Option[List[String]] = re.unapplySeq(target)
}

/** Integer extractor */
object Integer {
  val re = "^[0-9]+$".r
  def unapply(v: String): Option[String] =
    re.unapplySeq(v).fold[Option[String]](None)(_ ⇒ Some(v))
}

/** Integer range extractor */
sealed case class IntRange(min: Int, max: Int) {
  def unapply(v: String): Option[Int] = Integer.unapply(v) flatMap { s ⇒
    val i = s.toInt
    if (i < min || i > max) None else Some(i)
  }
}
