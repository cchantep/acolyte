package acolyte

import java.sql.{
  ResultSet,
  SQLException,
  SQLFeatureNotSupportedException,
  Statement
}

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
      statement().isWrapperFor(classOf[Statement]).
        aka("is wrapper for java.sql.Statement") must beTrue

    }

    "be unwrapped to java.sql.Statement" in {
      Option(statement().unwrap(classOf[Statement])).
        aka("unwrapped") must beSome.which(_.isInstanceOf[Statement])

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

    "be processed ignoring generated keys" in {
      lazy val s = statement(h = h)

      ((s.execute("QUERY", Statement.RETURN_GENERATED_KEYS).
        aka("flag") must beTrue).
        and(s.getResultSet aka "resultset" mustEqual AbstractResultSet.EMPTY).
        and(s.getUpdateCount aka "update count" mustEqual -1).
        and(sql aka "executed SQL" mustEqual "QUERY")).
        /*2*/ and((s.execute("QUERY", Array[Int]()) aka "flag" must beTrue).
          and(s.getResultSet aka "resultset" mustEqual AbstractResultSet.EMPTY).
          and(s.getUpdateCount aka "update count" mustEqual -1).
          and(sql aka "executed SQL" mustEqual "QUERY")).
        /*3*/ and((s.execute("QUERY", Array[String]()) aka "flag" must beTrue).
          and(s.getResultSet aka "resultset" mustEqual AbstractResultSet.EMPTY).
          and(s.getUpdateCount aka "update count" mustEqual -1).
          and(sql aka "executed SQL" mustEqual "QUERY"))

    }

    "be processed without generated keys" in {
      lazy val s = statement(h = h)

      (s.execute("QUERY", Statement.NO_GENERATED_KEYS).
        aka("flag") must beTrue).
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

    "return expected row count without generated keys" in {
      lazy val s = statement(h = h)

      (s.executeUpdate("UPDATE", Statement.NO_GENERATED_KEYS).
        aka("result") mustEqual 5).
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

    "be processed without generated keys" in {
      lazy val s = statement(h = h)

      (s.execute("UPDATE", Statement.NO_GENERATED_KEYS).
        aka("result") must beFalse).
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

    "have default resultset holdability" in {
      statement().getResultSetHoldability().
        aka("holdability") mustEqual ResultSet.CLOSE_CURSORS_AT_COMMIT

    }

    "not be poolable" in {
      statement().isPoolable aka "poolable" must beFalse
    }

    "not close on completion" in {
      statement().isCloseOnCompletion aka "close on completion" must beFalse
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

    "not be returned from update" in {
      (statement().executeUpdate("UPDATE", Statement.RETURN_GENERATED_KEYS).
        aka("update 1") must throwA[SQLFeatureNotSupportedException]).
        and(statement().executeUpdate("UPDATE", Array[Int]()).
          aka("update 2") must throwA[SQLFeatureNotSupportedException]).
        and(statement().executeUpdate("UPDATE", Array[String]()).
          aka("update 3") must throwA[SQLFeatureNotSupportedException])

    }

    "not be returned from execution" in {
      (statement().execute("UPDATE", Array[Int]()).
        aka("execute 1") must throwA[SQLFeatureNotSupportedException]).
        and(statement().execute("UPDATE", Array[String]()).
          aka("execute 2") must throwA[SQLFeatureNotSupportedException])

    }
  }

  // ---

  def statement(c: Connection = defaultCon, h: StatementHandler = defaultHandler) = new AbstractStatement(c, h) {}

  val jdbcUrl = "jdbc:acolyte:test"
  lazy val defaultCon = new acolyte.
    Connection(jdbcUrl, null, EmptyConnectionHandler)

  lazy val defaultHandler = new StatementHandler {
    def whenSQLQuery(sql: String): ResultSet = sys.error("TEST")
    def whenSQLUpdate(sql: String) = 0
    def isQuery(sql: String) = false
    def getGeneratedKeys = null
  }
}
