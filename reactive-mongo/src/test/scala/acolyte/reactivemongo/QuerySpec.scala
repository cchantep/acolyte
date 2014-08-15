package acolyte.reactivemongo

import reactivemongo.bson.{ BSONDocument, BSONDouble, BSONInteger, BSONString }

object QuerySpec extends org.specs2.mutable.Specification with QueryFixtures {
  "Query" title

  "Collection name" should {
    "match" >> {
      "query #1" in {
        query1 aka "query" must beLike { case QueryBody("db1.col1", _) ⇒ ok }
      }

      "query #2" in {
        query2 aka "query" must beLike { case QueryBody("db1.col2", _) ⇒ ok }
      }
    }

    "not match" >> {
      "query #1" in {
        query1 aka "query" must not(
          beLike { case QueryBody("db1.col2", _) ⇒ ok })
      }

      "query #2" in {
        query2 aka "query" must not(
          beLike { case QueryBody("db1.col1", _) ⇒ ok })
      }
    }

    "be extracted" >> {
      "query #1" in {
        query1 aka "query" must beLike {
          case QueryBody(n, _) ⇒ n aka "collection name" must_== "db1.col1"
        }
      }

      "query #2" in {
        query2 aka "query" must beLike {
          case QueryBody(n, _) ⇒ n aka "collection name" must_== "db1.col2"
        }
      }
    }
  }

