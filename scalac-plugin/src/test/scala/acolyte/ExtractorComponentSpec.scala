package acolyte

object ExtractorComponentSpec extends org.specs2.mutable.Specification
    with MatchTest with PartialFunctionTest {

  "Extractor component" title

  "Match" >> {
    "Basic Pattern matching" should {
      "match extractor: Integer(n)" in {
        patternMatching("456") aka "matching" mustEqual List("num-456")
      }

      "not match" in {
        patternMatching("@") aka "matching" mustEqual Nil
      }
    }

    "Extractor with unapply" should {
      "rich match without binding: ~(IntRange(5, 10))" in {
        patternMatching("7") aka "matching" mustEqual List("5-to-10")
      }

      "rich match without binding: ~(IntRange(10, 20), i)" in {
        patternMatching("12") aka "matching" mustEqual List("range:12")
      }

      "rich match with pattern in bindings: ~(IndexOf('/'), a :: b :: c :: _)".
        in({
          patternMatching("/path/to/file") aka "matching" mustEqual (
            List("IndexOf: /path/to/file", "0", "5", "8"))
        })
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

      """rich match with literal: ~(Regex(re), "literal")""" in {
        patternMatching("# magic: stop 1").
          aka("matching") mustEqual List("Literal: # magic: stop 1")
      }

      """not rich match with literal: ~(Regex(re), "literal")""" in {
        patternMatching("# magic: start") aka "matching" mustEqual Nil
      }
    }
  }

  "Recursive match" >> {
    "Basic Pattern matching" should {
      "match extractor: Integer(n)" in {
        recursivePatternMatching("456") aka "matching" mustEqual List("num-456")
      }

      "not match" in {
        recursivePatternMatching("@") aka "matching" mustEqual Nil
      }
    }

    "Extractor with unapply" should {
      "rich match without binding: ~(IntRange(5, 10))" in {
        recursivePatternMatching("7") aka "matching" mustEqual List("5-to-10")
      }

      "rich match without binding: ~(IntRange(10, 20), i)" in {
        recursivePatternMatching("12") aka "matching" mustEqual List("range:12")
      }

      "rich match with pattern in bindings: ~(IndexOf('/'), a :: b :: c :: _)".
        in({
          recursivePatternMatching("/path/to/file") aka "matching" mustEqual (
            List("IndexOf: /path/to/file", "0", "5", "8"))
        })
    }

    "Extractor with unapplySeq" should {
      "rich match without binding: ~(Regex(re))" in {
        recursivePatternMatching("abc").
          aka("matching") mustEqual List("no-binding")
      }

      "rich match with one binding: ~(Regex(re), a)" in {
        recursivePatternMatching("# BCD.") aka "matching" mustEqual List("BCD")
      }

      "rich match with several bindings: ~(Regex(re), (a, b))" in {
        recursivePatternMatching("123;xyz") aka "matching" mustEqual List(
          "123;xyz", "xyz", "123")
      }

      """rich match with literal: ~(Regex(re), "literal")""" in {
        recursivePatternMatching("# magic: stop 2").
          aka("matching") mustEqual List("Literal: # magic: stop 2")
      }

      """not rich match with literal: ~(Regex(re), "literal")""" in {
        recursivePatternMatching("# magic: start") aka "matching" mustEqual Nil
      }
    }
  }

  "Partial function #1" >> {
    "Basic Pattern matching" should {
      "match extractor: Integer(n)" in {
        partialFun1("456") aka "matching" mustEqual List("num-456")
      }

      "not match" in {
        partialFun1("@") aka "matching" must throwA[MatchError]
      }
    }

    "Extractor with unapply" should {
      "rich match without binding: ~(IntRange(5, 10))" in {
        partialFun1("7") aka "matching" mustEqual List("5-to-10")
      }

      "rich match without binding: ~(IntRange(10, 20), i)" in {
        partialFun1("12") aka "matching" mustEqual List("range:12")
      }

      "rich match with pattern in bindings: ~(IndexOf('/'), a :: b :: c :: _)".
        in({
          partialFun1("/path/to/file") aka "matching" mustEqual (
            List("IndexOf: /path/to/file", "0", "5", "8"))
        })
    }

    "Extractor with unapplySeq" should {
      "rich match without binding: ~(Regex(re))" in {
        partialFun1("abc") aka "matching" mustEqual List("no-binding")
      }

      "rich match with one binding: ~(Regex(re), a)" in {
        partialFun1("# BCD.") aka "matching" mustEqual List("BCD")
      }

      "rich match with several bindings: ~(Regex(re), (a, b))" in {
        partialFun1("123;xyz") aka "matching" mustEqual List(
          "123;xyz", "xyz", "123")
      }

      """rich match with literal: ~(Regex(re), "literal")""" in {
        partialFun1("# magic: stop 3").
          aka("matching") mustEqual List("Literal: # magic: stop 3")
      }

      """not rich match with literal: ~(Regex(re), "literal")""" in {
        partialFun1("# magic: start").
          aka("matching") must throwA[MatchError]
      }
    }
  }

  "Partial function #2" >> {
    "Basic Pattern matching" should {
      "match extractor: Integer(n)" in {
        partialFun2("456") aka "matching" mustEqual List("num-456")
      }

      "not match" in {
        partialFun2("@") aka "matching" must throwA[MatchError]
      }
    }

    "Extractor with unapply" should {
      "rich match without binding: ~(IntRange(5, 10))" in {
        partialFun2("7") aka "matching" mustEqual List("5-to-10")
      }

      "rich match without binding: ~(IntRange(10, 20), i)" in {
        partialFun2("12") aka "matching" mustEqual List("range:12")
      }

      "rich match with pattern in bindings: ~(IndexOf('/'), a :: b :: c :: _)".
        in({
          partialFun2("/path/to/file") aka "matching" mustEqual (
            List("IndexOf: /path/to/file", "0", "5", "8"))
        })
    }

    "Extractor with unapplySeq" should {
      "rich match without binding: ~(Regex(re))" in {
        partialFun2("abc") aka "matching" mustEqual List("no-binding")
      }

      "rich match with one binding: ~(Regex(re), a)" in {
        partialFun2("# BCD.") aka "matching" mustEqual List("BCD")
      }

      "rich match with several bindings: ~(Regex(re), (a, b))" in {
        partialFun2("123;xyz") aka "matching" mustEqual List(
          "123;xyz", "xyz", "123")
      }

      """rich match with literal: ~(Regex(re), "literal")""" in {
        partialFun2("# magic: stop 4").
          aka("matching") mustEqual List("Literal: # magic: stop 4")
      }

      """not rich match with literal: ~(Regex(re), "literal")""" in {
        partialFun2("# magic: start").
          aka("matching") must throwA[MatchError]
      }
    }
  }

  "Partial function #3 - Anonymous function" >> {
    "Basic Pattern matching" should {
      "match extractor: Integer(n)" in {
        partialFun3(Some("456")) aka "matching" must_== Some(List("num-456"))
      }

      "not match" in {
        partialFun3(Some("@")) aka "matching" must throwA[MatchError]
      }
    }

    "Extractor with unapply" should {
      "rich match without binding: ~(IntRange(5, 10))" in {
        partialFun3(Some("7")) aka "matching" must_== Some(List("5-to-10"))
      }

      "rich match without binding: ~(IntRange(10, 20), i)" in {
        partialFun3(Some("12")) aka "matching" must_== Some(List("range:12"))
      }

      "rich match with pattern in bindings: ~(IndexOf('/'), a :: b :: c :: _)".
        in({
          partialFun3(Some("/path/to/file")) aka "matching" must_== Some(
            List("IndexOf: /path/to/file", "0", "5", "8"))
        })
    }

    "Extractor with unapplySeq" should {
      "rich match without binding: ~(Regex(re))" in {
        partialFun3(Some("abc")).
          aka("matching") must_== Some(List("no-binding"))
      }

      "rich match with one binding: ~(Regex(re), a)" in {
        partialFun3(Some("# BCD.")) aka "matching" must_== Some(List("BCD"))
      }

      "rich match with several bindings: ~(Regex(re), (a, b))" in {
        partialFun3(Some("123;xyz")) aka "matching" must_== Some(List(
          "123;xyz", "xyz", "123"))
      }

      """rich match with literal: ~(Regex(re), "literal")""" in {
        partialFun3(Some("# magic: stop 5")).
          aka("matching") must_== Some(List("Literal: # magic: stop 5"))
      }

      """not rich match with literal: ~(Regex(re), "literal")""" in {
        partialFun3(Some("# magic: start")).
          aka("matching") must throwA[MatchError]
      }
    }
  }
}

