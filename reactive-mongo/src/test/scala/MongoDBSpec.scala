package acolyte.reactivemongo

import reactivemongo.io.netty.channel.DefaultChannelId

import reactivemongo.api.bson.BSONDocument

import reactivemongo.acolyte.{parseResponse, Delete, Insert, Response, Update}
import reactivemongo.core.errors.DatabaseException

final class MongoDBSpec
    extends org.specs2.mutable.Specification
    with MongoFixtures {

  "MongoDB".title

  @inline def channelId() = DefaultChannelId.newInstance()

  "Response to successful query" should {
    s"contains one expected document BSONDocument(${doc1.elements.toList})" in {
      MongoDB
        .querySuccess(channelId(), Seq(doc1))
        .aka("response") must beSuccessfulTry.which {
        parseResponse(_).toList aka "results" must beLike {
          case first :: Nil =>
            bson(first) aka "single document" must_=== bson(doc1)
        }
      }
    }

    s"contains expected collection of 3 documents" in {
      MongoDB.querySuccess(
        channelId(),
        Seq(doc2, doc1, doc3)
      ) aka "response" must {
        beSuccessfulTry.which {
          parseResponse(_).toList aka "results" must beLike {
            case a :: b :: c :: Nil =>
              bson(a) aka "first document" must_=== bson(doc2) and (bson(
                b
              ) aka "second document" must_=== bson(doc1)) and (bson(
                c
              ) aka "third document" must_=== bson(doc3))
          }
        }
      }
    }
  }

  "Response to failed query" should {
    @inline def shouldMatch(r: Response, msg: String) =
      r.error aka "error" must beSome[DatabaseException].which { err =>
        err.message aka "message" must_=== msg
      }

    "be expected MkResponseError" in {
      shouldMatch(MongoDB.mkQueryError(), "Fails to create response")
    }

    "be expected error #1" in {
      MongoDB
        .queryError(channelId(), "Error #1")
        .aka("response") must beSuccessfulTry.which(shouldMatch(_, "Error #1"))
    }
  }

  "Write operator" should {
    "be delete" in {
      MongoDB.writeOp(Delete("db.col", 0)) aka "parsed op" must beSome[WriteOp]
        .which(op =>
          op aka "write op" must_=== DeleteOp and (op must beLike {
            case DeleteOp => ok // check pattern matching
          })
        )
    }

    "be insert" in {
      MongoDB.writeOp(Insert(1, "db.col")) aka "parsed op" must beSome[WriteOp]
        .which(op =>
          op aka "write op" must_=== InsertOp and (op must beLike {
            case InsertOp => ok // check pattern matching
          })
        )
    }

    "be update" in {
      MongoDB.writeOp(Update("db.col", 2)) aka "parsed op" must beSome[WriteOp]
        .which(op =>
          op aka "write op" must_=== UpdateOp and (op must beLike {
            case UpdateOp => ok // check pattern matching
          })
        )
    }
  }

  "Response to write failure" should {
    "contain expected BSON without code" in {
      MongoDB
        .writeError(channelId(), "Write Error #1")
        .aka("response") must beSuccessfulTry.which(
        parseResponse(_).toList aka "error" must beLike {
          case doc :: Nil => doc aka "error document" must_=== doc4
        }
      )
    }

    "contain expected BSON with code" in {
      MongoDB
        .writeError(channelId(), "Write Error #2", Some(3))
        .aka("response") must beSuccessfulTry.which(
        parseResponse(_).toList aka "error" must beLike {
          case doc :: Nil => doc aka "error document" must_=== doc5
        }
      )
    }
  }

  "Response to write success" should {
    "contain expected BSON when no existing document was updated" in {
      MongoDB.writeSuccess(channelId(), 1) aka "response" must beSuccessfulTry
        .which(parseResponse(_).toList aka "response" must beLike {
          case doc :: Nil => doc aka "success" must_=== doc6
        })
    }

    "contain expected BSON when existing document was updated" in {
      MongoDB
        .writeSuccess(channelId(), 2, true)
        .aka("response") must beSuccessfulTry.which(
        parseResponse(_).toList aka "response" must beLike {
          case doc :: Nil => doc aka "success" must_=== doc7
        }
      )
    }
  }
}

private[reactivemongo] trait MongoFixtures {
  import reactivemongo.api.bson.BSONDateTime

  val doc1 = BSONDocument("email" -> "test1@test.fr", "age" -> 3)

  val doc2 = BSONDocument("name" -> "Document #2", "price" -> 5.1D)

  val doc3 = BSONDocument(
    "title" -> "Title",
    "modified" -> BSONDateTime(System.currentTimeMillis)
  )

  val doc4 = BSONDocument(
    "ok" -> 0,
    "err" -> "Write Error #1",
    "errmsg" -> "Write Error #1",
    "code" -> -1,
    "updatedExisting" -> false,
    "n" -> 0
  )

  val doc5 = BSONDocument(
    "ok" -> 0,
    "err" -> "Write Error #2",
    "errmsg" -> "Write Error #2",
    "code" -> 3,
    "updatedExisting" -> false,
    "n" -> 0
  )

  val doc6 = BSONDocument("ok" -> 1, "updatedExisting" -> false, "n" -> 1)
  val doc7 = BSONDocument("ok" -> 1, "updatedExisting" -> true, "n" -> 2)

  @inline def bson(d: BSONDocument) = d.elements.toList
}
