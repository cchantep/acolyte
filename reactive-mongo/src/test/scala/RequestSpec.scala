package acolyte.reactivemongo

import reactivemongo.api.bson.{
  BSONArray,
  BSONDocument,
  BSONDouble,
  BSONInteger,
  BSONString
}

final class RequestSpec extends org.specs2.mutable.Specification
  with RequestFixtures {

  "Request" title

  "Collection name" should {
    "match" >> {
      "request #1" in {
        request1 aka "request" must beLike {
          case Request("db1.col1", _) => ok
        }
      }

      "request #2" in {
        request2 aka "request" must beLike {
          case Request("db1.col2", _) => ok
        }
      }
    }

    "not match" >> {
      "request #1" in {
        request1 aka "request" must not(
          beLike { case Request("db1.col2", _) => ok })
      }

      "request #2" in {
        request2 aka "request" must not(
          beLike { case Request("db1.col1", _) => ok })
      }
    }

    "be extracted" >> {
      "request #1" in {
        request1 aka "request" must beLike {
          case Request(n, _) => n aka "collection name" must_=== "db1.col1"
        }
      }

      "request #2" in {
        request2 aka "request" must beLike {
          case Request(n, _) => n aka "collection name" must_=== "db1.col2"
        }
      }
    }
  }

  "Request properties" should {
    "be extracted from request #1" in {
      request1 aka "request" must beLike {
        case Request("db1.col1", SimpleBody(props)) =>
          props aka "properties" must_=== List(
            "email" -> BSONString("em@il.net"), "age" -> BSONInteger(11))
      }
    }

    "be extracted from request #2" in {
      request2 aka "request" must beLike {
        case Request("db1.col2", SimpleBody(props)) =>
          props aka "properties" must_=== List(
            "email" -> BSONString("em@il.net"),
            "age" -> BSONDocument(
              "meta" -> BSONString("y"), "$gt" -> BSONInteger(10)),
            "priority" -> BSONDouble(0.25))
      }
    }
  }

  "Document properties" should {
    "be extracted from doc #1" in {
      doc1 aka "document" must beLike {
        case ValueDocument(props) => props aka "properties" must_=== List(
          "email" -> BSONString("em@il.net"), "age" -> BSONInteger(11))
      }
    }

    "be extracted from doc #2" in {
      doc2 aka "document" must beLike {
        case ValueDocument(props) => props aka "properties" must_=== List(
          "email" -> BSONString("em@il.net"),
          "age" -> BSONDocument(
            "meta" -> BSONString("y"), "$gt" -> BSONInteger(10)),
          "priority" -> BSONDouble(0.25))
      }
    }
  }

  "Ordered property list" should {
    "match exactly 2 properties" in {
      request1 aka "request #1" must beLike {
        case Request("db1.col1", SimpleBody(
          ("email", BSONString("em@il.net")) ::
            ("age", BSONInteger(11)) :: Nil)) => ok
      }
    }

    "be extracted as BSON values for exactly 2 properties" in {
      request1 aka "request #1" must beLike {
        case Request(col, SimpleBody((k1, v1) :: (k2, v2) :: Nil)) =>
          col aka "collection" must_=== "db1.col1" and (
            k1 aka "key #1" must_=== "email") and (
              v1 aka "value #1" must_=== BSONString("em@il.net")) and (
                k2 aka "key #2" must_=== "age") and (
                  v2 aka "value #2" must_=== BSONInteger(11))
      }
    }

    "be extracted as Scala values for exactly 2 properties" in {
      request1 aka "request #1" must beLike {
        case Request(col, SimpleBody(
          (k1, BSONString(v1)) :: (k2, BSONInteger(v2)) :: Nil)) =>
          col aka "collection" must_=== "db1.col1" and (
            k1 aka "key #1" must_=== "email") and (
              v1 aka "value #1" must_=== "em@il.net") and (
                k2 aka "key #2" must_=== "age") and (
                  v2 aka "value #2" must_=== 11)
      }
    }

    "match with nested document" in {
      request2 aka "request #1" must beLike {
        case Request("db1.col2", SimpleBody(
          ("email", BSONString("em@il.net")) ::
            ("age", ValueDocument(("meta", BSONString("y")) ::
              ("$gt", BSONInteger(10)) :: Nil)) ::
            ("priority", BSONDouble(0.25D)) :: Nil)) => ok
      }
    }

    "be extracted as BSON values from nested document" in {
      request2 aka "request #1" must beLike {
        case Request("db1.col2", SimpleBody(("email", email) ::
          ("age", ValueDocument(("meta", meta) :: ("$gt", gt) :: Nil)) ::
          ("priority", prio) :: Nil)) =>
          email aka "email" must_=== BSONString("em@il.net") and (
            meta aka "meta" must_=== BSONString("y")) and (
              gt aka "gt" must_=== BSONInteger(10)) and (
                prio aka "priority" must_=== BSONDouble(0.25D))
      }
    }

    "be extracted as Scala values from nested document" in {
      request2 aka "request #1" must beLike {
        case Request("db1.col2", SimpleBody(("email", BSONString(email)) ::
          ("age", ValueDocument(("meta", BSONString(meta)) ::
            ("$gt", BSONInteger(gt)) :: Nil)) ::
          ("priority", BSONDouble(prio)) :: Nil)) =>
          email aka "email" must_=== "em@il.net" and (
            meta aka "meta" must_=== "y") and (gt aka "gt" must_=== 10) and (
              prio aka "priority" must_=== 0.25D)
      }
    }
  }

  "Unordered property list" should {
    val Age = Property("age")
    val Email = Property("email")
    val Meta = Property("meta")
    val Greater = Property("$gt")

    "match 'email' property" >> {
      "on request #1" in {
        request1 aka "request" must beLike {
          case Request("db1.col1", SimpleBody(
            Email(BSONString("em@il.net")))) => ok
        }
      }

      "on request #2" in {
        request2 aka "request" must beLike {
          case Request("db1.col2", SimpleBody(
            Email(BSONString("em@il.net")))) => ok
        }
      }
    }

    "extract 'email' property as BSON value" >> {
      "on request #1" in {
        request1 aka "request" must beLike {
          case Request("db1.col1", SimpleBody(
            Email(email))) =>
            email aka "email" must_=== BSONString("em@il.net")
        }
      }

      "on request #2" in {
        request2 aka "request" must beLike {
          case Request("db1.col2", SimpleBody(Email(email))) =>
            email aka "email" must_=== BSONString("em@il.net")
        }
      }
    }

    "extract 'email' property as Scala value" >> {
      "on request #1" in {
        request1 aka "request" must beLike {
          case Request("db1.col1", SimpleBody(
            Email(BSONString(email)))) =>
            email aka "email" must_=== "em@il.net"
        }
      }

      "on request #2" in {
        request2 aka "request" must beLike {
          case Request("db1.col2", SimpleBody(
            Email(BSONString(email)))) =>
            email aka "email" must_=== "em@il.net"
        }
      }
    }

    "match 'email' & 'age' properties" >> {
      "on request #1 in same order" in {
        request1 aka "request #1" must beLike {
          case Request("db1.col1", SimpleBody(
            Email(BSONString("em@il.net")) &
              Age(BSONInteger(11)))) => ok

        }
      }

      "on request #1 in reverse order" in {
        request1 aka "request #1" must beLike {
          case Request("db1.col1", SimpleBody(
            Age(BSONInteger(11)) &
              Email(BSONString("em@il.net")))) => ok

        }
      }

      "on request #2 in same order" in {
        request2 aka "request #2" must beLike {
          case Request("db1.col2", SimpleBody(
            Email(BSONString("em@il.net")) &
              Age(ValueDocument(
                Greater(BSONInteger(10)))))) => ok

        }
      }

      "on request #2 in reverse order" in {
        request2 aka "request #2" must beLike {
          case Request("db1.col2", SimpleBody(
            Age(ValueDocument(
              Greater(BSONInteger(10)))) &
              Email(BSONString("em@il.net")))) => ok

        }
      }
    }

    "not match 'email' & 'age' properties" >> {
      "on request #2 in same order with different 'age' type" in {
        request2 aka "request #2" must not(beLike {
          case Request("db1.col2", SimpleBody(
            Email(BSONString("em@il.net")) &
              Age(BSONInteger(11)))) => ok

        })
      }

      "on request #2 in reverse order with different 'email' type" in {
        request2 aka "request #2" must not(beLike {
          case Request("db1.col2", SimpleBody(Age(_) &
            Email(BSONInteger(_)))) => ok

        })
      }
    }

    "extract 'age' properties as BSON values" >> {
      "on request #2 in same order" in {
        request2 aka "request" must beLike {
          case Request("db1.col2", SimpleBody(Age(ValueDocument(
            Meta(meta) & Greater(gt))))) =>

            gt aka "gt" must_=== BSONInteger(10) and (
              meta aka "meta" must_=== BSONString("y"))
        }
      }

      "on request #2 in reverse order" in {
        request2 aka "request" must beLike {
          case Request("db1.col2", SimpleBody(Age(ValueDocument(
            Greater(gt) & Meta(meta))))) =>

            gt aka "gt" must_=== BSONInteger(10) and (
              meta aka "meta" must_=== BSONString("y"))
        }
      }
    }

    // ---

    "extract 'age' properties as Scala values" >> {
      "on request #2 in same order" in {
        request2 aka "request" must beLike {
          case Request("db1.col2", SimpleBody(Age(ValueDocument(
            Meta(BSONString(meta)) &
              Greater(BSONInteger(gt)))))) =>

            gt aka "gt" must_=== 10 and (meta aka "meta" must_=== "y")
        }
      }

      "on request #2 in reverse order" in {
        request2 aka "request" must beLike {
          case Request("db1.col2", SimpleBody(Age(ValueDocument(
            Greater(BSONInteger(gt)) &
              Meta(BSONString(meta)))))) =>

            gt aka "gt" must_=== 10 and (meta aka "meta" must_=== "y")
        }
      }
    }
  }

  "Count request" should {
    "be extracted" in {
      count1 aka "request" must beLike {
        case CountRequest("col3", ("fil", BSONString("ter")) :: Nil) => ok
      }
    }
  }

  "Find and modify request" should {
    "be extracted" in {
      findAndModify1 aka "request" must beLike {
        case FindAndModifyRequest("col3",
          List(("_id", BSONInteger(1))),
          List(("$set", ValueDocument(List(("foo", BSONString("bar")))))),
          List(("limit", BSONInteger(2)))
          ) => ok
      }
    }
  }

  "Aggregate request" should {
    "be extracted" in {
      aggregate1 aka "request" must beLike {
        case AggregateRequest("col5",
          List(ValueDocument(("$match",
            ValueDocument(("category", BSONString("A")) :: Nil)) :: Nil)),
          ("cursor", ValueDocument(
            ("batchSize", BSONInteger(123)) :: Nil)) :: Nil) =>
          ok
      }
    }
  }

  "Array" should {
    "be extracted as list values" in {
      BSONArray("a", 2, 3.45d) aka "array" must beLike {
        case ValueList(BSONString(_) :: BSONInteger(_) ::
          BSONDouble(_) :: Nil) => ok
      }
    }

    "be extracted from an $in clause" in {
      BSONDocument("selector" -> BSONDocument("$in" -> BSONArray("A", "B"))).
        aka("body") must beLike {
          case ValueDocument(("selector", InClause(
            BSONString("A") :: BSONString("B") :: Nil)) :: Nil) => ok
        }
    }

    "be extracted from an $nin clause" in {
      BSONDocument("selector" -> BSONDocument("$nin" -> BSONArray("A", "B"))).
        aka("body") must beLike {
          case ValueDocument(("selector", NotInClause(
            BSONString("A") :: BSONString("B") :: Nil)) :: Nil) => ok
        }
    }
  }

  "Update" should {
    "be extracted with pipeline" in {
      UpdateRequest.unapply(UpdateOp -> update2) must beSome.like {
        case ("db1.col5",
          ("_id", BSONString("foo")) :: Nil,
          ("$set", ValueDocument(
            ("status", ValueDocument(
              ("$cond", ValueDocument(
                ("if", ValueDocument(
                  ("status", BSONString("Pending")) :: Nil)) :: (
                  "then", BSONString("Assigned")) :: (
                  "else", BSONString("$status")) :: Nil
                )) :: Nil
              ))
              :: Nil
            )) :: Nil,
          false, false) =>
          ok
      }
    }
  }

  "Multiple document body" should {
    "be extracted" in {
      update1 aka "update request" must beLike {
        case Request(_, RequestBody(List(("sel", BSONString("hector"))) ::
          List(("updated", BSONString("property"))) :: Nil)) => ok
      }
    }

    "be pretty-printed" in {
      Request.pretty(update1) aka "representation" must beTypedEqualTo(
        s"""Request(db1.col4, [ {
  'sel': 'hector'
}, {
  'updated': 'property'
} ])""")
    }
  }
}

