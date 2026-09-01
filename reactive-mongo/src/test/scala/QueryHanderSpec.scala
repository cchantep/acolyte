package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.io.netty.channel.DefaultChannelId

import reactivemongo.api.bson.{ BSONDocument, BSONInteger, BSONString }

import reactivemongo.acolyte.Response

final class QueryHandlerSpec
    extends org.specs2.mutable.Specification
    with ResponseMatchers
    with QueryHandlerFixtures {

  "Query handler".title

  @inline def channelId() = DefaultChannelId.newInstance()

  "Handler" should {
    "return a response with Traversable[BSONDocument]" in {
      implicitly[QueryHandler]({ (_: Request) =>
        QueryResponse(Seq(BSONDocument("prop" -> "A")))
      }) aka "query handler" must beLike {
        case h =>
          h(channelId(), query1) must beSome[Try[Response]]
            .which(_ aka "response" must beResponseLike {
              case ValueDocument(("prop", BSONString("A")) :: Nil) :: Nil => ok
            })
      }
    }

    "return a response with a single BSONDocument" in {
      implicitly[QueryHandler]({ (_: Request) =>
        QueryResponse.successful(BSONDocument("prop" -> "A"))
      }) aka "query handler" must beLike {
        case h =>
          h(channelId(), query1) must beSome[Try[Response]]
            .which(_ aka "response" must beResponseLike {
              case ValueDocument(("prop", BSONString("A")) :: Nil) :: Nil => ok
            })
      }
    }

    "return an empty successful response" in {
      implicitly[QueryHandler]((_: Request) => QueryResponse.empty)
        .aka("query handler") must beLike {
        case h =>
          h(channelId(), query1) must beSome[Try[Response]]
            .which(_ aka "response" must beResponseLike {
              case res if res.isEmpty => ok
            })
      }
    }

    "return an error response" in {
      implicitly[QueryHandler]((_: Request) =>
        QueryResponse("Error message")
      ) aka "query handler" must beLike {
        case h =>
          h(channelId(), query1) must beSome[Try[Response]]
            .which(_ aka "response" must beQueryError("Error message"))
      }
    }

    "return no response" in {
      implicitly[QueryHandler]((_: Request) => QueryResponse(None))
        .aka("query handler") must beLike {
        case h => h(channelId(), query1) must beNone
      }
    }

    "be combined using orElse" in {
      QueryHandler { _ => QueryResponse.successful(BSONDocument("foo" -> 1)) }
        .orElse(QueryHandler.empty) must beLike {
        case h =>
          h(channelId(), query1) must beSome[Try[Response]]
            .which(_ aka "response" must beResponseLike {
              case ValueDocument(("foo", BSONInteger(1)) :: Nil) :: Nil => ok
            })
      }
    }
  }

  "Empty handler" should {
    "return no response" in {
      QueryHandler.empty aka "query handler" must beLike {
        case h => h(channelId(), query1) must beNone
      }
    }

    "be combined using orElse" in {
      QueryHandler.empty.orElse(QueryHandler { _ =>
        QueryResponse.successful(BSONDocument("foo" -> 1))
      }) aka "query handler" must beLike {
        case h =>
          h(channelId(), query1) must beSome[Try[Response]]
            .which(_ aka "response" must beResponseLike {
              case ValueDocument(("foo", BSONInteger(1)) :: Nil) :: Nil => ok
            })
      }
    }
  }

  "Mixed handler" should {
    val handler = QueryHandler { q =>
      q match {
        case Request("test1", _) => QueryResponse.undefined
        case Request("test2", _) => QueryResponse("Error #2")

        case Request("test3", _) =>
          QueryResponse(
            Seq(BSONDocument("prop" -> "A"), BSONDocument("a" -> 1))
          )

        case Request("test4", _) =>
          QueryResponse.successful(BSONDocument("prop" -> "B"))

        case req =>
          sys.error(s"Unexpected request: $req")
      }
    }

    "return no response" in {
      handler aka "mixed handler" must beLike {
        case h => h(channelId(), query1) aka "prepared" must beNone
      }
    }

    "return an error response" in {
      handler aka "mixed handler" must beLike {
        case h =>
          h(channelId(), query2) aka "prepared" must beSome[Try[Response]]
            .which(_ aka "query response" must beQueryError("Error #2"))
      }
    }

    "return an success response with many documents" in {
      handler aka "mixed handler" must beLike {
        case h =>
          h(channelId(), query3) aka "prepared" must beSome[Try[Response]]
            .which(_ aka "query response" must beResponseLike {
              case ValueDocument(("prop", BSONString("A")) :: Nil) ::
                  ValueDocument(("a", BSONInteger(1)) :: Nil) :: Nil =>
                ok
            })
      }
    }
  }
}

trait QueryHandlerFixtures {

  val query1 = new Request {
    val collection = "test1"
    val body = List(BSONDocument("filter" -> "valA"))
  }

  val query2 = new Request {
    val collection = "test2"
    val body = List(BSONDocument("filter" -> "valB"))
  }

  val query3 = new Request {
    val collection = "test3"
    val body = List(BSONDocument("filter" -> "valC"))
  }
}
