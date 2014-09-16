package acolyte.reactivemongo

import scala.util.Try
import reactivemongo.bson.BSONDocument

object ResponseMakerSpec
    extends org.specs2.mutable.Specification with ResponseMakerFixtures {

  "Response maker" title

  "Query response maker" should {
    "be working for Traversable[BSONDocument]" in {
      val makr = implicitly[QueryResponseMaker[Traversable[BSONDocument]]]

      makr(2, documents) aka "response" must beSome.which { prepared ⇒
        zip(prepared, MongoDB.QuerySuccess(2, documents)).
          aka("maker") must beSuccessfulTry.like {
            case (a, b) ⇒ a.documents aka "response" must_== b.documents
          }
      }
    }

    "be working for an error message (String)" in {
      val makr = implicitly[QueryResponseMaker[String]]

      makr(3, "Custom error") aka "response" must beSome.which { prepared ⇒
        zip(prepared, MongoDB.QueryError(3, "Custom error")).
          aka("maker") must beSuccessfulTry.like {
            case (a, b) ⇒ a.documents aka "response" must_== b.documents
          }
      }
    }

    "be working for an error with code (String, Int)" in {
      val makr = implicitly[QueryResponseMaker[(String, Int)]]

      makr(3, "Custom error" -> 5) aka "response" must beSome.
        which { prepared ⇒
          zip(prepared, MongoDB.QueryError(3, "Custom error", Some(5))).
            aka("maker") must beSuccessfulTry.like {
              case (a, b) ⇒ a.documents aka "response" must_== b.documents
            }
        }
    }

    shapeless.test.illTyped("implicitly[QueryResponseMaker[Any]]")
  }

  "Write response maker" should {
    "be a successful one for boolean (updatedExisting)" in {
      val makr = implicitly[WriteResponseMaker[(Int, Boolean)]]

      makr(4, 1 -> true) aka "response" must beSome.which { prepared ⇒
        zip(prepared, MongoDB.WriteSuccess(4, 1, true)).
          aka("maker") must beSuccessfulTry.like {
            case (a, b) ⇒ a.documents aka "response" must_== b.documents
          }
      }
    }

    "be a successful one for Unit (updatedExisting = false)" in {
      val makr = implicitly[WriteResponseMaker[Unit]]

      makr(4, ()) aka "response" must beSome.which { prepared ⇒
        zip(prepared, MongoDB.WriteSuccess(4, 0, false)).
          aka("maker") must beSuccessfulTry.like {
            case (a, b) ⇒ a.documents aka "response" must_== b.documents
          }
      }
    }

    "be working for an error message (String)" in {
      val makr = implicitly[WriteResponseMaker[String]]

      makr(5, "Custom error #1") aka "response" must beSome.
        which { prepared ⇒
          zip(prepared, MongoDB.WriteError(5, "Custom error #1", None)).
            aka("maker") must beSuccessfulTry.like {
              case (a, b) ⇒ a.documents aka "response" must_== b.documents
            }
        }
    }

    "be working for an error with code (String, Int)" in {
      val makr = implicitly[WriteResponseMaker[(String, Int)]]

      makr(5, "Custom error #2" -> 7) aka "response" must beSome.
        which { prepared ⇒
          zip(prepared, MongoDB.WriteError(5, "Custom error #2", Some(7))).
            aka("maker") must beSuccessfulTry.like {
              case (a, b) ⇒ a.documents aka "response" must_== b.documents
            }
        }
    }

    shapeless.test.illTyped("implicitly[WriteResponseMaker[Any]]")
  }

  @inline def zip[A, B](a: Try[A], b: Try[B]): Try[(A, B)] =
    for { x ← a; y ← b } yield (x -> y)
}

sealed trait ResponseMakerFixtures {
  val documents = Seq(
    BSONDocument("prop1" -> "str", "propB" -> 1),
    BSONDocument("propB" -> 3, "prop1" -> "text"))
}
