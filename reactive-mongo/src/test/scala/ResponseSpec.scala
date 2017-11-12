package acolyte.reactivemongo

import reactivemongo.bson.{ BSONDocument, BSONString, BSONDouble, BSONLong }
import reactivemongo.core.protocol.{ Response, ResponseInfo }

class ResponseSpec extends org.specs2.mutable.Specification {
  "Response" title

  "Response" should {
    "be an error one" in {
      MongoDB.QueryError(1, "An error").
        aka("error") must beSuccessfulTry.which { error ⇒
          Response.parse(error).toList aka "body" must beLike {
            case ValueDocument(("$err", BSONString(msg)) :: Nil) :: Nil ⇒
              msg aka "$err property" must_== "An error"
          } and (error.info aka "response info" must_== ResponseInfo(1))
        }
    }

    "be fallback error" in {
      MongoDB.MkQueryError(2) aka "error" must beLike {
        case error ⇒
          Response.parse(error).toList aka "body" must beLike {
            case ValueDocument(("$err", BSONString(msg)) :: Nil) :: Nil ⇒
              msg aka "$err property" must_== "Fails to create response"
          } and (error.info aka "response info" must_== ResponseInfo(2))
      }
    }

    "be an successful one" in {
      val body = Seq(
        BSONDocument("prop1" → "str", "2prop" → 1.23D),
        BSONDocument("2prop" → BSONDocument("property" → 2L)))

      MongoDB.QuerySuccess(3, body) aka "response" must beSuccessfulTry.
        which { success ⇒
          Response.parse(success).toList aka "body" must beLike {
            case ValueDocument(("prop1", BSONString("str")) ::
              ("2prop", BSONDouble(1.23D)) :: Nil) ::
              ValueDocument(("2prop", ValueDocument(
                ("property", BSONLong(2L)) :: Nil)) :: Nil) :: Nil ⇒ ok
          } and (success.info aka "response info" must_== ResponseInfo(3))
        }
    }
  }
}
