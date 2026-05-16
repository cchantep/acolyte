package acolyte.jdbc.mcp

final class QueryExecutorSpec extends org.specs2.mutable.Specification {
  "QueryExecutor".title

  "execute single parameter set" in withConnection { conn =>
    QueryExecutor.executeQuery(
      conn,
      "SELECT 1 as id",
      List(List.empty)
    ) must beSuccessfulTry.which { executions =>
      (executions.length must_=== 1) and (executions(0).rowCount must_=== 1)
    }
  }

  "execute multiple parameter sets" in withConnection { conn =>
    QueryExecutor.executeQuery(
      conn,
      "SELECT ? as val",
      List(List(1), List(2), List(3))
    ) must beSuccessfulTry.which(_.length must_=== 3)
  }

  "set string parameters" in withConnection { conn =>
    QueryExecutor.executeQuery(
      conn,
      "SELECT ? as str",
      List(List("hello"))
    ) must beSuccessfulTry.which(_.length must_=== 1)
  }

  "set numeric parameters" in withConnection { conn =>
    QueryExecutor.executeQuery(
      conn,
      "SELECT ? as num",
      List(List(42))
    ) must beSuccessfulTry.which(_.length must_=== 1)
  }

  "handle null parameters" in withConnection { conn =>
    QueryExecutor.executeQuery(
      conn,
      "SELECT ? as nullable",
      List(List(null))
    ) must beSuccessfulTry.which(_.nonEmpty must beTrue)
  }

  "preserve parameter order" in withConnection { conn =>
    QueryExecutor.executeQuery(
      conn,
      "SELECT ? as a, ? as b",
      List(List("x", "y"))
    ) must beSuccessfulTry.which(_.length must_=== 1)
  }

  "capture row count" in withConnection { conn =>
    QueryExecutor.executeQuery(
      conn,
      "SELECT 1 as id",
      List(List.empty)
    ) must beSuccessfulTry.which(_(0).rowCount must_=== 1)
  }

  "handle empty result" in withConnection { conn =>
    QueryExecutor.executeQuery(
      conn,
      "SELECT 1 WHERE 0 = 1",
      List(List.empty)
    ) must beSuccessfulTry.which(_(0).rowCount must_=== 0)
  }

  private def withConnection[T](f: java.sql.Connection => T): T = {
    java.sql.DriverManager.registerDriver(new org.h2.Driver)

    lazy val conn =
      java.sql.DriverManager.getConnection("jdbc:h2:mem:test", "sa", "")

    try {
      f(conn)
    } finally {
      conn.close()
    }
  }
}
