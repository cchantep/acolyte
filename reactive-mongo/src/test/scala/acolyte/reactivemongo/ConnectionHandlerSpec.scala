package acolyte.reactivemongo

object ConnectionHandlerSpec extends org.specs2.mutable.Specification 
    with QueryHandlerFixtures with WriteHandlerFixtures {

  "Connection handler" title

  "Empty handler" should {
    "not respond to any query" in {
      ConnectionHandler.empty aka "connection handler" must beLike {
        case h ⇒
          h.queryHandler(1, query1) aka "query result" must beNone and (
            h.writeHandler(2, write1._1, write1._2).
              aka("write result") must beNone)
      }
    }
  }
}
