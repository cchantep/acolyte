package acolyte.reactivemongo

import reactivemongo.bson.{ BSONDocument, BSONInteger, BSONString }

object QueryHandlerSpec extends org.specs2.mutable.Specification 
    with ResponseMatchers with QueryHandlerFixtures {

  "Query handler" title

  "Handler" should {
    "return a response with Traversable[BSONDocument]" in {
      implicitly[QueryHandler]({
        _: Request ⇒ QueryResponse(Seq(BSONDocument("prop" -> "A")))
      }) aka "query handler" must beLike {
        case h ⇒ h(1, query1) must beSome.which(
          _ aka "response" must beSuccessResponse {
            case ValueDocument(("prop", BSONString("A")) :: Nil) :: Nil ⇒ ok
          })
      }
    }

    "return a response with a single BSONDocument" in {
      implicitly[QueryHandler]({
        _: Request ⇒ QueryResponse.successful(BSONDocument("prop" -> "A"))
      }) aka "query handler" must beLike {
        case h ⇒ h(1, query1) must beSome.which(
          _ aka "response" must beSuccessResponse {
            case ValueDocument(("prop", BSONString("A")) :: Nil) :: Nil ⇒ ok
          })
      }
    }

    "return an error response" in {
      implicitly[QueryHandler]({ _: Request ⇒ 
        QueryResponse("Error message") }) aka "query handler" must beLike {
          case h ⇒ h(1, query1) must beSome.which(
            _ aka "response" must beErrorResponse("Error message"))
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
    val handler = implicitly[QueryHandler]({ q: Request ⇒
      q match {
        case RequestBody("test1", _) ⇒ QueryResponse.empty
        case RequestBody("test2", _) ⇒ QueryResponse("Error #2")

        case RequestBody("test3", _) ⇒ QueryResponse(
          Seq(BSONDocument("prop" -> "A"), BSONDocument("a" -> 1)))

        case RequestBody("test4", _) ⇒
          QueryResponse.successful(BSONDocument("prop" -> "B"))
      }
    })

    "return no response" in {
      handler aka "mixed handler" must beLike {
        case h ⇒ h(1, query1) aka "prepared" must beNone
      }
    }

    "return an error response" in {
      handler aka "mixed handler" must beLike {
        case h ⇒ h(2, query2) aka "prepared" must beSome.which(
          _ aka "query response" must beErrorResponse("Error #2"))
      }
    }

    "return an success response with many documents" in {
      handler aka "mixed handler" must beLike {
        case h ⇒ h(3, query3) aka "prepared" must beSome.which(
          _ aka "query response" must beSuccessResponse {
            case ValueDocument(("prop", BSONString("A")) :: Nil) ::
              ValueDocument(("a", BSONInteger(1)) :: Nil) :: Nil ⇒ ok
          })
      }
    }
  }
}

trait QueryHandlerFixtures {
  val query1 = new Request {
    val collection = "test1"
    val body = BSONDocument("filter" -> "valA")
  }

  val query2 = new Request {
    val collection = "test2"
    val body = BSONDocument("filter" -> "valB")
  }

  val query3 = new Request {
    val collection = "test3"
    val body = BSONDocument("filter" -> "valC")
  }
}
