package acolyte.reactivemongo

import reactivemongo.io.netty.channel.DefaultChannelId
import reactivemongo.bson.{ BSONDocument, BSONBoolean, BSONInteger, BSONString }

class WriteHandlerSpec extends org.specs2.mutable.Specification
  with ResponseMatchers with WriteHandlerFixtures {

  "Write handler" title

  @inline def channelId() = DefaultChannelId.newInstance()

  "Handler" should {
    "return a success response with existing document updated" in {
      implicitly[WriteHandler]({
        (_: WriteOp, _: Request) ⇒ WriteResponse(1 → true)
      }) aka "write handler" must beLike {
        case h ⇒ h(channelId(), write1._1, write1._2) must beSome.which(
          _ aka "result" must beResponse {
            _ aka "response" must beWriteSuccess(1, true)
          })
      }
    }

    "return a success response without existing document updated" in {
      implicitly[WriteHandler]({
        (_: WriteOp, _: Request) ⇒ WriteResponse(0 → false)
      }) aka "write handler" must beLike {
        case h ⇒ h(channelId(), write1._1, write1._2) must beSome.which(
          _ aka "result" must beResponse {
            _ aka "response" must beWriteSuccess(0, false)
          })
      }
    }

    "return a success response with unit" in {
      implicitly[WriteHandler]({
        (_: WriteOp, _: Request) ⇒ WriteResponse({})
      }) aka "write handler" must beLike {
        case h ⇒ h(channelId(), write1._1, write1._2) must beSome.which(
          _ aka "result" must beResponse {
            _ aka "response" must beWriteSuccess(0, false)
          })
      }
    }

    "return an error response" in {
      implicitly[WriteHandler]({ (_: WriteOp, _: Request) ⇒
        WriteResponse("Error message #1")
      }) aka "write handler" must beLike {
        case h ⇒ h(channelId(), write1._1, write1._2) must beSome.which(
          _ aka "response" must beWriteError("Error message #1"))
      }
    }

    "return an error with code" in {
      implicitly[WriteHandler]({ (_: WriteOp, _: Request) ⇒
        WriteResponse("Error message #2" → 7)
      }) aka "write handler" must beLike {
        case h ⇒ h(channelId(), write1._1, write1._2) must beSome.which(
          _ aka "response" must beWriteError("Error message #2", Some(7)))
      }
    }

    "return no response" in {
      implicitly[WriteHandler]({ (_: WriteOp, _: Request) ⇒
        WriteResponse(None)
      }) aka "write handler" must beLike {
        case h ⇒ h(channelId(), write1._1, write1._2) must beNone
      }
    }

    "be combined using orElse" in {
      WriteHandler { (_, _) ⇒ WriteResponse(1 → true) }.
        orElse(WriteHandler.empty) must beLike {
          case h ⇒ h(channelId(), write1._1, write1._2) must beSome.which(
            _ aka "response" must beResponse {
              _ aka "response" must beWriteSuccess(1, true)
            })
        }
    }
  }

  "Empty handler" should {
    "return no response" in {
      WriteHandler.empty aka "write handler" must beLike {
        case h ⇒ h(channelId(), write1._1, write1._2) must beNone
      }
    }

    "be combined using orElse" in {
      WriteHandler.empty.orElse(
        WriteHandler { (_, _) ⇒ WriteResponse(1 → true) }) must beLike {
          case h ⇒ h(channelId(), write1._1, write1._2) must beSome.which(
            _ aka "response" must beResponse {
              _ aka "response" must beWriteSuccess(1, true)
            })
        }
    }
  }

  "Mixed handler" should {
    val handler = WriteHandler { (op, req) ⇒
      (op, req) match {
        case (DeleteOp, Request("test1", _)) ⇒ WriteResponse.undefined
        case (InsertOp, Request("test2", _)) ⇒ WriteResponse("Error #2")
        case (UpdateOp, Request("test3", _)) ⇒ WriteResponse(2 → true)
        case (_, Request("test4", _))        ⇒ WriteResponse.successful()
      }
    }

    "return no response" in {
      handler(channelId(), write1._1, write1._2) aka "prepared" must beNone
    }

    "return an error response" in {
      handler(channelId(), write2._1, write2._2) aka "prepared" must beSome.which(
        _ aka "write response" must beWriteError("Error #2"))
    }

    "return an success response" in {
      handler(channelId(), write3._1, write3._2) aka "prepared" must beSome.which(
        _ aka "write response" must beResponse {
          case ValueDocument(("ok", BSONInteger(1)) ::
            ("updatedExisting", BSONBoolean(true)) ::
            ("n", BSONInteger(2)) :: Nil) :: Nil ⇒ ok
        })
    }
  }

  "Convenient extractor" should {
    "handle insert" in {
      WriteHandler { (op, req) ⇒
        (op, req) match {
          case InsertRequest("col1",
            ("a", BSONInteger(1)) :: ("b", BSONBoolean(true)) :: Nil) ⇒

            WriteResponse.successful(1, false)
          case _ ⇒ WriteResponse.failed("Unexpected")
        }
      } apply (channelId(), InsertOp, new Request {
        val collection = "col1"
        val body = List(BSONDocument("a" → 1, "b" → true))
      }) aka "prepared" must beSome.which(
        _ aka "result" must beResponse(
          _ aka "response" must beWriteSuccess(1, false)))
    }

    "handle update" in {
      WriteHandler { (op, req) ⇒
        (op, req) match {
          case UpdateRequest("col2", ("id", BSONString("id1")) :: Nil,
            ("a", BSONInteger(1)) :: ("b", BSONBoolean(true)) :: Nil) ⇒
            WriteResponse.successful(1, true)

          case _ ⇒ WriteResponse.failed("Unexpected")
        }
      } apply (channelId(), UpdateOp, new Request {
        val collection = "col2"
        val body = List(
          BSONDocument("id" → "id1"),
          BSONDocument("a" → 1, "b" → true))
      }) aka "prepared" must beSome.which(
        _ aka "result" must beResponse(
          _ aka "response" must beWriteSuccess(1, true)))
    }

    "handle delete" in {
      WriteHandler { (op, req) ⇒
        (op, req) match {
          case DeleteRequest("col3", ("name", BSONString("xyz")) :: Nil) ⇒
            WriteResponse.successful(2, true)

          case _ ⇒ WriteResponse.failed("Unexpected")
        }
      } apply (channelId(), DeleteOp, new Request {
        val collection = "col3"
        val body = List(BSONDocument("name" → "xyz"))
      }) aka "prepared" must beSome.which(
        _ aka "result" must beResponse(
          _ aka "response" must beWriteSuccess(2, true)))
    }
  }
}

trait WriteHandlerFixtures {
  val write1 = (DeleteOp, new Request {
    val collection = "test1"
    val body = List(BSONDocument("filter" → "valA"))
  })

  val write2 = (InsertOp, new Request {
    val collection = "test2"
    val body = List(BSONDocument("filter" → "valB"))
  })

  val write3 = (UpdateOp, new Request {
    val collection = "test3"
    val body = List(BSONDocument("filter" → "valC"))
  })
}
