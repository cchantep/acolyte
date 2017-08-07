package acolyte.reactivemongo

import scala.util.Try

import org.specs2.mutable.Specification
import org.specs2.matcher.{ Expectable, Matcher, MatchResult }

import reactivemongo.bson.{
  BSONBoolean,
  BSONDocument,
  BSONInteger,
  BSONString
}
import reactivemongo.core.protocol.Response

trait ResponseMatchers { specs: Specification ⇒
  def beResponse(f: List[BSONDocument] ⇒ MatchResult[_]) =
    new Matcher[Try[Response]] {
      def apply[R <: Try[Response]](e: Expectable[R]) = {
        e.value aka "prepared" must beSuccessfulTry.which { resp ⇒
          val r = f(Response.parse(resp).toList).toResult

          result(
            r.isSuccess,
            s"response is valid and ${r.message}",
            s"response is valid but ${r.message}", e
          )
        }
      }
    }

  def beQueryError(msg: String, code: Option[Int] = None) =
    new Matcher[Try[Response]] {
      def apply[R <: Try[Response]](e: Expectable[R]) =
        e.value aka "prepared" must beSuccessfulTry.which { r ⇒
          r.reply.inError aka "in-error" must beTrue and (
            Response.parse(r).toList aka "response" must beLike {
              case ValueDocument(("$err", BSONString(m)) :: others) :: Nil ⇒
                m aka "error message" must_== msg and ((code, others).
                  aka("extra properties") must beLike {
                    case (None, _) ⇒ ok
                    case (Some(a), ("code", BSONInteger(b)) :: Nil) ⇒
                      a aka "code" must_== b
                  })
            }
          )
        }
    }

  def beWriteError(msg: String, code: Option[Int] = None) =
    new Matcher[Try[Response]] {
      def apply[R <: Try[Response]](e: Expectable[R]) =
        e.value aka "prepared" must beSuccessfulTry.which {
          Response.parse(_).toList aka "response" must beLike {
            case ValueDocument(("ok", BSONInteger(0)) ::
              ("err", BSONString(err)) ::
              ("errmsg", BSONString(errmsg)) ::
              ("code", BSONInteger(c)) ::
              ("updatedExisting", BSONBoolean(false)) ::
              ("n", BSONInteger(0)) :: Nil) :: Nil ⇒
              err aka "error message (err)" must_== msg and (
                errmsg aka "error message (errmsg)" must_== msg
              ) and (
                  c aka "error code" must_== code.getOrElse(-1)
                )
          }
        }
    }

  def beWriteSuccess(count: Int, updatedExisting: Boolean) =
    new Matcher[List[BSONDocument]] {
      val C = count
      val U = updatedExisting
      def apply[L <: List[BSONDocument]](e: Expectable[L]) =
        e.value aka "body" must beLike {
          case ValueDocument(("ok", BSONInteger(1)) ::
            ("updatedExisting", BSONBoolean(U)) ::
            ("n", BSONInteger(C)) :: Nil) :: Nil ⇒ ok
        }
    }
}
