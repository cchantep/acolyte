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
}
