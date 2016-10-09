package acolyte.reactivemongo

import reactivemongo.bson.{
  BSONBoolean,
  BSONDocument,
  BSONDouble,
  BSONInteger,
  BSONString
}

class ConnectionHandlerSpec extends org.specs2.mutable.Specification
    with QueryHandlerFixtures with WriteHandlerFixtures
    with ConnectionHandlerFixtures with ResponseMatchers {

  "Connection handler" title

  "Empty handler" should {
    "not respond to any query" in {
      ConnectionHandler.empty aka "connection handler" must beLike {
        case h ⇒
          h.queryHandler(1, query1) aka "query result" must beNone and (
            h.writeHandler(2, write1._1, write1._2).
            aka("write result") must beNone
          )
      }
    }
  }

  "Handler with query handler" should {
    "return some query result" in {
      val h = implicitly[QueryHandler] {
        _: Request ⇒ QueryResponse(BSONDocument("a" → "b"))
      }
      val handler = ConnectionHandler.empty

      ConnectionHandler(h).queryHandler(1, query1).
        aka("query result #1") must beSome and (
          handler.queryHandler(1, query1) aka "query result #2" must beNone
        ).
          and(handler.withQueryHandler(h).queryHandler(1, query1).
            aka("query result #3") must beSome)
    }

    "return no query result" in {
      ConnectionHandler({
        _: Request ⇒ QueryResponse(None)
      }).queryHandler(1, query1) aka "query result" must beNone
    }
  }

  "Handler with write handler" should {
    "return some write error" in {
      val h = implicitly[WriteHandler] {
        (_: WriteOp, _: Request) ⇒ WriteResponse("Error #1" → 8)
      }
      val handler = ConnectionHandler()

      ConnectionHandler(writeHandler = h).writeHandler(
        1, write1._1, write1._2
      ) aka "write result #1" must beSome and (
          handler.writeHandler(1, write1._1, write1._2).
          aka("write result #2") must beNone
        ) and (
            handler.withWriteHandler(h).writeHandler(1, write1._1, write1._2).
            aka("write result #3") must beSome
          )
    }

    "return no write result" in {
      ConnectionHandler(writeHandler = implicitly[WriteHandler] {
        (_: WriteOp, _: Request) ⇒ WriteResponse(None)
      }).writeHandler(1, write1._1, write1._2) aka "write result" must beNone
    }
  }

  "Complete handler" should {
    "return expected query result #1" in {
      chandler1.queryHandler(1, query1) aka "query result" must beSome.which(
        _ aka "response" must beResponse {
          case ValueDocument(("b", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
        }
      )
    }

    "return expected query result #2" in {
      chandler1.queryHandler(2, query2) aka "query result" must beSome.which(
        _ aka "response" must beResponse {
          case ValueDocument(("d", BSONDouble(4.56d)) :: Nil) ::
            ValueDocument(("ef", BSONString("ghi")) :: Nil) :: Nil ⇒ ok
        }
      )
    }

    "return no query result" in {
      chandler1.queryHandler(3, query3) aka "query handler" must beNone
    }

    "return expected write result #1" in {
      chandler1.writeHandler(1, write1._1, write1._2).
        aka("write result") must beSome.which(
          _ aka "response" must beWriteError("Error #2")
        )
    }

    "return expected write result #2" in {
      chandler1.writeHandler(2, write2._1, write2._2).
        aka("write result") must beSome.which(_ aka "response" must beResponse {
          case ValueDocument(("ok", BSONInteger(1)) ::
            ("updatedExisting", BSONBoolean(false)) ::
            ("n", BSONInteger(0)) :: Nil) :: Nil ⇒ ok
        })
    }

    "return no write result" in {
      chandler1.writeHandler(3, write3._1, write3._2).
        aka("write result") must beNone
    }
  }
}

trait ConnectionHandlerFixtures {
  fixtures: QueryHandlerFixtures with WriteHandlerFixtures ⇒

  lazy val chandler1 = ConnectionHandler(QueryHandler {
    case Request(col, _) if col.endsWith("test1") ⇒
      QueryResponse(BSONDocument("b" → 3))

    case Request(col, _) if col.endsWith("test2") ⇒ QueryResponse(
      Seq(BSONDocument("d" → 4.56d), BSONDocument("ef" → "ghi"))
    )
    case _ ⇒ QueryResponse(None)
  }, WriteHandler {
    case (DeleteOp, _) ⇒ WriteResponse("Error #2")
    case (InsertOp, _) ⇒ WriteResponse({})
    case _             ⇒ WriteResponse(None)
  })
}
