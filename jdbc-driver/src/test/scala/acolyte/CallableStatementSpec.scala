package acolyte

import java.sql.{ SQLException, SQLFeatureNotSupportedException }

import scala.collection.JavaConversions

import org.specs2.mutable.Specification

object CallableStatementSpec
    extends Specification with StatementSpecification[CallableStatement] {

  "Callable statement specification" title

  "Out parameter registration" should {
    "fail on closed statement" in {
      lazy val stmt = statement()
      stmt.close()

      (stmt.registerOutParameter(1, 1).
        aka("registration") must throwA[SQLException]("Statement is closed")).
        and(stmt.registerOutParameter(1, 1, 1).
          aka("registration") must throwA[SQLException]("Statement is closed")).
        and(stmt.registerOutParameter("p", 1).
          aka("registration") must throwA[SQLException]("Statement is closed")).
        and(stmt.registerOutParameter("p", 1, 1).
          aka("registration") must throwA[SQLException]("Statement is closed"))
    }

    "fail with custom type" in {
      lazy val stmt = statement()

      (stmt.registerOutParameter(1, 1, "VARCHAR").
        aka("registration") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.registerOutParameter("p", 1, "VARCHAR").
          aka("registration") must throwA[SQLFeatureNotSupportedException])

    }

    "fail for invalid parameter" in {
      lazy val stmt = statement()

      (stmt.registerOutParameter(-1, 1).
        aka("registration") must throwA[SQLException]("Invalid index: -1")).
        and(stmt.registerOutParameter(0, 1, 1).
          aka("registration") must throwA[SQLException]("Invalid index: 0")).
        and(stmt.registerOutParameter(null, 1).
          aka("registration") must throwA[SQLException]("Invalid name: null")).
        and(stmt.registerOutParameter("", 1, 1).
          aka("registration") must throwA[SQLException]("Invalid name: "))

    }
  }

  "Parameter setter" should {
    "fail with named param or unsupported data types" in {
      lazy val stmt = statement()

      (stmt.setObject("param", null.asInstanceOf[Object]).
        aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setObject("param", null.asInstanceOf[Object], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setObject("param", null.asInstanceOf[Object], 1, 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setURL("param", new java.net.URL("https://github.com")).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setAsciiStream("param", null.
          asInstanceOf[java.io.InputStream]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setAsciiStream("param", null.
          asInstanceOf[java.io.InputStream], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setAsciiStream("param", null.
          asInstanceOf[java.io.InputStream], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBinaryStream("param", null.
          asInstanceOf[java.io.InputStream]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBinaryStream("param", null.
          asInstanceOf[java.io.InputStream], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBinaryStream("param", null.
          asInstanceOf[java.io.InputStream], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBytes("param", Array[Byte](1.toByte)).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBlob("param", null.asInstanceOf[java.sql.Blob]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBlob("param", null.asInstanceOf[java.io.InputStream]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBlob("param", null.asInstanceOf[java.io.InputStream], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setClob("param", null.asInstanceOf[java.sql.Clob]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setClob("param", null.asInstanceOf[java.io.Reader]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setClob("param", null.asInstanceOf[java.io.Reader], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setCharacterStream("param", null.asInstanceOf[java.io.Reader]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setCharacterStream("param", null.
          asInstanceOf[java.io.Reader], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setCharacterStream("param", null.
          asInstanceOf[java.io.Reader], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setNClob("param", null.asInstanceOf[java.io.Reader]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setNClob("param", null.asInstanceOf[java.sql.NClob]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setNClob("param", null.asInstanceOf[java.io.Reader], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setNString("param", "str").
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setNull("param", 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setNull("param", 1, "type").
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setSQLXML(1, null.asInstanceOf[java.sql.SQLXML]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setSQLXML("param", null.asInstanceOf[java.sql.SQLXML]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setDouble("param", 1d).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setFloat("param", 1f).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setLong("param", 1l).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setInt("param", 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setShort("param", 1.toShort).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setByte("param", 1.toByte).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBoolean("param", true).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setBigDecimal("param", new java.math.BigDecimal("1")).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setString("param", "str").
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setNCharacterStream("param", null.
          asInstanceOf[java.io.Reader]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setNCharacterStream("param", null.
          asInstanceOf[java.io.Reader], 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setRowId("param", null.asInstanceOf[java.sql.RowId]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setTimestamp("param", null.asInstanceOf[java.sql.Timestamp]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setTimestamp("param", null.asInstanceOf[java.sql.Timestamp],
          java.util.Calendar.getInstance).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setTime("param", new java.sql.Time(1, 2, 3)).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setTime("param", new java.sql.Time(1, 2, 3),
          java.util.Calendar.getInstance).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setDate("param", new java.sql.Date(1, 2, 3)).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.setDate("param", new java.sql.Date(1, 2, 3),
          java.util.Calendar.getInstance).
          aka("setter") must throwA[SQLFeatureNotSupportedException])

    }
  }

  "Getter" should {
    "fail to unsupported datatypes" in {
      lazy val stmt = statement()

      (stmt.getArray(1).
        aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getArray("param").
          aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getRef(1).
          aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getRef("param").
          aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getBlob(1).
          aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getBlob("param").
          aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getClob(1).
          aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getClob("param").
          aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getBytes(1).
          aka("getter") must throwA[SQLFeatureNotSupportedException]).
        and(stmt.getBytes("param").
          aka("getter") must throwA[SQLFeatureNotSupportedException])

    }

    "fail if there is no result" in {
      lazy val stmt = statement()

      (stmt.getObject(1).
        aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getObject(1,
          JavaConversions mapAsJavaMap Map[String, Class[_]]()).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getObject("param",
          JavaConversions mapAsJavaMap Map[String, Class[_]]()).
          aka("getter") must throwA[SQLException]("No result")).
        /* Java 1.7
        and(stmt.getObject(1, classOf[String]).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getObject("param", classOf[String])
          aka ("getter") must throwA[SQLException]("No result")).
         */
        and(stmt.getString(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getString("param").
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getBoolean(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getBoolean("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getByte(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getByte("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getShort(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getShort("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getInt(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getInt("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getLong(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getLong("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getFloat(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getFloat("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getDouble(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getDouble("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getBigDecimal(1)
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getBigDecimal("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getBigDecimal(1, 1)
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getTime(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getTime("param").
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getTime(1, java.util.Calendar.getInstance).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getTime("param", java.util.Calendar.getInstance).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getTimestamp(1)
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getTimestamp("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getTimestamp(1, java.util.Calendar.getInstance).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getTimestamp("param", java.util.Calendar.getInstance).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getCharacterStream(1)
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getCharacterStream("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getNCharacterStream(1)
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getNCharacterStream("param")
          aka ("getter") must throwA[SQLException]("No result")).
        and(stmt.getRowId(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getRowId("param").
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getURL(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getURL("param").
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getNClob(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getNClob("param").
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getNString(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getNString("param").
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getSQLXML(1).
          aka("getter") must throwA[SQLException]("No result")).
        and(stmt.getSQLXML("param").
          aka("getter") must throwA[SQLException]("No result"))

    }
  }

  "Null check" should {
    "fail if there is no result" in {
      statement().wasNull aka "check" must throwA[SQLException]("No result")
    }

    "fail if statement is closed" in {
      val stmt = statement()
      stmt.close()

      stmt.wasNull aka "check" must throwA[SQLException](
        message = "Statement is closed")

    }
  }

  override def statement(c: Connection = defaultCon, s: String = "TEST", h: StatementHandler = defaultHandler.getStatementHandler) = new CallableStatement(c, s, h)

}