sealed trait RequestFixtures {
  val doc1 = BSONDocument("email" -> "em@il.net", "age" -> 11)
  val request1 = new Request {
    val collection = "db1.col1"
    val body = List(doc1)
  }

  val doc2 = BSONDocument(
    "email" -> "em@il.net",
    "age" -> BSONDocument("meta" -> "y", f"$$gt" -> 10),
    "priority" -> 0.25D)

  val request2 = new Request {
    val collection = "db1.col2"
    val body = List(doc2)
  }

  val count1 = new Request {
    val collection = f"db1.$$cmd"
    val body = List(BSONDocument(
      "count" -> "col3",
      "query" -> BSONDocument("fil" -> "ter")))
  }

  val findAndModify1 = new Request {
    val collection = f"db1.$$cmd"
    val body = List(BSONDocument(
      "findAndModify" -> "col3",
      "update" -> BSONDocument(f"$$set" -> BSONDocument("foo" -> "bar")),
      "query" -> BSONDocument("_id" -> 1),
      "limit" -> 2))
  }

  val update1 = new Request {
    val collection = "db1.col4"
    val body = List(
      BSONDocument("sel" -> "hector"),
      BSONDocument("updated" -> "property"))
  }

  val update2 = new Request {
    val collection = "db1.col5"
    val body = List(
      BSONDocument(
        "q" -> BSONDocument("_id" -> "foo"),
        "u" -> BSONArray(
          BSONDocument(
            f"$$set" -> BSONDocument(
              "status" -> BSONDocument(
                f"$$cond" -> BSONDocument(
                  "if" -> BSONDocument("status" -> "Pending"),
                  "then" -> "Assigned",
                  "else" -> f"$$status"))))),
        "upsert" -> false,
        "multi" -> false))
  }

  val aggregate1 = new Request {
    val collection = f"db1.$$cmd"
    val body = List(BSONDocument(
      "aggregate" -> "col5",
      "pipeline" -> List(
        BSONDocument(f"$$match" -> BSONDocument("category" -> "A"))),
      "cursor" -> BSONDocument("batchSize" -> 123)))
  }
}
