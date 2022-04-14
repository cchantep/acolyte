package acolyte.reactivemongo

import reactivemongo.io.netty.channel.DefaultChannelId
import reactivemongo.api.bson.{ BSONDocument, BSONInteger, BSONString }

final class QueryResponseSpec
  extends org.specs2.mutable.Specification with ResponseMatchers {

  "Query response" title

  @inline def channelId() = DefaultChannelId.newInstance()

  "Reponse" should {
    "be made for error message" >> {
      "using generic factory" in {
        QueryResponse("error message #1") aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "query response" must beQueryError("error message #1"))
        }
      }

      "using named factory" in {
        QueryResponse.failed("error message #2") aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "query response" must beQueryError("error message #2"))
        }
      }
    }

    "be made for error with code" >> {
      "using generic factory" in {
        QueryResponse("error message #3" → 5) aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "query response" must beQueryError(
              "error message #3", Some(5)))
        }
      }

      "using named factory" in {
        QueryResponse.failed("error message #4", 7) aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "query response" must beQueryError(
              "error message #4", Some(7)))
        }
      }
    }

    "be made for successful result" >> {
      val Doc1 = BSONDocument("a" → "b")

      "with a single document using generic factory" in {
        QueryResponse(Doc1) aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "query response" must beResponse {
              case ValueDocument(("a", BSONString("b")) :: Nil) :: Nil ⇒ ok
            })
        }
      }

      "with a single document using named factory" in {
        QueryResponse.successful(Doc1) aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "query response" must beResponse {
              case ValueDocument(("a", BSONString("b")) :: Nil) :: Nil ⇒ ok
            })
        }
      }

      "with a single document using a writable value" in {
        QueryResponse(Foo("test", 1)) aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "query response" must beResponse {
              case ValueDocument(
                ("name", BSONString("test")) ::
                  ("age", BSONInteger(1)) :: Nil) :: Nil ⇒ ok
            })
        }
      }

      "with a many documents using generic factory" in {
        QueryResponse(Seq(Doc1, BSONDocument("b" → 2))).
          aka("prepared") must beLike {
            case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
              _ aka "response" must beResponse {
                case Doc1 :: ValueDocument(
                  ("b", BSONInteger(2)) :: Nil) :: Nil ⇒ ok
              })
          }
      }

      "with many writable values" in {
        QueryResponse(List(Foo("foo", 1), Foo("bar", 2))) must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "query response" must beResponse {
              case ValueDocument(
                ("name", BSONString("foo")) ::
                  ("age", BSONInteger(1)) :: Nil) ::
                ValueDocument(
                ("name", BSONString("bar")) ::
                  ("age", BSONInteger(2)) :: Nil) ::
                Nil ⇒ ok
            })
        }
      }

      "with a many documents using named factory" in {
        QueryResponse.successful(Doc1, BSONDocument("b" → 3)).
          aka("prepared") must beLike {
            case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
              _ aka "response" must beResponse {
                case Doc1 :: ValueDocument(
                  ("b", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
              })
          }
      }

      "with empty list of document" in {
        QueryResponse(List.empty[BSONDocument]) aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "response" must beResponse { case res if res.isEmpty ⇒ ok })
        }
      }

      "with empty success" in {
        QueryResponse.empty aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "response" must beResponse { case res if res.isEmpty ⇒ ok })
        }
      }

      "for count command" in {
        QueryResponse.count(3) aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "reponse" must beResponse {
              case ValueDocument(("ok", BSONInteger(1)) ::
                ("n", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
            })
        }
      }
    }

    "be undefined" >> {
      "using generic factory" in {
        QueryResponse(None) aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beNone
        }
      }

      "using named factory" in {
        QueryResponse.undefined aka "prepared" must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beNone
        }
      }
    }

    "be made from already prepared response" in {
      QueryResponse(QueryResponse(BSONDocument("iden" → "tity"))).
        aka("prepared") must beLike {
          case prepared ⇒ prepared(channelId()) aka "applied" must beSome.which(
            _ aka "response" must beResponse {
              case ValueDocument(("iden", BSONString("tity")) :: Nil) :: Nil ⇒
                ok
            })
        }
    }
  }
}

case class Foo(name: String, age: Int)

object Foo {
  import reactivemongo.api.bson._

  implicit val writer: BSONDocumentWriter[Foo] = Macros.writer
}
