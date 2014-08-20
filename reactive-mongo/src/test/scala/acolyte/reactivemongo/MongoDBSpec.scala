package acolyte.reactivemongo

import reactivemongo.bson.BSONDocument
import reactivemongo.core.protocol.Response

object MongoDBSpec extends org.specs2.mutable.Specification with MongoFixtures {
  "MongoDB" title

  "Successful query response" should {
    s"contains one expected document $doc1" in {
      MongoDB.Success(1, doc1) aka "response" must beSuccessfulTry.which {
        Response.parse(_).toList aka "results" must beLike {
          case first :: Nil ⇒
            bson(first) aka "single document" must_== bson(doc1)
        }
      }
    }

    s"contains expected collection of 3 documents" in {
      MongoDB.Success(2, doc2, doc1, doc3) aka "response" must {
        beSuccessfulTry.which {
          Response.parse(_).toList aka "results" must beLike {
            case a :: b :: c :: Nil ⇒
              bson(a) aka "first document" must_== bson(doc2) and (
                bson(b) aka "second document" must_== bson(doc1)) and (
                  bson(c) aka "third document" must_== bson(doc3))
          }
        }
      }
    }
  }

  "Erroneous query response" should {
    @inline def shouldMatch(r: Response, msg: String) =
      r.error aka "error" must beSome.which { err ⇒
        err.message aka "message" must_== msg and (
          err.originalDocument aka "document" must beSome(
            BSONDocument("$err" -> msg)))
      }

    "be expected MkResponseError" in {
      shouldMatch(MongoDB.MkResponseError(), "Fails to create response")
    }

    "be expected error #1" in {
      MongoDB.Error(2, "Error #1") aka "response" must beSuccessfulTry.
        which(shouldMatch(_, "Error #1"))
    }
  }
}

private[reactivemongo] trait MongoFixtures {
  import reactivemongo.bson.{
    BSONDateTime,
    BSONDouble,
    BSONInteger,
    BSONString,
    BSONValue
  }

  val doc1 = BSONDocument("email" -> "test1@test.fr", "age" -> 3)

  val doc2 = BSONDocument("name" -> "Document #2", "price" -> 5.1D)

  val doc3 = BSONDocument(
    "title" -> "Title", "modified" -> BSONDateTime(System.currentTimeMillis))

  @inline def bson(d: BSONDocument) = d.elements.toList
}
