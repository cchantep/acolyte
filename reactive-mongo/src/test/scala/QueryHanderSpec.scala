package acolyte.reactivemongo

import reactivemongo.bson.{ BSONDocument, BSONInteger, BSONString }

class QueryHandlerSpec extends org.specs2.mutable.Specification
    with ResponseMatchers with QueryHandlerFixtures {

  "Query handler" title

  "Handler" should {
    "return a response with Traversable[BSONDocument]" in {
      implicitly[QueryHandler]({
        _: Request ⇒ QueryResponse(Seq(BSONDocument("prop" → "A")))
      }) aka "query handler" must beLike {
        case h ⇒ h(1, query1) must beSome.which(
          _ aka "response" must beResponse {
            case ValueDocument(("prop", BSONString("A")) :: Nil) :: Nil ⇒ ok
          }
        )
      }
    }

    "return a response with a single BSONDocument" in {
      implicitly[QueryHandler]({
        _: Request ⇒ QueryResponse.successful(BSONDocument("prop" → "A"))
      }) aka "query handler" must beLike {
        case h ⇒ h(1, query1) must beSome.which(
          _ aka "response" must beResponse {
            case ValueDocument(("prop", BSONString("A")) :: Nil) :: Nil ⇒ ok
          }
        )
      }
    }

    "return an empty successful response" in {
      implicitly[QueryHandler]({ _: Request ⇒ QueryResponse.empty }).
        aka("query handler") must beLike {
          case h ⇒ h(1, query1) must beSome.which(
            _ aka "response" must beResponse { case res if res.isEmpty ⇒ ok }
          )
        }
    }

    "return an error response" in {
      implicitly[QueryHandler]({ _: Request ⇒
        QueryResponse("Error message")
      }) aka "query handler" must beLike {
        case h ⇒ h(1, query1) must beSome.which(
          _ aka "response" must beQueryError("Error message")
        )
      }
    }

    "return no response" in {
      implicitly[QueryHandler]({ _: Request ⇒ QueryResponse(None) }).
        aka("query handler") must beLike {
          case h ⇒ h(1, query1) must beNone
        }
    }
  }

  "Empty handler" should {
    "return no response" in {
      QueryHandler.empty aka "query handler" must beLike {
        case h ⇒ h(1, query1) must beNone
      }
    }
  }

  "Mixed handler" should {
    val handler = QueryHandler { q ⇒
      q match {
        case Request("test1", _) ⇒ QueryResponse.undefined
        case Request("test2", _) ⇒ QueryResponse("Error #2")

        case Request("test3", _) ⇒ QueryResponse(
          Seq(BSONDocument("prop" → "A"), BSONDocument("a" → 1))
        )

        case Request("test4", _) ⇒
          QueryResponse.successful(BSONDocument("prop" → "B"))
      }
    }

    "return no response" in {
      handler aka "mixed handler" must beLike {
        case h ⇒ h(1, query1) aka "prepared" must beNone
      }
    }

    "return an error response" in {
      handler aka "mixed handler" must beLike {
        case h ⇒ h(2, query2) aka "prepared" must beSome.which(
          _ aka "query response" must beQueryError("Error #2")
        )
      }
    }

    "return an success response with many documents" in {
      handler aka "mixed handler" must beLike {
        case h ⇒ h(3, query3) aka "prepared" must beSome.which(
          _ aka "query response" must beResponse {
            case ValueDocument(("prop", BSONString("A")) :: Nil) ::
              ValueDocument(("a", BSONInteger(1)) :: Nil) :: Nil ⇒ ok
          }
        )
      }
    }
  }
}

trait QueryHandlerFixtures {
  val query1 = new Request {
    val collection = "test1"
    val body = List(BSONDocument("filter" → "valA"))
  }

  val query2 = new Request {
    val collection = "test2"
    val body = List(BSONDocument("filter" → "valB"))
  }

  val query3 = new Request {
    val collection = "test3"
    val body = List(BSONDocument("filter" → "valC"))
  }
}
