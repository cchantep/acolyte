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
        and(statement().setCharacterStream(0, null, 1.toInt).
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

    "be set as object" in {
      lazy val s = statement()
      s.setObject(1, null, Types.FLOAT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.FLOAT)

    }

    "cannot be set as object without SQL type" in {
      statement().setObject(1, null).
        aka("set null object") must throwA[SQLException](
          message = "Cannot set parameter from null object")

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

    "be set as first object with SQL type" in {
      lazy val s = statement()
      s.setObject(1, true, Types.BOOLEAN)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.BOOLEAN)

    }

    "be set as first object without SQL type" in {
      lazy val s = statement()
      s.setObject(1, true)

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

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.toByte, Types.TINYINT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.TINYINT)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.toByte)

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

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.toShort, Types.SMALLINT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.SMALLINT)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.toShort)

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

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1, Types.INTEGER)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.INTEGER)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1)

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

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.toLong, Types.BIGINT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.BIGINT)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.toLong)

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

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.2.toFloat, Types.FLOAT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.FLOAT).
        and(m.getScale(1) aka "scale" mustEqual 1)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.2.toFloat)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.FLOAT).
        and(m.getScale(1) aka "scale" mustEqual 1)

    }

    "be set as REAL object" in {
      lazy val s = statement()
      s.setObject(1, null, Types.REAL)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.REAL)

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

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.234.toDouble, Types.DOUBLE)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.DOUBLE).
        and(m.getScale(1) aka "scale" mustEqual 3)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.234.toDouble)

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

    "be set as first numeric object with type" in {
      lazy val s = statement()
      s.setObject(1, new java.math.BigDecimal("1.2345678"), Types.NUMERIC)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.NUMERIC).
        and(m.getScale(1) aka "scale" mustEqual 7)

    }

    "be set as first numeric object without type" in {
      lazy val s = statement()
      s.setObject(1, new java.math.BigDecimal("1.2345678"))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.NUMERIC).
        and(m.getScale(1) aka "scale" mustEqual 7)

    }

    "be set as first decimal object" in {
      lazy val s = statement()
      s.setObject(1, new java.math.BigDecimal("1.2345678"), Types.DECIMAL)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.DECIMAL).
        and(m.getScale(1) aka "scale" mustEqual 7)

    }
  }

  "String" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setString(1, "str")

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.VARCHAR)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, "str", Types.VARCHAR)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.VARCHAR)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, "str")

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.VARCHAR)

    }
  }

  "Byte array" should {
    "not be supported" in {
      statement().setBytes(1, Array[Byte]()).
        aka("setter") must throwA[SQLException]("Not supported")

    }

    "not be supported passed as object (VARBINARY)" in {
      statement().setObject(1, Array[Byte](), Types.VARBINARY).
        aka("setter") must throwA[SQLFeatureNotSupportedException]

    }

    "not be supported passed as object (LONGVARBINARY)" in {
      statement().setObject(1, Array[Byte](), Types.LONGVARBINARY).
        aka("setter") must throwA[SQLFeatureNotSupportedException]
    }

    "not be supported passed as untyped object" in {
      statement().setObject(1, Array[Byte]()).
        aka("setter") must throwA[SQLFeatureNotSupportedException]
    }
  }

  "Date" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setDate(1, new java.sql.Date(1, 1, 1))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.DATE)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Date(1, 1, 1), Types.DATE)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.DATE)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Date(1, 1, 1))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.DATE)

    }
  }

  "Time" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setTime(1, new java.sql.Time(1, 1, 1))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.TIME)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Time(1, 1, 1), Types.TIME)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.TIME)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Time(1, 1, 1))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.TIME)

    }
  }

  "Timestamp" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setTimestamp(1, new java.sql.Timestamp(1l))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.TIMESTAMP)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Timestamp(1l), Types.TIMESTAMP)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.TIMESTAMP)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Timestamp(1l))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.TIMESTAMP)

    }
  }

  "Parameters" should {
    "be kept in order when not set orderly" in {
      lazy val s = statement()
      s.setBoolean(2, false)
      s.setNull(1, Types.INTEGER)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 2).
        and(m.getParameterType(1) aka "first type" mustEqual Types.INTEGER).
        and(m.getParameterType(2) aka "second type" mustEqual Types.BOOLEAN)

    }

    "be kept in order when partially set at end" in {
      lazy val s = statement()
      s.setObject(2, null, Types.DOUBLE)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 2).
        and(m.getParameterType(2) aka "SQL type" mustEqual Types.DOUBLE).
        and(m.getParameterType(1) aka "missing" must throwA[SQLException](
          message = "Parameter is not set: 1"))

    }

    "be kept in order when partially set at middle" in {
      lazy val s = statement()
      s.setObject(3, null, Types.DOUBLE)
      s.setNull(1, Types.INTEGER)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 3).
        and(m.getParameterType(3) aka "third type" mustEqual Types.DOUBLE).
        and(m.getParameterType(1) aka "first type" mustEqual Types.INTEGER).
        and(m.getParameterType(2) aka "missing" must throwA[SQLException](
          message = "Parameter is not set: 2"))

    }

    "be cleared" in {
      lazy val s = statement()
      s.setBoolean(2, false)
      s.setNull(1, Types.INTEGER)
      s.clearParameters()

      s.getParameterMetaData.getParameterCount aka "count" mustEqual 0
    }
  }

  "Object" should {
    "not be set as parameter" in {
      statement().setObject(1, new Object(), Types.BLOB).
        aka("object param") must throwA[SQLFeatureNotSupportedException]

    }
  }

  // ---

  def statement(c: Connection = defaultCon, h: StatementHandler = defaultHandler) = new PreparedStatement(c, h)

  val jdbcUrl = "jdbc:acolyte:test"
  lazy val defaultCon = new acolyte.Connection(jdbcUrl, null, defaultHandler)
  lazy val defaultHandler = EmptyConnectionHandler
}
