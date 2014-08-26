package acolyte.reactivemongo

object ConnectionHandlerSpec 
    extends org.specs2.mutable.Specification with QueryHandlerFixtures {

  "Connection handler" title

  "Empty handler" should {
    "not respond to any query" in {
      ConnectionHandler.empty aka "connection handler" must beLike {
        case h ⇒ h.queryHandler(1, query1) must beNone
      }
    }    
  }
}
