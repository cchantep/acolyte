package acolyte

import java.sql.{ ResultSet, SQLException, SQLFeatureNotSupportedException }

import org.specs2.mutable.Specification

object AbstractStatementSpec extends Specification {
  "Abstract statement specification" title

  "Constructor" should {
    "refuse null connection" in {
      statement(c = null) aka "ctor" must throwA[IllegalArgumentException](
        message = "Invalid connection")
    }

    "refuse null handler" in {
      statement(h = null).
        aka("ctor") must throwA[IllegalArgumentException](
          message = "Invalid handler")
    }
  }

  "Wrapping" should {
    "be valid for java.sql.Statement" in {
      statement().isWrapperFor(classOf[java.sql.Statement]).
        aka("is wrapper for java.sql.Statement") must beTrue

    }

    "be unwrapped to java.sql.Statement" in {
      Option(statement().unwrap(classOf[java.sql.Statement])).
        aka("unwrapped") must beSome.which(_.isInstanceOf[java.sql.Statement])

    }
  }

  "Query execution" should {
    var sql: String = null
    lazy val h = new StatementHandler {
      def getGeneratedKeys = null
      def isQuery(s: String) = true
      def whenSQLUpdate(s: String) = -1
      def whenSQLQuery(s: String) = {
        sql = s
        AbstractResultSet.EMPTY
      }
    }

    "return empty resultset" in {
      lazy val s = statement(h = h)

      (s.executeQuery("QUERY").
        aka("result") mustEqual AbstractResultSet.EMPTY).
        and(s.getResultSet aka "resultset" mustEqual AbstractResultSet.EMPTY).
        and(s.getUpdateCount aka "update count" mustEqual -1).
        and(sql aka "executed SQL" mustEqual "QUERY")

    }

    "fail on a closed statement" in {
      lazy val s = statement()
      s.close()

      s.executeQuery("QUERY") aka "query" must throwA[SQLException](
        message = "Statement is closed")

    }

    "be processed" in {
      lazy val s = statement(h = h)

      (s.execute("QUERY") aka "flag" must beTrue).
        and(s.getResultSet aka "resultset" mustEqual AbstractResultSet.EMPTY).
        and(s.getUpdateCount aka "update count" mustEqual -1).
        and(sql aka "executed SQL" mustEqual "QUERY")

    }
  }

  "Update" should {
    var sql: String = null
    lazy val h = new StatementHandler {
      def getGeneratedKeys = null
      def isQuery(s: String) = false
      def whenSQLUpdate(s: String) = { sql = s; 5 }
      def whenSQLQuery(s: String): ResultSet = sys.error("TEST")
    }

    "return expected row count" in {
      lazy val s = statement(h = h)

      (s.executeUpdate("UPDATE") aka "result" mustEqual 5).
        and(s.getUpdateCount aka "update count" mustEqual 5).
        and(s.getResultSet aka "resultset" must beNull).
        and(sql aka "executed SQL" mustEqual "UPDATE")

    }

    "fail on a closed statement" in {
      lazy val s = statement()
      s.close()

      s.executeUpdate("UPDATE") aka "update" must throwA[SQLException](
        message = "Statement is closed")

    }

    "be processed" in {
      lazy val s = statement(h = h)

      (s.execute("UPDATE") aka "result" must beFalse).
        and(s.getUpdateCount aka "update count" mustEqual 5).
        and(s.getResultSet aka "resultset" must beNull).
        and(sql aka "executed SQL" mustEqual "UPDATE")

    }
  }

  "Closed statement" should {
    "be marked" in {
      lazy val s = statement()
      s.close()

      s.isClosed aka "flag" should beTrue
    }
  }

  "Statement" should {
    "have expected connection" in {
      statement().getConnection aka "connection" mustEqual defaultCon
    }

    "have no field max size" in {
      (statement().getMaxFieldSize aka "max size" mustEqual 0).
        and(statement().setMaxFieldSize(1).
          aka("setter") must throwA[UnsupportedOperationException])
    }

    "have no max row count" in {
      (statement().getMaxRows aka "max count" mustEqual 0).
        and(statement().setMaxRows(1).
          aka("setter") must throwA[UnsupportedOperationException])
    }

    "have not query timeout" in {
      (statement().getQueryTimeout aka "timeout" mustEqual 0).
        and(statement().setQueryTimeout(1).
          aka("setter") must throwA[UnsupportedOperationException])

    }

    "not support cancel" in {
      statement().cancel().
        aka("cancel") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Warning" should {
    "initially be null" in {
      statement().getWarnings aka "warning" must beNull
    }
  }

  "Fetch size" should {
    "initially be zero" in {
      statement().getFetchSize aka "initial size" mustEqual 0
    }

    "not be accessible on a closed statement" in {
      lazy val s = statement()
      s.close()

      (s.getFetchSize aka "getter" must throwA[SQLException](
        message = "Statement is closed")).
        and(s.setFetchSize(1) aka "setter" must throwA[SQLException](
          message = "Statement is closed"))

    }

    "not be set negative" in {
      statement().setFetchSize(-1) aka "setter" must throwA[SQLException](
        message = "Negative fetch size")

    }
  }

  "Batch" should {
    "not be added" in {
      statement().addBatch("BATCH") aka "add" must throwA[SQLException](
        message = "Batch is not supported")

    }

    "not be cleared" in {
      statement().clearBatch() aka "add" must throwA[SQLException](
        message = "Batch is not supported")

    }

    "not be executed" in {
      statement().executeBatch() aka "add" must throwA[SQLException](
        message = "Batch is not supported")

    }
  }

  "Generated keys" should {
    "be empty when null is returned by handler" in {
      statement().getGeneratedKeys.
        aka("keys") mustEqual AbstractResultSet.EMPTY

    }
  }

  // ---

  def statement(c: Connection = defaultCon, h: StatementHandler = defaultHandler) = new AbstractStatement(c, h) {}

  val jdbcUrl = "jdbc:acolyte:test"
  lazy val defaultCon = new acolyte.Connection(jdbcUrl, null, "handler")

  lazy val defaultHandler = new StatementHandler {
    def whenSQLQuery(sql: String): ResultSet = sys.error("TEST")
    def whenSQLUpdate(sql: String) = 0
    def isQuery(sql: String) = false
    def getGeneratedKeys = null
  }
}
