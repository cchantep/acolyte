package acolyte

import java.sql.{ SQLException, SQLFeatureNotSupportedException, Types }

import org.specs2.mutable.Specification

object PreparedStatementSpec extends Specification {
  "Prepared statement specification" title

  "Statement" should {
    "not support resultset metadata" in {
      statement().getMetaData.
        aka("metadata") must throwA[SQLFeatureNotSupportedException]

    }
  }

  "Unsupported type" should {
    "be refused as parameter" in {
      (statement().setAsciiStream(0, null, 1).
        aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setUnicodeStream(0, null, 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setBinaryStream(0, null, 1).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setRef(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setBlob(0, null.asInstanceOf[java.sql.Blob]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setClob(0, null.asInstanceOf[java.sql.Clob]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setArray(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setURL(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setRowId(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setNString(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setNCharacterStream(0, null, 1.toLong).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setNClob(0, null.asInstanceOf[java.sql.NClob]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setClob(0, null, 1.toLong).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setBlob(0, null, 1.toLong).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setNClob(0, null, 1.toLong).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setSQLXML(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setAsciiStream(0, null, 1.toLong).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setBinaryStream(0, null, 1.toLong).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setCharacterStream(0, null, 1.toLong).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setAsciiStream(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setCharacterStream(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setNCharacterStream(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setClob(0, null.asInstanceOf[java.io.Reader]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setBlob(0, null.asInstanceOf[java.io.InputStream]).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setNClob(0, null.asInstanceOf[java.io.Reader]).
          aka("setter") must throwA[SQLFeatureNotSupportedException])

    }
  }

  "Batch" should {
    "not be supported" in {
      statement().addBatch() aka "batch" must throwA[SQLException](
        message = "Batch is not supported")
    }
  }

  "Null" should {
    "be set as first parameter (VARCHAR)" in {
      lazy val s = statement()
      s.setNull(1, Types.VARCHAR)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.VARCHAR)

    }
  }

  "Boolean" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setBoolean(1, true)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.BOOLEAN)

    }
  }

  "Byte" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setByte(1, 1.toByte)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.TINYINT)

    }
  }

  "Short" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setShort(1, 1.toShort)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.SMALLINT)

    }
  }

  "Integer" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setInt(1, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.INTEGER)

    }
  }

  "Long" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setLong(1, 1.toLong)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.BIGINT)

    }
  }

  "Float" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setFloat(1, 1.2.toFloat)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.FLOAT).
        and(m.getScale(1) aka "scale" mustEqual 1)

    }
  }

  "Double" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setDouble(1, 1.234.toDouble)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.DOUBLE).
        and(m.getScale(1) aka "scale" mustEqual 3)

    }
  }

  "Numeric" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setBigDecimal(1, new java.math.BigDecimal("1.2345678"))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.NUMERIC).
        and(m.getScale(1) aka "scale" mustEqual 7)

    }
  }

  "Parameter order" should {
    "be kept" in {
      lazy val s = statement()
      s.setBoolean(2, false)
      s.setNull(1, Types.INTEGER)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 2).
        and(m.getParameterType(1) aka "first type" mustEqual Types.INTEGER).
        and(m.getParameterType(2) aka "second type" mustEqual Types.BOOLEAN)

    }
  }

  // ---

  def statement(c: Connection = defaultCon, h: StatementHandler = defaultHandler) = new PreparedStatement(c, h)

  val jdbcUrl = "jdbc:acolyte:test"
  lazy val defaultCon = new acolyte.Connection(jdbcUrl, null, defaultHandler)
  lazy val defaultHandler = EmptyConnectionHandler
}
