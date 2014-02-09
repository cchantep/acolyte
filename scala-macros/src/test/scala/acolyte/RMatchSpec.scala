package acolyte

object RMatchSpec extends org.specs2.mutable.Specification with RMatchTest {
  "Macro rmatch" title

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
}

sealed trait RMatchTest {
  import acolyte.macros.{ Regex, Xt, rmatch }

  def regex(s: String): List[String] = rmatch {
    Xt(s) match {
      case Xt(Regex("^a.*"), ()) ⇒ Nil
      case Xt(Regex("# ([A-Z]+).*"), a) ⇒ List(
        a.asInstanceOf[String] /*TODO: types */ )

      case Xt(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ List(
        b.asInstanceOf[String], a.asInstanceOf[String])

      case s ⇒ List("fallback")
    }
  }
}
