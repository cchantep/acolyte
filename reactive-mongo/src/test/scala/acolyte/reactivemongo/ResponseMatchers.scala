package acolyte.reactivemongo

import scala.util.Try

import org.specs2.mutable.Specification
import org.specs2.matcher.{ Expectable, Matcher, MatchResult }

import reactivemongo.bson.{ BSONDocument, BSONInteger, BSONString }
import reactivemongo.core.protocol.Response

trait ResponseMatchers { specs: Specification ⇒
  def beResponse(f: List[BSONDocument] ⇒ MatchResult[_]) =
    new Matcher[Try[Response]] {
      def apply[R <: Try[Response]](e: Expectable[R]) = {
        e.value aka "prepared" must beSuccessfulTry.which { resp ⇒
          val r = f(Response.parse(resp).toList).toResult

          result(r.isSuccess,
            s"response is valid and ${r.message}",
            s"response is valid but ${r.message}", e)
        }
      }
    }

  def beQueryError(msg: String) = new Matcher[Try[Response]] {
    def apply[R <: Try[Response]](e: Expectable[R]) =
      e.value aka "prepared" must beSuccessfulTry.which {
        Response.parse(_).toList aka "response" must beLike {
          case ValueDocument(("$err", BSONString(m)) :: Nil) :: Nil ⇒
            m aka "error message" must_== msg
        }
      }
  }

  def beWriteError(msg: String, code: Option[Int] = None) =
    new Matcher[Try[Response]] {
      def apply[R <: Try[Response]](e: Expectable[R]) =
        e.value aka "prepared" must beSuccessfulTry.which {
          Response.parse(_).toList aka "response" must beLike {
            case ValueDocument(("ok", BSONInteger(0)) ::
              ("err", BSONString(err)) :: ("errmsg", BSONString(errmsg)) ::
              ("code", BSONInteger(c)) :: Nil) :: Nil ⇒
              err aka "error message (err)" must_== msg and (
                errmsg aka "error message (errmsg)" must_== msg) and (
                  c aka "error code" must_== code.getOrElse(-1))
          }
        }
    }
}