  "Query properties" should {
    "be extracted from query #1" in {
      query1 aka "query" must beLike {
        case QueryBody("db1.col1", props) ⇒
          props aka "properties" must_== List(
            "email" -> BSONString("em@il.net"), "age" -> BSONInteger(11))
      }
    }

    "be extracted from query #2" in {
      query2 aka "query" must beLike {
        case QueryBody("db1.col2", props) ⇒
          props aka "properties" must_== List(
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
        case ValueDocument(props) ⇒ props aka "properties" must_== List(
          "email" -> BSONString("em@il.net"), "age" -> BSONInteger(11))
      }
    }

    "be extracted from doc #2" in {
      doc2 aka "document" must beLike {
        case ValueDocument(props) ⇒ props aka "properties" must_== List(
          "email" -> BSONString("em@il.net"),
          "age" -> BSONDocument(
            "meta" -> BSONString("y"), "$gt" -> BSONInteger(10)),
          "priority" -> BSONDouble(0.25))
      }
    }
  }

  "Ordered property list" should {
    "match exactly 2 properties" in {
      query1 aka "query #1" must beLike {
        case QueryBody("db1.col1",
          ("email", BSONString("em@il.net")) ::
            ("age", BSONInteger(11)) :: Nil) ⇒ ok
      }
    }

    "be extracted as BSON values for exactly 2 properties" in {
      query1 aka "query #1" must beLike {
        case QueryBody(col, (k1, v1) :: (k2, v2) :: Nil) ⇒
          col aka "collection" must_== "db1.col1" and (
            k1 aka "key #1" must_== "email") and (
              v1 aka "value #1" must_== BSONString("em@il.net")) and (
                k2 aka "key #2" must_== "age") and (
                  v2 aka "value #2" must_== BSONInteger(11))
      }
    }

    "be extracted as Scala values for exactly 2 properties" in {
      query1 aka "query #1" must beLike {
        case QueryBody(col,
          (k1, BSONString(v1)) :: (k2, BSONInteger(v2)) :: Nil) ⇒
          col aka "collection" must_== "db1.col1" and (
            k1 aka "key #1" must_== "email") and (
              v1 aka "value #1" must_== "em@il.net") and (
                k2 aka "key #2" must_== "age") and (
                  v2 aka "value #2" must_== 11)
      }
    }

    "match with nested document" in {
      query2 aka "query #1" must beLike {
        case QueryBody("db1.col2", ("email", BSONString("em@il.net")) ::
          ("age", ValueDocument(("meta", BSONString("y")) ::
            ("$gt", BSONInteger(10)) :: Nil)) ::
          ("priority", BSONDouble(0.25D)) :: Nil) ⇒ ok
      }
    }

    "be extracted as BSON values from nested document" in {
      query2 aka "query #1" must beLike {
        case QueryBody("db1.col2", ("email", email) ::
          ("age", ValueDocument(("meta", meta) :: ("$gt", gt) :: Nil)) ::
          ("priority", prio) :: Nil) ⇒
          email aka "email" must_== BSONString("em@il.net") and (
            meta aka "meta" must_== BSONString("y")) and (
              gt aka "gt" must_== BSONInteger(10)) and (
                prio aka "priority" must_== BSONDouble(0.25D))
      }
    }

    "be extracted as Scala values from nested document" in {
      query2 aka "query #1" must beLike {
        case QueryBody("db1.col2", ("email", BSONString(email)) ::
          ("age", ValueDocument(("meta", BSONString(meta)) ::
            ("$gt", BSONInteger(gt)) :: Nil)) ::
          ("priority", BSONDouble(prio)) :: Nil) ⇒
          email aka "email" must_== "em@il.net" and (
            meta aka "meta" must_== "y") and (gt aka "gt" must_== 10) and (
              prio aka "priority" must_== 0.25D)
      }
    }
  }

  "Unordered property list" should {
    "match 'email' property" >> {
      "on query #1" in {
        query1 aka "query" must beLike {
          case QueryBody("db1.col1",
            ~(Property("email"), BSONString("em@il.net"))) ⇒ ok
        }
      }

      "on query #2" in {
        query2 aka "query" must beLike {
          case QueryBody("db1.col2",
            ~(Property("email"), BSONString("em@il.net"))) ⇒ ok
        }
      }
    }

    "extract 'email' property as BSON value" >> {
      "on query #1" in {
        query1 aka "query" must beLike {
          case QueryBody("db1.col1", ~(Property("email"), email)) ⇒
            email aka "email" must_== BSONString("em@il.net")
        }
      }

      "on query #2" in {
        query2 aka "query" must beLike {
          case QueryBody("db1.col2", ~(Property("email"), email)) ⇒
            email aka "email" must_== BSONString("em@il.net")
        }
      }
    }

    "extract 'email' property as Scala value" >> {
      "on query #1" in {
        query1 aka "query" must beLike {
          case QueryBody("db1.col1", ~(Property("email"), BSONString(email))) ⇒
            email aka "email" must_== "em@il.net"
        }
      }

      "on query #2" in {
        query2 aka "query" must beLike {
          case QueryBody("db1.col2", ~(Property("email"), BSONString(email))) ⇒
            email aka "email" must_== "em@il.net"
        }
      }
    }

    "match 'email' & 'age' properties" >> {
      "on query #1 in same order" in {
        query1 aka "query #1" must beLike {
          case QueryBody("db1.col1",
            ~(Property("email"), BSONString("em@il.net")) &
              ~(Property("age"), BSONInteger(11))) ⇒ ok

        }
      }

      "on query #1 in reverse order" in {
        query1 aka "query #1" must beLike {
          case QueryBody("db1.col1", ~(Property("age"), BSONInteger(11)) &
            ~(Property("email"), BSONString("em@il.net"))) ⇒ ok

        }
      }

      "on query #2 in same order" in {
        query2 aka "query #2" must beLike {
          case QueryBody("db1.col2",
            ~(Property("email"), BSONString("em@il.net")) &
              ~(Property("age"), ValueDocument(
                ~(Property("$gt"), BSONInteger(10))))) ⇒ ok

        }
      }

      "on query #2 in reverse order" in {
        query2 aka "query #2" must beLike {
          case QueryBody("db1.col2", ~(Property("age"), ValueDocument(
            ~(Property("$gt"), BSONInteger(10)))) &
            ~(Property("email"), BSONString("em@il.net"))) ⇒ ok

        }
      }
    }

    "not match 'email' & 'age' properties" >> {
      "on query #2 in same order with different 'age' type" in {
        query2 aka "query #2" must not(beLike {
          case QueryBody("db1.col2",
            ~(Property("email"), BSONString("em@il.net")) &
              ~(Property("age"), BSONInteger(11))) ⇒ ok

        })
      }

      "on query #2 in reverse order with different 'email' type" in {
        query2 aka "query #2" must not(beLike {
          case QueryBody("db1.col2", ~(Property("age"), _) &
            ~(Property("email"), BSONInteger(_))) ⇒ ok

        })
      }
    }

    "extract 'age' properties as BSON values" >> {
      "on query #2 in same order" in {
        query2 aka "query" must beLike {
          case QueryBody("db1.col2", ~(Property("age"), ValueDocument(
            ~(Property("meta"), meta) & ~(Property("$gt"), gt)))) ⇒

            gt aka "gt" must_== BSONInteger(10) and (
              meta aka "meta" must_== BSONString("y"))
        }
      }

      "on query #2 in reverse order" in {
        query2 aka "query" must beLike {
          case QueryBody("db1.col2", ~(Property("age"), ValueDocument(
            ~(Property("$gt"), gt) & ~(Property("meta"), meta)))) ⇒

            gt aka "gt" must_== BSONInteger(10) and (
              meta aka "meta" must_== BSONString("y"))
        }
      }
    }

    // ---

    "extract 'age' properties as Scala values" >> {
      "on query #2 in same order" in {
        query2 aka "query" must beLike {
          case QueryBody("db1.col2", ~(Property("age"), ValueDocument(
            ~(Property("meta"), BSONString(meta)) &
              ~(Property("$gt"), BSONInteger(gt))))) ⇒

            gt aka "gt" must_== 10 and (meta aka "meta" must_== "y")
        }
      }

      "on query #2 in reverse order" in {
        query2 aka "query" must beLike {
          case QueryBody("db1.col2", ~(Property("age"), ValueDocument(
            ~(Property("$gt"), BSONInteger(gt)) &
              ~(Property("meta"), BSONString(meta))))) ⇒

            gt aka "gt" must_== 10 and (meta aka "meta" must_== "y")
        }
      }
    }
  }
}

sealed trait QueryFixtures {
  val doc1 = BSONDocument("email" -> "em@il.net", "age" -> 11)
  val query1 = new Query {
    val collection = "db1.col1"
    val body = doc1
    override val toString = "<query1>"
  }

  val doc2 = BSONDocument(
    "email" -> "em@il.net",
    "age" -> BSONDocument("meta" -> "y", "$gt" -> 10),
    "priority" -> 0.25D)

  val query2 = new Query {
    val collection = "db1.col2"
    val body = doc2
    override val toString = "<query2>"
  }
}
