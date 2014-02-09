package acolyte

import acolyte.macros.Regex

object RegexSpec extends org.specs2.mutable.Specification {
  "Extractor factory for Regular expression" title

  "Extractor without capturing parenthesis" should {
    "match" in {
      Regex(".*").unapplySeq("abc") aka "extraction" must beSome
    }

    "not match" in {
      Regex("[0-9]+").unapplySeq("abc") aka "extraction" must beNone
    }
  }

  "Extractor with one capturing parenthesis" should {
    "match" in {
      Regex("(.*)").unapplySeq("abc") aka "extraction" must beSome.which { g ⇒
        g aka "captured groups" mustEqual Seq("abc")
      }
    }

    "not match" in {
      Regex("([0-9]+)").unapplySeq("abc") aka "extraction" must beNone
    }
  }

  "Extractor with several capturing parenthesis" should {
    "match" in {
      Regex("^([a-z]+);([0-9]*)").
        unapplySeq("abc;123") aka "extraction" must beSome.which {
          _ aka "captured groups" mustEqual Seq("abc", "123")

        }
    }

    "not match" in {
      Regex("([A-Z]*)/(.*)$").unapplySeq("abc") must beNone
    }
  }
}
