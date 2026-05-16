package acolyte.jdbc.mcp

final class QueryAnalyzerSpec extends org.specs2.mutable.Specification {
  "QueryAnalyzer".title

  "extract SELECT fields" in {
    QueryAnalyzer.analyzeQuery(
      "SELECT id, name FROM users"
    ) must beSuccessfulTry.which(
      _.selectFields must_=== List("id", "name")
    ) and {
      QueryAnalyzer.analyzeQuery("SELECT * FROM users") must beSuccessfulTry
        .which(_.selectFields must_=== List("*"))
    } and {
      QueryAnalyzer.analyzeQuery(
        "SELECT id, name, email FROM users"
      ) must beSuccessfulTry.which(_.selectFields.length must_=== 3)
    }
  }

  "extract WHERE conditions with parameters" in {
    val result =
      QueryAnalyzer.analyzeQuery("SELECT id, name FROM users WHERE id = ?")

    result must beSuccessfulTry.which { r =>
      r.whereConditions.length must_=== 1 and {
        r.whereConditions(0).field must_=== "id"
      } and {
        r.whereConditions(0).operator must_=== "="
      } and {
        r.parameterCount must_=== 1
      }
    }
  }

  "handle multiple WHERE conditions" in {
    val result = QueryAnalyzer.analyzeQuery(
      "SELECT id, name FROM users WHERE id = ? AND name LIKE ?"
    )

    result must beSuccessfulTry.which { r =>
      r.whereConditions.length must_=== 2 and {
        r.parameterCount must_=== 2
      } and {
        r.whereConditions(0).operator must_=== "="
      } and {
        r.whereConditions(1).operator must_=== "LIKE"
      }
    }
  }

  "parse comparison operators" in {
    QueryAnalyzer.analyzeQuery(
      "SELECT * FROM t WHERE a = ?"
    ) must beSuccessfulTry.which(
      _.whereConditions(0).operator must_=== "="
    ) and {
      QueryAnalyzer.analyzeQuery(
        "SELECT * FROM t WHERE a > ?"
      ) must beSuccessfulTry.which(
        _.whereConditions(0).operator must_=== ">"
      )
    } and {
      QueryAnalyzer.analyzeQuery(
        "SELECT * FROM t WHERE a < ?"
      ) must beSuccessfulTry.which(
        _.whereConditions(0).operator must_=== "<"
      )
    } and {
      QueryAnalyzer.analyzeQuery(
        "SELECT * FROM t WHERE a >= ?"
      ) must beSuccessfulTry.which(
        _.whereConditions(0).operator must_=== ">="
      )
    }
  }

  "handle queries without WHERE clause" in {
    val result = QueryAnalyzer.analyzeQuery("SELECT id, name FROM users")

    result must beSuccessfulTry.which { r =>
      r.whereConditions.length must_=== 0 and {
        r.parameterCount must_=== 0
      }
    }
  }

  "identify parameter positions in WHERE clause" in {
    QueryAnalyzer
      .identifyParameterPositions("WHERE a = ? AND b = ?")
      .length must_=== 2 and {
      QueryAnalyzer.identifyParameterPositions("WHERE c = ?").length must_=== 1
    } and {
      QueryAnalyzer.identifyParameterPositions("WHERE x > 10").length must_=== 0
    }
  }

  "ignore literals in WHERE clause" in {
    val result = QueryAnalyzer.analyzeQuery(
      "SELECT * FROM t WHERE name = 'John' AND id = ?"
    )

    result must beSuccessfulTry.which(_.parameterCount must_=== 1)
  }

  "handle case-insensitive SELECT and WHERE" in {
    QueryAnalyzer.analyzeQuery(
      "select id from users where id = ?"
    ) must beSuccessfulTry.which(
      _.selectFields must_=== List("id")
    ) and {
      QueryAnalyzer.analyzeQuery(
        "SELECT ID FROM USERS WHERE ID = ?"
      ) must beSuccessfulTry.which(
        _.selectFields must_=== List("ID")
      )
    }
  }
}
