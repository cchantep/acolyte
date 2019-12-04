package acolyte.reactivemongo

import reactivemongo.io.netty.channel.DefaultChannelId
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

  @inline def channelId() = DefaultChannelId.newInstance()

  "Empty handler" should {
    "not respond to any query" in {
      ConnectionHandler.empty aka "connection handler" must beLike {
        case h ⇒ h.queryHandler(channelId(), query1).
          aka("query result") must beNone and (
            h.writeHandler(channelId(), write1._1, write1._2).
            aka("write result") must beNone)
      }
    }
  }

  "Handler with query handler" should {
    "return some query result" in {
      val h = implicitly[QueryHandler] {
        _: Request ⇒ QueryResponse(BSONDocument("a" → "b"))
      }
      val handler = ConnectionHandler.empty

      ConnectionHandler(h).queryHandler(channelId(), query1).
        aka("query result #1") must beSome and (
          handler.queryHandler(channelId(), query1) aka "query result #2" must beNone).and(handler.withQueryHandler(h).queryHandler(channelId(), query1).
            aka("query result #3") must beSome)
    }

    "return no query result" in {
      ConnectionHandler({
        _: Request ⇒ QueryResponse(None)
      }).queryHandler(channelId(), query1) aka "query result" must beNone
    }

    "be combined with orElse #1" in {
      ConnectionHandler({
        _: Request ⇒ QueryResponse(BSONDocument("a" → "b"))
      }).orElse(ConnectionHandler { _: Request ⇒ QueryResponse.empty }).
        queryHandler(channelId(), query1) aka "query result" must beSome.which(
          _ aka "response" must beResponse {
            case ValueDocument(("a", BSONString("b")) :: Nil) :: Nil ⇒ ok
          })
    }

    "be combined with orElse #2" in {
      ConnectionHandler({ _: Request ⇒ QueryResponse.empty }).
        orElse(ConnectionHandler { _: Request ⇒
          QueryResponse(BSONDocument("a" → "b"))
        }).queryHandler(channelId(), query1) aka "query result" must beSome.which(
          _ aka "response" must beResponse { case res if res.isEmpty ⇒ ok })
    }
  }

  "Handler with write handler" should {
    "return some write error" in {
      val h = implicitly[WriteHandler] {
        (_: WriteOp, _: Request) ⇒ WriteResponse("Error #1" → 8)
      }
      val handler = ConnectionHandler()

      ConnectionHandler(writeHandler = h).writeHandler(
        channelId(), write1._1, write1._2).
        aka("write result #1") must beSome and (
          handler.writeHandler(channelId(), write1._1, write1._2).
          aka("write result #2") must beNone) and (
            handler.withWriteHandler(h).
            writeHandler(channelId(), write1._1, write1._2).
            aka("write result #3") must beSome)
    }

    "return no write result" in {
      ConnectionHandler(writeHandler = implicitly[WriteHandler] {
        (_: WriteOp, _: Request) ⇒ WriteResponse(None)
      }).writeHandler(channelId(), write1._1, write1._2).
        aka("write result") must beNone
    }

    "be combined using orElse" in {
      val handler: ConnectionHandler = ConnectionHandler({
        _: Request ⇒ QueryResponse(BSONDocument("a" → "b"))
      })

      ConnectionHandler(writeHandler = {
        (_: WriteOp, _: Request) ⇒ WriteResponse.successful(1, false)
      })

      handler.queryHandler(channelId(), query1) aka "query result" must beSome.which(
        _ aka "response" must beResponse {
          case ValueDocument(("a", BSONString("b")) :: Nil) :: Nil ⇒ ok
        })
    }
  }

  "Complete handler" should {
    "return expected query result #1" in {
      chandler1.queryHandler(channelId(), query1) aka "query result" must beSome.which(
        _ aka "response" must beResponse {
          case ValueDocument(("b", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
        })
    }

    "return expected query result #2" in {
      chandler1.queryHandler(channelId(), query2) aka "query result" must beSome.which(
        _ aka "response" must beResponse {
          case ValueDocument(("d", BSONDouble(4.56d)) :: Nil) ::
            ValueDocument(("ef", BSONString("ghi")) :: Nil) :: Nil ⇒ ok
        })
    }

    "return no query result" in {
      chandler1.queryHandler(channelId(), query3).
        aka("query handler") must beNone
    }

    "return expected write result #1" in {
      chandler1.writeHandler(channelId(), write1._1, write1._2).
        aka("write result") must beSome.which(
          _ aka "response" must beWriteError("Error #2"))
    }

    "return expected write result #2" in {
      chandler1.writeHandler(channelId(), write2._1, write2._2).
        aka("write result") must beSome.which(_ aka "response" must beResponse {
          case ValueDocument(("ok", BSONInteger(1)) ::
            ("updatedExisting", BSONBoolean(false)) ::
            ("n", BSONInteger(0)) :: Nil) :: Nil ⇒ ok
        })
    }

    "return no write result" in {
      chandler1.writeHandler(channelId(), write3._1, write3._2).
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
      Seq(BSONDocument("d" → 4.56d), BSONDocument("ef" → "ghi")))
    case _ ⇒ QueryResponse(None)
  }, WriteHandler {
    case (DeleteOp, _) ⇒ WriteResponse("Error #2")
    case (InsertOp, _) ⇒ WriteResponse({})
    case _             ⇒ WriteResponse(None)
  })
}
