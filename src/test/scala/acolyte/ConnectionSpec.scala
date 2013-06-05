package acolyte

import java.util.concurrent.Executors

import java.sql.{
  ResultSet,
  SQLClientInfoException,
  SQLException,
  SQLFeatureNotSupportedException
}

import org.specs2.mutable.Specification

object ConnectionSpec extends Specification with ConnectionFixtures {
  "Connection specification" title

  "Connection constructor" should {
    "not accept null URL" in {
      connection(url = null, props = null, handler = defaultHandler).
        aka("connection") must throwA[IllegalArgumentException](
          message = "Invalid JDBC URL")
    }

    "not accept null handler" in {
      connection(url = jdbcUrl, props = null, handler = null).
        aka("connection") must throwA[IllegalArgumentException](
          message = "Invalid Acolyte handler")
    }

    "return not-null instance for valid information" in {
      Option(connection(url = jdbcUrl, props = null, handler = defaultHandler)).
        aka("connection") must beSome.which { conn ⇒
          (conn.getAutoCommit aka "auto-commit" must beFalse).
            and(conn.isReadOnly aka "read-only" must beFalse).
            and(conn.isClosed aka "closed" must beFalse).
            and(conn.isValid(0) aka "validity" must beTrue).
            and(conn.getWarnings aka "warnings" must beNull).
            and(conn.getTransactionIsolation.aka("transaction isolation").
              mustEqual(java.sql.Connection.TRANSACTION_NONE)).
            and(conn.getTypeMap aka "type-map" mustEqual emptyTypeMap).
            and(conn.getClientInfo aka "client info" mustEqual emptyClientInfo).
            and(conn.getCatalog aka "catalog" must beNull).
            and(conn.getSchema aka "schema" must beNull).
            and(conn.getHoldability.
              aka("holdability") mustEqual ResultSet.CLOSE_CURSORS_AT_COMMIT).
            and(Option(conn.getMetaData) aka "meta-data" must beSome.which(
              _.getConnection aka "meta-data owner" mustEqual conn))

        }
    }
  }

  "Type-map setter" should {
    "refuse null mapping" in {
      defaultCon.setTypeMap(null).
        aka("setter") must throwA[SQLException](
          message = "Invalid type-map")

    }
  }

  "Client info setter" should {
    "refuse null properties" in {
      defaultCon.setClientInfo(null).
        aka("set client info") must throwA[java.lang.IllegalArgumentException]

    }

    "not be settable on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.setClientInfo(new java.util.Properties()).
        aka("set client info") must throwA[SQLClientInfoException]

    }

