package acolyte.jdbc

import java.util.concurrent.Executors

import java.sql.{
  ResultSet,
  SQLClientInfoException,
  SQLException,
  SQLFeatureNotSupportedException,
  Statement,
  Types
}
import java.sql.ResultSet.{
  CLOSE_CURSORS_AT_COMMIT,
  CONCUR_READ_ONLY,
  CONCUR_UPDATABLE,
  HOLD_CURSORS_OVER_COMMIT,
  TYPE_FORWARD_ONLY,
  TYPE_SCROLL_INSENSITIVE
}

import org.specs2.mutable.Specification

import acolyte.jdbc.test.EmptyConnectionHandler

object ConnectionSpec extends Specification with ConnectionFixtures {
  "Connection specification".title

  "Connection constructor" should {
    "not accept null URL" in {
      connection(url = null, props = null, handler = defaultHandler).aka(
        "connection"
      ) must throwA[IllegalArgumentException](message = "Invalid JDBC URL")
    }

    "not accept null handler" in {
      connection(url = jdbcUrl, props = null, handler = null)
        .aka("connection") must throwA[IllegalArgumentException](
        message = "Invalid Acolyte handler"
      )
    }

    "return not-null instance for valid information" in {
      val conn =
        connection(url = jdbcUrl, props = null, handler = defaultHandler)

      (conn.getAutoCommit aka "auto-commit" must beFalse)
        .and(conn.isReadOnly aka "read-only" must beFalse)
        .and(conn.isClosed aka "closed" must beFalse)
        .and(conn.isValid(0) aka "validity" must beTrue)
        .and(conn.getWarnings aka "warnings" must beNull)
        .and(
          conn.getTransactionIsolation
            .aka("transaction isolation")
            .must_===(java.sql.Connection.TRANSACTION_NONE)
        )
        .and(conn.getTypeMap aka "type-map" must_=== emptyTypeMap)
        .and(conn.getClientInfo aka "client info" must_=== emptyClientInfo)
        .and(conn.getCatalog aka "catalog" must beNull)
        .and(conn.getSchema aka "schema" must beNull)
        .and(
          conn.getHoldability
            .aka("holdability") must_=== ResultSet.CLOSE_CURSORS_AT_COMMIT
        )
        .and(
          Option(conn.getMetaData) aka "meta-data" must beSome[
            java.sql.DatabaseMetaData
          ].which(_.getConnection aka "meta-data owner" must_=== conn)
        )

    }

    "set immutable properties on new connection" in {
      val props = new java.util.Properties()
      props.put("_test", "_1")

      val con =
        connection(url = jdbcUrl, props = props, handler = defaultHandler)

      con.getProperties aka "properties" must_=== props and {
        props.put("_test", "_2")
        con.getProperties.get("_test") aka "property" must_=== "_1"
      }
    }
  }

  "Type-map setter" should {
    "refuse null mapping" in {
      defaultCon.setTypeMap(null) must throwA[SQLException]("Invalid type-map")
    }
  }

  "Client info setter" should {
    "refuse null properties" in {
      defaultCon
        .setClientInfo(null)
        .aka("set client info") must throwA[java.lang.IllegalArgumentException]

    }

    "not be settable on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.setClientInfo(new java.util.Properties())
        .aka("set client info") must throwA[SQLClientInfoException]

    }

