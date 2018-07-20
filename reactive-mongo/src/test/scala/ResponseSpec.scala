package acolyte.reactivemongo

import reactivemongo.bson.{ BSONDocument, BSONString, BSONDouble, BSONLong }
import reactivemongo.io.netty.channel.DefaultChannelId
import reactivemongo.core.protocol.Response

class ResponseSpec extends org.specs2.mutable.Specification {
  "Response" title

  @inline def channelId() = DefaultChannelId.newInstance()

  "Response" should {
    "be an error one" in {
      val cid = channelId()

      MongoDB.QueryError(cid, "An error").
        aka("error") must beSuccessfulTry.which { error ⇒
          Response.parse(error).toList aka "body" must beLike {
            case ValueDocument(("$err", BSONString(msg)) :: Nil) :: Nil ⇒
              msg aka "$err property" must_== "An error"
          } and (error.info._channelId aka "response info" must_== cid)
        }
    } tag "wip"

    "be fallback error" in {
      val cid = channelId()

      MongoDB.MkQueryError(cid) aka "error" must beLike {
        case error ⇒
          Response.parse(error).toList aka "body" must beLike {
            case ValueDocument(("$err", BSONString(msg)) :: Nil) :: Nil ⇒
              msg aka "$err property" must_== "Fails to create response"
          } and (error.info._channelId aka "response info" must_== cid)
      }
    }

    "be an successful one" in {
      val cid = channelId()
      val body = Seq(
        BSONDocument("prop1" → "str", "2prop" → 1.23D),
        BSONDocument("2prop" → BSONDocument("property" → 2L)))

      MongoDB.QuerySuccess(cid, body).
        aka("response") must beSuccessfulTry.which { success ⇒
          Response.parse(success).toList aka "body" must beLike {
            case ValueDocument(("prop1", BSONString("str")) ::
              ("2prop", BSONDouble(1.23D)) :: Nil) ::
              ValueDocument(("2prop", ValueDocument(
                ("property", BSONLong(2L)) :: Nil)) :: Nil) :: Nil ⇒ ok
          } and (success.info._channelId aka "response info" must_== cid)
        }
    }
  }
}