sealed trait PartialFunctionTest {
  val partialFun1: String ⇒ List[String] = {
    case ~(IntRange(5, 10), _)                         ⇒ List("5-to-10")
    case ~(IntRange(10, 20), i)                        ⇒ List(s"range:$i")
    case Integer(n)                                    ⇒ List(s"num-$n")
    case ~(Regex("^a.*"))                              ⇒ List("no-binding")
    case ~(Regex("# ([A-Z]+).*"), a)                   ⇒ List(a)
    case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b))   ⇒ List(str, b, a)
    case str @ ~(Regex("# magic: ([a-z]+).*"), "stop") ⇒ List(s"Literal: $str")
    case str @ ~(IndexOf('/'), a :: b :: c :: _) ⇒
      List(s"IndexOf: $str", a.toString, b.toString, c.toString)

  }

  def partialFun2: String ⇒ List[String] = {
    case ~(IntRange(5, 10), _)                         ⇒ List("5-to-10")
    case ~(IntRange(10, 20), i)                        ⇒ List(s"range:$i")
    case Integer(n)                                    ⇒ List(s"num-$n")
    case ~(Regex("^a.*"))                              ⇒ List("no-binding")
    case ~(Regex("# ([A-Z]+).*"), a)                   ⇒ List(a)
    case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b))   ⇒ List(str, b, a)
    case str @ ~(Regex("# magic: ([a-z]+).*"), "stop") ⇒ List(s"Literal: $str")
    case str @ ~(IndexOf('/'), a :: b :: c :: _) ⇒
      List(s"IndexOf: $str", a.toString, b.toString, c.toString)

  }

  /* Anonymous partial function */
  def partialFun3(s: Option[String]): Option[List[String]] = s map {
    case ~(IntRange(5, 10), _)                         ⇒ List("5-to-10")
    case ~(IntRange(10, 20), i)                        ⇒ List(s"range:$i")
    case Integer(n)                                    ⇒ List(s"num-$n")
    case ~(Regex("^a.*"))                              ⇒ List("no-binding")
    case ~(Regex("# ([A-Z]+).*"), a)                   ⇒ List(a)
    case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b))   ⇒ List(str, b, a)
    case str @ ~(Regex("# magic: ([a-z]+).*"), "stop") ⇒ List(s"Literal: $str")
    case str @ ~(IndexOf('/'), a :: b :: c :: _) ⇒
      List(s"IndexOf: $str", a.toString, b.toString, c.toString)

  }
}