    "not be set a single property on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.setClientInfo("name", "value")
        .aka("set single client property") must throwA[SQLClientInfoException]

    }
  }

  "Network timeout" should {
    "not be settable" in {
      defaultCon
        .setNetworkTimeout(null, 0)
        .aka("timeout setting") must throwA[SQLFeatureNotSupportedException]

    }

    "not be readable" in {
      defaultCon.getNetworkTimeout
        .aka("getter") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Closed connection" should {
    "be marked" in {
      lazy val c = defaultCon
      c.close()

      c.isClosed aka "closed" must beTrue
    }

    "not be closed again" in {
      lazy val c = defaultCon
      c.close()

      c.close() aka "closing" must throwA[SQLException](
        message = "Connection is already closed"
      )
    }

    "not be valid" in {
      lazy val c = defaultCon
      c.close()

      c.isValid(0) aka "validity" must beFalse
    }

    "not return property" >> {
      lazy val c = defaultCon
      c.close()

      "catalog" in {
        c.getCatalog aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )
      }

      "auto-commit" in {
        c.getAutoCommit aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "read-only" in {
        c.isReadOnly aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "isolation level" in {
        c.getTransactionIsolation aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "warnings" in {
        c.getWarnings aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "type-map" in {
        c.getTypeMap aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "holdability" in {
        c.getHoldability aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "client info properties" in {
        c.getClientInfo aka "getter" must throwA[SQLClientInfoException]
      }

      "client info property" in {
        c.getClientInfo("name")
          .aka("getter") must throwA[SQLClientInfoException]

      }

      "schema" in {
        c.getSchema aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "meta-data" in {
        c.getMetaData aka "getter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }
    }

    "not set property" >> {
      lazy val c = defaultCon
      c.close()

      "catalog" in {
        c.setCatalog("catalog") aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )
      }

      "auto-commit" in {
        c.setAutoCommit(true) aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "read-only" in {
        c.setReadOnly(true) aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "isolation level" in {
        c.setTransactionIsolation(-1) aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "warnings" in {
        c.clearWarnings() aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "type-map" in {
        c.setTypeMap(new java.util.HashMap())
          .aka("setter") must throwA[SQLException]("Connection is closed")

      }

      "holdability" in {
        c.setHoldability(-1) aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "client info properties" in {
        c.setClientInfo(new java.util.Properties())
          .aka("setter") must throwA[SQLClientInfoException]
      }

      "client info property" in {
        c.setClientInfo("name", "value")
          .aka("setter") must throwA[SQLClientInfoException]

      }

      "schema" in {
        c.setSchema("schema") aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }
    }
  }

  "Rollback" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      (c.getAutoCommit aka "auto-commit" must beTrue).and(
        c.rollback() aka "rollback" must throwA[SQLException](
          message = "Auto-commit is enabled"
        )
      )

    }

    "be intercepted" >> {
      "successfully" in {
        @volatile var rollback = 0

        val conHandler = new ConnectionHandler {
          def getStatementHandler = test.EmptyStatementHandler

          def getResourceHandler = new ResourceHandler {
            def whenCommitTransaction(c: Connection): Unit = ()

            def whenRollbackTransaction(c: Connection): Unit = {
              rollback += 1
            }
          }

          def withResourceHandler(h: ResourceHandler): ConnectionHandler =
            new ConnectionHandler.Default(test.EmptyStatementHandler, h)
        }

        connection(jdbcUrl, emptyClientInfo, conHandler).rollback() must not(
          throwA[SQLException]
        ) and {
          rollback must_=== 1
        }
      }

      "with exception" in {
        val conHandler = new ConnectionHandler {
          def getStatementHandler = test.EmptyStatementHandler

          def getResourceHandler = new ResourceHandler {
            def whenCommitTransaction(c: Connection): Unit = ()

            def whenRollbackTransaction(c: Connection): Unit =
              throw new SQLException("Foo")
          }

          def withResourceHandler(h: ResourceHandler): ConnectionHandler =
            new ConnectionHandler.Default(test.EmptyStatementHandler, h)
        }

        connection(jdbcUrl, emptyClientInfo, conHandler)
          .rollback() must throwA[SQLException]("Foo")
      }
    }

    "not be applied on closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.rollback() aka "rollback" must throwA[SQLException](
        message = "Connection is closed"
      )

    }
  }

  "Savepoint" should {
    "not be set on closed connection" >> {
      lazy val c = defaultCon
      c.close()

      "as un-named one" in {
        c.setSavepoint() aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }

      "as named one" in {
        c.setSavepoint("name") aka "setter" must throwA[SQLException](
          message = "Connection is closed"
        )

      }
    }

    "not be set when auto-commit mode is enabled" >> {
      val c = defaultCon
      c.setAutoCommit(true)

      "as un-named one" in {
        c.setSavepoint() aka "setter" must throwA[SQLException](
          message = "Auto-commit is enabled"
        )

      }

      "as named one" in {
        c.setSavepoint("name") aka "setter" must throwA[SQLException](
          message = "Auto-commit is enabled"
        )

      }
    }
  }

  "Savepoint rollback" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.setAutoCommit(true)

      c.rollback(s) aka "savepoint rollback" must throwA[SQLException](
        message = "Auto-commit is enabled"
      )

    }

    "not be applied on closed connection" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.close()

      c.rollback(s) aka "savepoint rollback" must throwA[SQLException](
        message = "Connection is closed"
      )

    }

    "be an unsupported feature" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.rollback(s)
        .aka("savepoint rollback") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Savepoint release" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.setAutoCommit(true)

      c.releaseSavepoint(s) aka "savepoint release" must throwA[SQLException](
        message = "Auto-commit is enabled"
      )

    }

    "not be applied on closed connection" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.close()

      c.releaseSavepoint(s) aka "savepoint release" must throwA[SQLException](
        message = "Connection is closed"
      )

    }

    "be an unsupported feature" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.releaseSavepoint(s)
        .aka("savepoint release") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Commit" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      (c.getAutoCommit aka "auto-commit" must beTrue).and(
        c.commit() aka "commit" must throwA[SQLException](
          message = "Auto-commit is enabled"
        )
      )

    }

    "be intercepted" >> {
      "successfully" in {
        @volatile var commit = 0

        val conHandler = new ConnectionHandler {
          def getStatementHandler = test.EmptyStatementHandler

          def getResourceHandler = new ResourceHandler {
            def whenCommitTransaction(c: Connection): Unit = {
              commit += 1
            }

            def whenRollbackTransaction(c: Connection): Unit = ()
          }

          def withResourceHandler(h: ResourceHandler): ConnectionHandler =
            new ConnectionHandler.Default(test.EmptyStatementHandler, h)
        }

        connection(jdbcUrl, emptyClientInfo, conHandler).commit() must not(
          throwA[SQLException]
        ) and {
          commit must_=== 1
        }
      }

      "with exception" in {
        val conHandler = new ConnectionHandler {
          def getStatementHandler = test.EmptyStatementHandler

          def getResourceHandler = new ResourceHandler {
            def whenCommitTransaction(c: Connection): Unit =
              throw new SQLException("Bar")

            def whenRollbackTransaction(c: Connection): Unit = ()
          }

          def withResourceHandler(h: ResourceHandler): ConnectionHandler =
            new ConnectionHandler.Default(test.EmptyStatementHandler, h)
        }

        connection(jdbcUrl, emptyClientInfo, conHandler)
          .commit() must throwA[SQLException]("Bar")
      }
    }
  }

  "Auto-commit mode" should {
    "not be set on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.setAutoCommit(true) aka "setting" must throwA[SQLException](
        message = "Connection is closed"
      )

    }
  }

  "Native conversion" should {
    "not be called on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.nativeSQL("test") aka "conversion" must throwA[SQLException](
        message = "Connection is closed"
      )

    }

    "return unchanged SQL" in {
      defaultCon.nativeSQL("SELECT *") aka "SQL" must_=== "SELECT *"
    }
  }

  "Holdability" should {
    "not be changeable" in {
      defaultCon
        .setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT)
        .aka("set holdability") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Savepoint" should {
    "not be settable without name on auto-commit connection" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      c.setSavepoint() aka "set savepoint" must throwA[SQLException](
        message = "Auto-commit is enabled"
      )

    }

    "not be settable with name on auto-commit connection" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      c.setSavepoint("savepoint")
        .aka("set savepoint") must throwA[SQLException](
        message = "Auto-commit is enabled"
      )

    }
  }

  "Connection wrapper" should {
    "be valid for java.sql.Connection" in {
      defaultCon
        .isWrapperFor(classOf[java.sql.Connection])
        .aka("is wrapper for java.sql.Connection") must beTrue

    }

    "be unwrapped to java.sql.Connection" in {
      Option(defaultCon.unwrap(classOf[java.sql.Connection]))
        .aka("unwrapped") must beSome[java.sql.Connection]

    }
  }

  "Abortion" should {
    lazy val exec = Executors.newSingleThreadExecutor

    "not be executed without executor" in {
      defaultCon.abort(null) aka "abortion" must throwA[SQLException](
        message = "Missing executor"
      )

    }

    "be no-op on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.abort(exec) aka "abortion" must not(throwA[SQLException])
    }

    "mark connection as closed" in {
      lazy val c = defaultCon

      (c.abort(exec) aka "aborting" must not(throwA[SQLException]))
        .and(c.isClosed aka "closed" must beTrue)

    }
  }

  "LOB" should {
    "not be created for characters" in {
      defaultCon.createClob
        .aka("create clob") must throwA[SQLFeatureNotSupportedException]

    }

    "not be created for binary" in {
      val data = s"test:${System identityHashCode this}".getBytes("UTF-8")
      val dataLen = data.length.toLong

      Option(defaultCon.createBlob()) aka "create blob" must beSome[
        java.sql.Blob
      ].which { b =>
        b.length aka "initial size" must_=== 0L and {
          b.setBytes(0, data).toLong aka "setting bytes #1" must_=== dataLen
        } and {
          b.length aka "updated size" must_=== dataLen
        } and {
          b.setBytes(2, "test".getBytes("UTF-8"), 1, 3)
            .aka("setting bytes #2") must_=== 3
        } and {
          b.length aka "overriden size" must_=== dataLen
        }
      }
    }

    "not be created for national characters" in {
      defaultCon.createNClob
        .aka("create nclob") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Structural types" should {
    "not be supported for XML" in {
      defaultCon
        .createSQLXML()
        .aka("create XML") must throwA[SQLFeatureNotSupportedException]

    }

    "not be supported for STRUCT" in {
      defaultCon
        .createStruct("CHAR", Array[Object]())
        .aka("create struct") must throwA[SQLFeatureNotSupportedException]

    }

    "be supported for ARRAY" in {
      defaultCon
        .createArrayOf("VARCHAR", Array("Ab", "cD", "EF"))
        .aka("array") must beLike {
        case strArr =>
          (strArr.getBaseType aka "base type" must_=== Types.VARCHAR) and
            (strArr.getBaseTypeName aka "base type name" must_=== "VARCHAR")
              .and(strArr.getArray aka "element array" must beLike {
                case elmts: Array[String] =>
                  (elmts.size aka "size" must_=== 3) and
                    (elmts(0) aka "1st element" must_=== "Ab") and
                    (elmts(1) aka "2nd element" must_=== "cD") and
                    (elmts(2) aka "3rd element" must_=== "EF")
              })
      }

    }
  }

  "Plain statement" should {
    "be owned by connection" in {
      lazy val c = defaultCon

      (c.createStatement.getConnection aka "statement connection" must_=== c)
        .and(
          c.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)
            .getConnection aka "statement connection" must_=== c
        )
        .and(
          c.createStatement(
            TYPE_FORWARD_ONLY,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT
          ).getConnection aka "statement connection" must_=== c
        )

    }

    "not be created from a closed connection" in {
      lazy val c = defaultCon
      c.close()

      (c.createStatement aka "creation" must throwA[SQLException](
        message = "Connection is closed"
      )).and(
        c.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)
          .aka("creation") must throwA[SQLException]("Connection is closed")
      ).and(
        c.createStatement(
          TYPE_FORWARD_ONLY,
          CONCUR_READ_ONLY,
          CLOSE_CURSORS_AT_COMMIT
        ).aka("creation") must throwA[SQLException]("Connection is closed")
      )

    }

    "not be created with unsupported resultset type" in {
      (defaultCon
        .createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set type"
      )).and(
        defaultCon
          .createStatement(
            TYPE_SCROLL_INSENSITIVE,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT
          )
          .aka("creation") must throwA[SQLFeatureNotSupportedException](
          message = "Unsupported result set type"
        )
      )

    }

    "not be created with unsupported resultset concurrency" in {
      (defaultCon
        .createStatement(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set concurrency"
      )).and(
        defaultCon
          .createStatement(
            TYPE_FORWARD_ONLY,
            CONCUR_UPDATABLE,
            CLOSE_CURSORS_AT_COMMIT
          )
          .aka("creation") must throwA[SQLFeatureNotSupportedException](
          message = "Unsupported result set concurrency"
        )
      )

    }

    "not be created with unsupported resultset holdability" in {
      defaultCon
        .createStatement(
          TYPE_FORWARD_ONLY,
          CONCUR_READ_ONLY,
          HOLD_CURSORS_OVER_COMMIT
        )
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set holdability"
      )

    }
  }

  "Prepared statement" should {
    "support generated keys" >> {
      import acolyte.jdbc.RowList.Column
      import acolyte.jdbc.test.Params

      lazy val meta = (Column(classOf[String], "a"), Column(classOf[Int], "b"))
      lazy val generatedKeys = RowLists
        .rowList2(meta._1, meta._2.withNullable(true))
        .append("Foo", 200)

      lazy val h = new StatementHandler {
        def isQuery(s: String) = false
        def whenSQLQuery(s: String, p: Params) = sys.error("Not")
        def whenSQLUpdate(s: String, p: Params) =
          UpdateResult.One.withGeneratedKeys(generatedKeys)

      }
      lazy val c =
        connection(jdbcUrl, null, new acolyte.jdbc.ConnectionHandler.Default(h))

      def spec(s: java.sql.PreparedStatement) = {
        (s.executeUpdate aka "update count" must_=== 1)
          .and(s.getGeneratedKeys aka "generated keys" must beLike {
            case ks =>
              (ks.getStatement aka "keys statement" must_=== s)
                .and(ks.next aka "has first key" must beTrue)
                .and(ks.getInt(1) aka "first key" must_=== 200)
                .and(ks.next aka "has second key" must beFalse)
          })
      }

      "specified by column names" in {
        spec(c.prepareStatement("TEST", Array("b")))
      }

      "specified by column indexes" in {
        spec(c.prepareStatement("TEST", Array(2)))
      }
    }

    "be owned by connection" in {
      lazy val c = defaultCon

      (c.prepareStatement("TEST")
        .getConnection
        .aka("statement connection") must_=== c)
        .and(
          c.prepareStatement("TEST", Statement.NO_GENERATED_KEYS)
            .getConnection aka "statement connection" must_=== c
        )
        .and(
          c.prepareStatement("TEST", Statement.RETURN_GENERATED_KEYS)
            .getConnection aka "statement connection" must_=== c
        )
        .and(
          c.prepareStatement("TEST", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)
            .getConnection aka "statement connection" must_=== c
        )
        .and(
          c.prepareStatement(
            "TEST",
            TYPE_FORWARD_ONLY,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT
          ).getConnection aka "statement connection" must_=== c
        )

    }

    "not be created from a closed connection" in {
      lazy val c = defaultCon
      c.close()

      (c.prepareStatement("TEST") aka "creation" must throwA[SQLException](
        message = "Connection is closed"
      )).and(
        c.prepareStatement("TEST", Statement.NO_GENERATED_KEYS)
          .aka("creation") must throwA[SQLException]("Connection is closed")
      ).and(
        c.prepareStatement("TEST", Statement.RETURN_GENERATED_KEYS)
          .aka("creation") must throwA[SQLException]("Connection is closed")
      ).and(
        c.prepareStatement("TEST", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)
          .aka("creation") must throwA[SQLException]("Connection is closed")
      ).and(
        c.prepareStatement(
          "TEST",
          TYPE_FORWARD_ONLY,
          CONCUR_READ_ONLY,
          CLOSE_CURSORS_AT_COMMIT
        ).aka("creation") must throwA[SQLException]("Connection is closed")
      )

    }

    "not be created with unsupported resultset type" in {
      (defaultCon
        .prepareStatement("TEST", TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set type"
      )).and(
        defaultCon
          .prepareStatement(
            "TEST",
            TYPE_SCROLL_INSENSITIVE,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT
          )
          .aka("creation") must throwA[SQLFeatureNotSupportedException](
          message = "Unsupported result set type"
        )
      )

    }

    "not be created with unsupported resultset concurrency" in {
      (defaultCon
        .prepareStatement("TEST", TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set concurrency"
      )).and(
        defaultCon
          .prepareStatement(
            "TEST",
            TYPE_FORWARD_ONLY,
            CONCUR_UPDATABLE,
            CLOSE_CURSORS_AT_COMMIT
          )
          .aka("creation") must throwA[SQLFeatureNotSupportedException](
          message = "Unsupported result set concurrency"
        )
      )

    }

    "not be created with unsupported resultset holdability" in {
      defaultCon
        .prepareStatement(
          "TEST",
          TYPE_FORWARD_ONLY,
          CONCUR_READ_ONLY,
          HOLD_CURSORS_OVER_COMMIT
        )
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set holdability"
      )

    }
  }

  "Callable statement" should {
    "be owned by connection" in {
      lazy val c = defaultCon

      (c.prepareCall("TEST")
        .getConnection
        .aka("statement connection") must_=== c)
        .and(
          c.prepareCall("TEST", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)
            .getConnection aka "statement connection" must_=== c
        )
        .and(
          c.prepareCall(
            "TEST",
            TYPE_FORWARD_ONLY,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT
          ).getConnection aka "statement connection" must_=== c
        )

    }

    "not be created from a closed connection" in {
      lazy val c = defaultCon
      c.close()

      (c.prepareCall("TEST") aka "creation" must throwA[SQLException](
        message = "Connection is closed"
      )).and(
        c.prepareCall("TEST", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)
          .aka("creation") must throwA[SQLException]("Connection is closed")
      ).and(
        c.prepareCall(
          "TEST",
          TYPE_FORWARD_ONLY,
          CONCUR_READ_ONLY,
          CLOSE_CURSORS_AT_COMMIT
        ).aka("creation") must throwA[SQLException]("Connection is closed")
      )

    }

    "not be created with unsupported resultset type" in {
      (defaultCon
        .prepareCall("TEST", TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set type"
      )).and(
        defaultCon
          .prepareCall(
            "TEST",
            TYPE_SCROLL_INSENSITIVE,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT
          )
          .aka("creation") must throwA[SQLFeatureNotSupportedException](
          message = "Unsupported result set type"
        )
      )

    }

    "not be created with unsupported resultset concurrency" in {
      (defaultCon
        .prepareCall("TEST", TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set concurrency"
      )).and(
        defaultCon
          .prepareCall(
            "TEST",
            TYPE_FORWARD_ONLY,
            CONCUR_UPDATABLE,
            CLOSE_CURSORS_AT_COMMIT
          )
          .aka("creation") must throwA[SQLFeatureNotSupportedException](
          message = "Unsupported result set concurrency"
        )
      )

    }

    "not be created with unsupported resultset holdability" in {
      defaultCon
        .prepareCall(
          "TEST",
          TYPE_FORWARD_ONLY,
          CONCUR_READ_ONLY,
          HOLD_CURSORS_OVER_COMMIT
        )
        .aka("creation") must throwA[SQLFeatureNotSupportedException](
        message = "Unsupported result set holdability"
      )

    }
  }
}

sealed trait ConnectionFixtures {
  val jdbcUrl = "jdbc:acolyte:test"
  val emptyTypeMap = new java.util.HashMap[String, Class[_]]()
  val emptyClientInfo = new java.util.Properties()
  val defaultHandler = EmptyConnectionHandler

  def defaultCon = connection(jdbcUrl, null, defaultHandler)

  def connection(
      url: String,
      props: java.util.Properties,
      handler: ConnectionHandler
    ) = new Connection(url, props, handler)
}