    "not be set a single property on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.setClientInfo("name", "value").
        aka("set single client property") must throwA[SQLClientInfoException]

    }
  }

  "Network timeout" should {
    "not be settable" in {
      defaultCon.setNetworkTimeout(null, 0).
        aka("timeout setting") must throwA[SQLFeatureNotSupportedException]

    }

    "not be readable" in {
      defaultCon.getNetworkTimeout.
        aka("getter") must throwA[SQLFeatureNotSupportedException]

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
        message = "Connection is already closed")
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
          message = "Connection is closed")
      }

      "auto-commit" in {
        c.getAutoCommit aka "getter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "read-only" in {
        c.isReadOnly aka "getter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "isolation level" in {
        c.getTransactionIsolation aka "getter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "warnings" in {
        c.getWarnings aka "getter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "type-map" in {
        c.getTypeMap aka "getter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "holdability" in {
        c.getHoldability aka "getter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "client info properties" in {
        c.getClientInfo aka "getter" must throwA[SQLClientInfoException]
      }

      "client info property" in {
        c.getClientInfo("name").
          aka("getter") must throwA[SQLClientInfoException]

      }

      "schema" in {
        c.getSchema aka "getter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "meta-data" in {
        c.getMetaData aka "getter" must throwA[SQLException](
          message = "Connection is closed")

      }
    }

    "not set property" >> {
      lazy val c = defaultCon
      c.close()

      "catalog" in {
        c.setCatalog("catalog") aka "setter" must throwA[SQLException](
          message = "Connection is closed")
      }

      "auto-commit" in {
        c.setAutoCommit(true) aka "setter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "read-only" in {
        c.setReadOnly(true) aka "setter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "isolation level" in {
        c.setTransactionIsolation(-1) aka "setter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "warnings" in {
        c.clearWarnings() aka "setter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "type-map" in {
        c.setTypeMap(new java.util.HashMap()).
          aka("setter") must throwA[SQLException]("Connection is closed")

      }

      "holdability" in {
        c.setHoldability(-1) aka "setter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "client info properties" in {
        c.setClientInfo(new java.util.Properties()).
          aka("setter") must throwA[SQLClientInfoException]
      }

      "client info property" in {
        c.setClientInfo("name", "value").
          aka("setter") must throwA[SQLClientInfoException]

      }

      "schema" in {
        c.setSchema("schema") aka "setter" must throwA[SQLException](
          message = "Connection is closed")

      }
    }
  }

  "Rollback" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      (c.getAutoCommit aka "auto-commit" must beTrue).
        and(c.rollback() aka "rollback" must throwA[SQLException](
          message = "Auto-commit is enabled"))

    }

    "not be applied on closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.rollback() aka "rollback" must throwA[SQLException](
        message = "Connection is closed")

    }
  }

  "Savepoint" should {
    "not be set on closed connection" >> {
      lazy val c = defaultCon
      c.close()

      "as un-named one" in {
        c.setSavepoint() aka "setter" must throwA[SQLException](
          message = "Connection is closed")

      }

      "as named one" in {
        c.setSavepoint("name") aka "setter" must throwA[SQLException](
          message = "Connection is closed")

      }
    }

    "not be set when auto-commit mode is enabled" >> {
      val c = defaultCon
      c.setAutoCommit(true)

      "as un-named one" in {
        c.setSavepoint() aka "setter" must throwA[SQLException](
          message = "Auto-commit is enabled")

      }

      "as named one" in {
        c.setSavepoint("name") aka "setter" must throwA[SQLException](
          message = "Auto-commit is enabled")

      }
    }
  }

  "Savepoint rollback" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.setAutoCommit(true)

      c.rollback(s) aka "savepoint rollback" must throwA[SQLException](
        message = "Auto-commit is enabled")

    }

    "not be applied on closed connection" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.close()

      c.rollback(s) aka "savepoint rollback" must throwA[SQLException](
        message = "Connection is closed")

    }

    "be an unsupported feature" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.rollback(s).
        aka("savepoint rollback") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Savepoint release" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.setAutoCommit(true)

      c.releaseSavepoint(s) aka "savepoint release" must throwA[SQLException](
        message = "Auto-commit is enabled")

    }

    "not be applied on closed connection" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.close()

      c.releaseSavepoint(s) aka "savepoint release" must throwA[SQLException](
        message = "Connection is closed")

    }

    "be an unsupported feature" in {
      lazy val c = defaultCon
      lazy val s = c.setSavepoint

      c.releaseSavepoint(s).
        aka("savepoint release") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Commit" should {
    "not be supported when auto-commit is enabled" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      (c.getAutoCommit aka "auto-commit" must beTrue).
        and(c.commit() aka "commit" must throwA[SQLException](
          message = "Auto-commit is enabled"))

    }
  }

  "Auto-commit mode" should {
    "not be set on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.setAutoCommit(true) aka "setting" must throwA[SQLException](
        message = "Connection is closed")

    }
  }

  "Native conversion" should {
    "not be called on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.nativeSQL("test") aka "conversion" must throwA[SQLException](
        message = "Connection is closed")

    }

    "return unchanged SQL" in {
      defaultCon.nativeSQL("SELECT *") aka "SQL" mustEqual "SELECT *"
    }
  }

  "Holdability" should {
    "not be changeable" in {
      defaultCon.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT).
        aka("set holdability") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Savepoint" should {
    "not be settable without name on auto-commit connection" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      c.setSavepoint() aka "set savepoint" must throwA[SQLException](
        message = "Auto-commit is enabled")

    }

    "not be settable with name on auto-commit connection" in {
      lazy val c = defaultCon
      c.setAutoCommit(true)

      c.setSavepoint("savepoint").
        aka("set savepoint") must throwA[SQLException](
          message = "Auto-commit is enabled")

    }
  }

  "Connection wrapper" should {
    "be valid for java.sql.Connection" in {
      defaultCon.isWrapperFor(classOf[java.sql.Connection]).
        aka("is wrapper for java.sql.Connection") must beTrue

    }

    "be unwrapped to java.sql.Connection" in {
      Option(defaultCon.unwrap(classOf[java.sql.Connection])).
        aka("unwrapped") must beSome.which(_.isInstanceOf[java.sql.Connection])

    }
  }

  "Abortion" should {
    lazy val exec = Executors.newSingleThreadExecutor

    "not be executed without executor" in {
      defaultCon.abort(null) aka "abortion" must throwA[SQLException](
        message = "Missing executor")

    }

    "be no-op on a closed connection" in {
      lazy val c = defaultCon
      c.close()

      c.abort(exec) aka "abortion" must not(throwA[SQLException])
    }

    "mark connection as closed" in {
      lazy val c = defaultCon

      (c.abort(exec) aka "aborting" must not(throwA[SQLException])).
        and(c.isClosed aka "closed" must beTrue)

    }
  }

  "LOB" should {
    "not be created for characters" in {
      defaultCon.createClob.
        aka("create clob") must throwA[SQLFeatureNotSupportedException]

    }

    "not be created for binary" in {
      defaultCon.createBlob.
        aka("create blob") must throwA[SQLFeatureNotSupportedException]

    }

    "not be created for national characters" in {
      defaultCon.createNClob.
        aka("create nclob") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Structural types" should {
    "not be supported for XML" in {
      defaultCon.createSQLXML().
        aka("create XML") must throwA[SQLFeatureNotSupportedException]

    }

    "not be supported for STRUCT" in {
      defaultCon.createStruct("CHAR", Array[Object]()).
        aka("create struct") must throwA[SQLFeatureNotSupportedException]

    }

    "not be supported for ARRAY" in {
      defaultCon.createArrayOf("CHAR", Array[Object]()).
        aka("create array") must throwA[SQLFeatureNotSupportedException]

    }
  }
}

sealed trait ConnectionFixtures {
  val jdbcUrl = "jdbc:acolyte:test"
  val emptyTypeMap = new java.util.HashMap[String, Class[_]]()
  val emptyClientInfo = new java.util.Properties()
  val defaultHandler = EmptyConnectionHandler

  def defaultCon = connection(jdbcUrl, null, defaultHandler)

  def connection(url: String, props: java.util.Properties, handler: ConnectionHandler) = new acolyte.Connection(url, props, handler)
}
