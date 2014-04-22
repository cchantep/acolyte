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
    }
  }

  "Partial function #3 - Anonymous function" >> {
    "Basic Pattern matching" should {
      "match extractor: Integer(n)" in {
        partialFun3(Some("456")) aka "matching" mustEqual Some(List("num-456"))
      }

      "not match" in {
        partialFun3(Some("@")) aka "matching" must throwA[MatchError]
      }
    }

    "Extractor with unapply" should {
      "rich match without binding: ~(IntRange(5, 10))" in {
        partialFun3(Some("7")) aka "matching" mustEqual Some(List("5-to-10"))
      }

      "rich match without binding: ~(IntRange(10, 20), i)" in {
        partialFun3(Some("12")) aka "matching" mustEqual Some(List("range:12"))
      }
    }

    "Extractor with unapplySeq" should {
      "rich match without binding: ~(Regex(re))" in {
        partialFun3(Some("abc")).
          aka("matching") mustEqual Some(List("no-binding"))
      }

      "rich match with one binding: ~(Regex(re), a)" in {
        partialFun3(Some("# BCD.")) aka "matching" mustEqual Some(List("BCD"))
      }

      "rich match with several bindings: ~(Regex(re), (a, b))" in {
        partialFun3(Some("123;xyz")) aka "matching" mustEqual Some(List(
          "123;xyz", "xyz", "123"))
      }
    }
  }
}

sealed trait PartialFunctionTest {
  val partialFun1: String ⇒ List[String] = {
    case ~(IntRange(5, 10), _)                       ⇒ List("5-to-10")
    case ~(IntRange(10, 20), i)                      ⇒ List(s"range:$i")
    case Integer(n)                                  ⇒ List(s"num-$n")
    case ~(Regex("^a.*"))                            ⇒ List("no-binding")
    case ~(Regex("# ([A-Z]+).*"), a)                 ⇒ List(a)
    case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ List(str, b, a)
  }

  def partialFun2: String ⇒ List[String] = {
    case ~(IntRange(5, 10), _)                       ⇒ List("5-to-10")
    case ~(IntRange(10, 20), i)                      ⇒ List(s"range:$i")
    case Integer(n)                                  ⇒ List(s"num-$n")
    case ~(Regex("^a.*"))                            ⇒ List("no-binding")
    case ~(Regex("# ([A-Z]+).*"), a)                 ⇒ List(a)
    case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ List(str, b, a)
  }

  /* Anonymous partial function */
  def partialFun3(s: Option[String]): Option[List[String]] = s map {
    case ~(IntRange(5, 10), _)                       ⇒ List("5-to-10")
    case ~(IntRange(10, 20), i)                      ⇒ List(s"range:$i")
    case Integer(n)                                  ⇒ List(s"num-$n")
    case ~(Regex("^a.*"))                            ⇒ List("no-binding")
    case ~(Regex("# ([A-Z]+).*"), a)                 ⇒ List(a)
    case str @ ~(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ List(str, b, a)
  }
}

sealed trait MatchTest {
  def patternMatching(s: String): List[String] = s match {
    case ~(IntRange(5, 10), _) ⇒ List("5-to-10")
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