sealed trait MatchTest {
  def patternMatching(s: String): List[String] = s match {
    case ~(IntRange(5, 10), _)                         ⇒ List("5-to-10")
    case ~(IntRange(10, 20), i)                        ⇒ List(s"range:$i")
    case Integer(n)                                    ⇒ List(s"num-$n")
    case ~(Regex("^a.*"))                              ⇒ List("no-binding")
    case ~(Regex("# ([A-Z]+).*"), a)                   ⇒ List(a)
    case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b))   ⇒ List(str, b, a)
    case str @ ~(Regex("# magic: ([a-z]+).*"), "stop") ⇒ List(s"Literal: $str")
    case str @ ~(IndexOf('/'), a :: b :: c :: _) ⇒
      List(s"IndexOf: $str", a.toString, b.toString, c.toString)
    case x ⇒ Nil
  }

  def recursivePatternMatching(s: String): List[String] = s match {
    case v ⇒ v match {
      case ~(IntRange(5, 10), _)                       ⇒ List("5-to-10")
      case ~(IntRange(10, 20), i)                      ⇒ List(s"range:$i")
      case Integer(n)                                  ⇒ List(s"num-$n")
      case ~(Regex("^a.*"))                            ⇒ List("no-binding")
      case ~(Regex("# ([A-Z]+).*"), a)                 ⇒ List(a)
      case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ List(str, b, a)
      case str @ ~(Regex("# magic: ([a-z]+).*"), "stop") ⇒
        List(s"Literal: $str")
      case str @ ~(IndexOf('/'), a :: b :: c :: _) ⇒
        List(s"IndexOf: $str", a.toString, b.toString, c.toString)
      case x ⇒ Nil
    }
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

/** Character index extractor */
sealed case class IndexOf(ch: Char) {
  def unapply(v: String): Option[List[Int]] = {
    val is = v.foldLeft(0 -> List.empty[Int]) { (st, c) ⇒
      val (i, l) = st
      i + 1 -> { if (c == ch) l :+ i else l }
    }._2

    if (is.isEmpty) None else Some(is)
  }
}
