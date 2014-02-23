package acolyte

object MatchPluginSpec extends org.specs2.mutable.Specification {
  "Match plugin" title

  case class A(e: String) {
    lazy val re = e.r
    def unapplySeq(target: Any): Option[List[String]] = re.unapplySeq(target)
  }

  case class B(e: String)

  "X" should {
    "Y" in {
      "x" match {
        case ~(B(".*"), a) => Nil
        case _ => Nil
      }

      true must beTrue
    }
  }

  /*
  "Regex extractor" should {
    "match without binding" in {
      regex("abc") aka "matching" mustEqual Nil
    }

    "match with one binding" in {
      regex("# BCD.") aka "matching" mustEqual List("BCD")
    }

    "match with several bindings" in {
      regex("123;xyz") aka "matching" mustEqual List("xyz", "123")
    }

    "not match" in {
      regex("456") aka "matching" mustEqual List("fallback")
    }
  }
   */
}

/*
sealed trait RMatchTest {
  import acolyte.macros.{ Regex, Xt, rmatch }

  object XX {
    def unapply(v: Any): Option[String] = ???
  }

  def regex(s: String): List[String] = rmatch {
    s match {
      case Xt(Regex("^a.*"), ())                  ⇒ Nil
      case XX(".*c$")                             ⇒ Nil
      case Xt(Regex("# ([A-Z]+).*"), a)           ⇒ List(a)
      case Xt(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ List(b, a)
      case x                                      ⇒ List(x)
    }
  }
}
 */
