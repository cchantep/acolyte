package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.io.netty.channel.DefaultChannelId
import reactivemongo.api.bson.BSONDocument

final class ResponseMakerSpec
  extends org.specs2.mutable.Specification with ResponseMakerFixtures {

  "Response maker" title

  @inline def channelId() = DefaultChannelId.newInstance()

  "Query response maker" should {
    shapeless.test.illTyped("implicitly[QueryResponseMaker[Any]]")

    "be working for Traversable[BSONDocument]" in {
      val makr = implicitly[QueryResponseMaker[Traversable[BSONDocument]]]
      val cid = channelId()

      makr(cid, documents) aka "response" must beSome.which { prepared ⇒
        zip(prepared, MongoDB.querySuccess(cid, documents)).
          aka("maker") must beSuccessfulTry.like {
            case (a, b) ⇒ a.documents aka "response" must_== b.documents
          }
      }
    }

    "be working for an error message (String)" in {
      val makr = implicitly[QueryResponseMaker[String]]
      val cid = channelId()

      makr(cid, "Custom error") aka "response" must beSome.which { prepared ⇒
        zip(prepared, MongoDB.queryError(cid, "Custom error")).
          aka("maker") must beSuccessfulTry.like {
            case (a, b) ⇒ a.documents aka "response" must_== b.documents
          }
      }
    }

    "be working for an error with code (String, Int)" in {
      val makr = implicitly[QueryResponseMaker[(String, Int)]]
      val cid = channelId()

      makr(cid, "Custom error" → 5) aka "response" must beSome.
        which { prepared ⇒
          zip(prepared, MongoDB.queryError(cid, "Custom error", Some(5))).
            aka("maker") must beSuccessfulTry.like {
              case (a, b) ⇒ a.documents aka "response" must_== b.documents
            }
        }
    }
  }

  "Write response maker" should {
    shapeless.test.illTyped("implicitly[WriteResponseMaker[Any]]")

    "be a successful one for boolean (updatedExisting)" in {
      val makr = implicitly[WriteResponseMaker[(Int, Boolean)]]
      val cid = channelId()

      makr(cid, 1 → true) aka "response" must beSome.which { prepared ⇒
        zip(prepared, MongoDB.writeSuccess(cid, 1, true)).
          aka("maker") must beSuccessfulTry.like {
            case (a, b) ⇒ a.documents aka "response" must_== b.documents
          }
      }
    }

    "be a successful one for Unit (updatedExisting = false)" in {
      val makr = implicitly[WriteResponseMaker[Unit]]
      val cid = channelId()

      makr(cid, ()) aka "response" must beSome.which { prepared ⇒
        zip(prepared, MongoDB.writeSuccess(cid, 0, false)).
          aka("maker") must beSuccessfulTry.like {
            case (a, b) ⇒ a.documents aka "response" must_== b.documents
          }
      }
    }

    "be working for an error message (String)" in {
      val makr = implicitly[WriteResponseMaker[String]]
      val cid = channelId()

      makr(cid, "Custom error #1") aka "response" must beSome.
        which { prepared ⇒
          zip(prepared, MongoDB.writeError(cid, "Custom error #1", None)).
            aka("maker") must beSuccessfulTry.like {
              case (a, b) ⇒ a.documents aka "response" must_== b.documents
            }
        }
    }

    "be working for an error with code (String, Int)" in {
      val makr = implicitly[WriteResponseMaker[(String, Int)]]
      val cid = channelId()

      makr(cid, "Custom error #2" → 7) aka "response" must beSome.
        which { prepared ⇒
          zip(prepared, MongoDB.writeError(cid, "Custom error #2", Some(7))).
            aka("maker") must beSuccessfulTry.like {
              case (a, b) ⇒ a.documents aka "response" must_== b.documents
            }
        }
    }
  }

  @inline def zip[A, B](a: Try[A], b: Try[B]): Try[(A, B)] =
    for { x ← a; y ← b } yield (x → y)
}

sealed trait ResponseMakerFixtures {
  val documents = Seq(
    BSONDocument("prop1" → "str", "propB" → 1),
    BSONDocument("propB" → 3, "prop1" → "text"))
}
