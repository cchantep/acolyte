package acolyte

import java.util.TimeZone // Force test default TZ

import java.sql.{ SQLException, SQLFeatureNotSupportedException, Types }

import org.specs2.mutable.Specification

import acolyte.test.{EmptyConnectionHandler,Params}

object PreparedStatementSpec extends Specification with Setters {
  "Prepared statement specification" title

  "Test time zone" should {
    "be UTC" in {
      TimeZone.getDefault.
        aka("default TZ") mustEqual TimeZone.getTimeZone("UTC")

    }
  }

  "Statement" should {
    "not support resultset metadata" in {
      statement().getMetaData.
        aka("metadata") must throwA[SQLFeatureNotSupportedException]

    }

    "have SQL" in {
      statement(defaultCon, null).
        aka("ctor") must throwA[IllegalArgumentException]("Missing SQL")

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

    "be set as first parameter with type name VARCHAR" in {
      lazy val s = statement()
      s.setNull(1, Types.VARCHAR, "VARCHAR")

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

    "be set as object with scale" in {
      lazy val s = statement()
      s.setObject(1, null, Types.FLOAT, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.FLOAT)

    }

    "cannot be set as object without SQL type" in {
      statement().setObject(1, null).
        aka("set null object") must throwA[SQLException](
          message = "Cannot set parameter from null object")

    }

    "be NULL as SQL" in {
      (executeUpdate("TEST ?, y", Types.VARCHAR, null).
        aka("SQL update") mustEqual "TEST NULL, y").
        and(executeQuery("SELECT ? WHERE true", Types.FLOAT, null).
          aka("SQL query") mustEqual "SELECT NULL WHERE true")
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

    "be set as first object with SQL type and scale" in {
      lazy val s = statement()
      s.setObject(1, true, Types.BOOLEAN, 1)

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

    "be properly encoded as SQL" >> {
      "when true" in {
        (executeUpdate("TEST ?, y", Types.BOOLEAN, true).
          aka("SQL update") mustEqual "TEST true, y").
          and(executeQuery("SELECT ? WHERE true", Types.BOOLEAN, true).
            aka("SQL query") mustEqual "SELECT true WHERE true")

      }

      "when false" in {
        (executeUpdate("TEST ?, y", Types.BOOLEAN, false).
          aka("SQL update") mustEqual "TEST false, y").
          and(executeQuery("SELECT ? WHERE false", Types.BOOLEAN, false).
            aka("SQL query") mustEqual "SELECT false WHERE false")

      }
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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.toByte, Types.TINYINT, 1)

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

    "be property encoded as SQL" in {
      (executeUpdate("TEST ?, y", Types.TINYINT, 2.toByte).
        aka("SQL update") mustEqual "TEST 2, y").
        and(executeQuery("SELECT ? WHERE false", Types.TINYINT, 100.toByte).
          aka("SQL query") mustEqual "SELECT 100 WHERE false")

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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.toShort, Types.SMALLINT, 1)

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

    "be property encoded as SQL" in {
      (executeUpdate("TEST ?, y", Types.SMALLINT, 5.toShort).
        aka("SQL update") mustEqual "TEST 5, y").
        and(executeQuery("SELECT ? WHERE false", Types.SMALLINT, 256.toShort).
          aka("SQL query") mustEqual "SELECT 256 WHERE false")

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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1, Types.INTEGER, 1)

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

    "be property encoded as SQL" in {
      (executeUpdate("TEST ?, y", Types.INTEGER, 7).
        aka("SQL update") mustEqual "TEST 7, y").
        and(executeQuery("SELECT ? WHERE false", Types.INTEGER, 1001).
          aka("SQL query") mustEqual "SELECT 1001 WHERE false")

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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.toLong, Types.BIGINT, 2)

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

    "be property encoded as SQL" in {
      (executeUpdate("TEST ?, y", Types.BIGINT, 9.toLong).
        aka("SQL update") mustEqual "TEST 9, y").
        and(executeQuery("SELECT ? WHERE false", Types.BIGINT, 67598.toLong).
          aka("SQL query") mustEqual "SELECT 67598 WHERE false")

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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.2.toFloat, Types.FLOAT, 3)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.FLOAT).
        and(m.getScale(1) aka "scale" mustEqual 3)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.2.toFloat)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.FLOAT).
        and(m.getScale(1) aka "scale" mustEqual 1)

    }

    "be set as REAL null object" in {
      lazy val s = statement()
      s.setObject(1, null, Types.REAL)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.REAL)

    }

    "be set as REAL object" in {
      lazy val s = statement()
      s.setObject(1, 1.23f, Types.REAL)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.REAL).
        and(m.getScale(1) aka "scale" mustEqual 2)

    }

    "be set as REAL object with scale" in {
      lazy val s = statement()
      s.setObject(1, 1.23f, Types.REAL, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.REAL).
        and(m.getScale(1) aka "scale" mustEqual 1)

    }

    "be property encoded as SQL" >> {
      "when FLOAT" in {
        (executeUpdate("TEST ?, y", Types.FLOAT, 1.23.toFloat).
          aka("SQL update") mustEqual "TEST 1.23, y").
          and(executeQuery("SELECT ? WHERE false", Types.FLOAT, 34.561.toFloat).
            aka("SQL query") mustEqual "SELECT 34.561 WHERE false")

      }

      "when REAL" in {
        (executeUpdate("TEST ?, y", Types.REAL, 1.23.toFloat).
          aka("SQL update") mustEqual "TEST 1.23, y").
          and(executeQuery("SELECT ? WHERE false", Types.REAL, 34.561.toFloat).
            aka("SQL query") mustEqual "SELECT 34.561 WHERE false")

      }
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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.234.toDouble, Types.DOUBLE, 5)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.DOUBLE).
        and(m.getScale(1) aka "scale" mustEqual 5)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.234.toDouble)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.DOUBLE).
        and(m.getScale(1) aka "scale" mustEqual 3)

    }

    "be encoded as SQL" in {
      (executeUpdate("TEST ?, y", Types.DOUBLE, 1.23.toDouble).
        aka("SQL update") mustEqual "TEST 1.23, y").
        and(executeQuery("SELECT ? WHERE false", Types.DOUBLE, 34.561.toDouble).
          aka("SQL query") mustEqual "SELECT 34.561 WHERE false")

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

    "be set as first numeric object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, new java.math.BigDecimal("1.2345678"), Types.NUMERIC, 2)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" mustEqual 1).
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.NUMERIC).
        and(m.getScale(1) aka "scale" mustEqual 2)

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

    "be encoded as SQL" in {
      (executeUpdate("TEST ?, y", Types.DECIMAL, 
        new java.math.BigDecimal("876.78")).
        aka("SQL update") mustEqual "TEST 876.78, y").
        and(executeQuery("SELECT ? WHERE false", Types.DECIMAL, 
          new java.math.BigDecimal("9007.2")).
          aka("SQL query") mustEqual "SELECT 9007.2 WHERE false")

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

    "be set as first object with type and length" in {
      lazy val s = statement()
      s.setObject(1, "str", Types.VARCHAR, 2)

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

    "be encoded as SQL" >> {
      "when VARCHAR" in {
        (executeUpdate("TEST ?, y", Types.VARCHAR, "l'étoile").
          aka("SQL update") mustEqual "TEST 'l''étoile', y").
          and(executeQuery("SELECT ? WHERE false", Types.VARCHAR, "val").
            aka("SQL query") mustEqual "SELECT 'val' WHERE false")

      }

      "when LONGVARCHAR" in {
        (executeUpdate("TEST ?, y", Types.LONGVARCHAR, "l'étoile").
          aka("SQL update") mustEqual "TEST 'l''étoile', y").
          and(executeQuery("SELECT ? WHERE false", Types.LONGVARCHAR, "val").
            aka("SQL query") mustEqual "SELECT 'val' WHERE false")

      }
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

    "not be supported passed as object with scale (VARBINARY)" in {
      statement().setObject(1, Array[Byte](), Types.VARBINARY, 1).
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

    "be set as first parameter with calendar" in {
      lazy val s = statement()
      s.setDate(1, new java.sql.Date(1, 1, 1), java.util.Calendar.getInstance)

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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Date(1, 1, 1), Types.DATE, 1)

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

    "be encoded as SQL" >> {
      "with default TZ" in {
        (executeUpdate("TEST ?, y", Types.DATE, new java.sql.Date(1, 2, 3)).
          aka("SQL update") mustEqual "TEST DATE '1901-03-03+00:00', y").
          and(executeQuery("SELECT ? WHERE false", Types.DATE,
            new java.sql.Date(13, 10, 5)).aka("SQL query").
            mustEqual("SELECT DATE '1913-11-05+00:00' WHERE false"))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")

        (executeUpdate("TEST ?, y", Types.DATE, 
          (new java.sql.Date(1, 2, 3) -> tz)).
          aka("SQL update") mustEqual "TEST DATE '1901-03-03+01:00', y").
          and(executeQuery("SELECT ? WHERE false", Types.DATE,
            (new java.sql.Date(13, 10, 5) -> tz)).aka("SQL query").
            mustEqual("SELECT DATE '1913-11-05+01:00' WHERE false"))

      }
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

    "be set as first parameter with calendar" in {
      lazy val s = statement()
      s.setTime(1, new java.sql.Time(1, 1, 1), java.util.Calendar.getInstance)

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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Time(1, 1, 1), Types.TIME, 1)

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

    "be encoded as SQL" >> {
      "with default TZ" in {
        (executeUpdate("TEST ?, y", Types.TIME, new java.sql.Time(1, 2, 3)).
          aka("SQL update") mustEqual "TEST TIME '01:02:03.0+00:00', y").
          and(executeQuery("SELECT ? WHERE false", Types.TIME,
            new java.sql.Time(13, 10, 5)).aka("SQL query").
            mustEqual("SELECT TIME '13:10:05.0+00:00' WHERE false"))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")

        (executeUpdate("TEST ?, y", Types.TIME, 
          (new java.sql.Time(1, 2, 3) -> tz)).
          aka("SQL update") mustEqual "TEST TIME '01:02:03.0+01:00', y").
          and(executeQuery("SELECT ? WHERE false", Types.TIME,
            (new java.sql.Time(13, 10, 5) -> tz)).aka("SQL query").
            mustEqual("SELECT TIME '13:10:05.0+01:00' WHERE false"))

      }
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

    "be set as first parameter with calendar" in {
      lazy val s = statement()
      s.setTimestamp(1, new java.sql.Timestamp(1l), 
        java.util.Calendar.getInstance)

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

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Timestamp(1l), Types.TIMESTAMP, 1)

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

    "be encoded as SQL" >> {
      "with default TZ" in {
        (executeUpdate("TEST ?, y", Types.TIMESTAMP, 
          new java.sql.Timestamp(1, 2, 3, 4, 5, 6, 7)).
          aka("SQL update").
          mustEqual("TEST TIMESTAMP '1901-03-03T04:05:06.0+00:00', y")).
          and(executeQuery("SELECT ? WHERE false", Types.TIMESTAMP,
            new java.sql.Timestamp(13, 10, 5, 1, 1, 45, 0)).aka("SQL query").
            mustEqual(
              "SELECT TIMESTAMP '1913-11-05T01:01:45.0+00:00' WHERE false"))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")

        (executeUpdate("TEST ?, y", Types.TIMESTAMP, 
          (new java.sql.Timestamp(1, 2, 3, 4, 5, 6, 7) -> tz)).
          aka("SQL update").
          mustEqual("TEST TIMESTAMP '1901-03-03T04:05:06.0+01:00', y")).
          and(executeQuery("SELECT ? WHERE false", Types.TIMESTAMP,
            (new java.sql.Timestamp(13, 10, 5, 1, 1, 45, 0) -> tz)).
            aka("SQL query").
            mustEqual(
              "SELECT TIMESTAMP '1913-11-05T01:01:45.0+01:00' WHERE false"))

      }
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

  "Query execution" should {
    lazy val h = new StatementHandler {
      def getGeneratedKeys = null
      def isQuery(s: String) = true
      def whenSQLUpdate(s: String,p:Params) = -1
      def whenSQLQuery(s: String,p:Params) = {
        AbstractResultSet.EMPTY
      }
    }

    "be detected" in {
      statement(h = h).execute() aka "query" must beTrue
    }

    "return resultset" in {
      lazy val query = statement(h = h).executeQuery()

      (query aka "execution" must not(throwA[SQLException])).
        and(query aka "resultset" must not beNull)
    }

    "fail with update statement" in {
      lazy val s = statement()

      s.executeQuery() aka "query" must throwA[SQLException]("Not a query")
    }
  }

  "Update execution" should {
    "be detected" in {
      !statement().execute() aka "update" must beTrue
    }

    "return update count" in {
      statement().executeUpdate() aka "execution" must not(throwA[SQLException])
    }

    "fail with query statement" in {
      lazy val h = new StatementHandler {
        def getGeneratedKeys = null
        def isQuery(s: String) = true
        def whenSQLUpdate(s: String,p:Params) = -1
        def whenSQLQuery(s: String,p:Params) = {
          AbstractResultSet.EMPTY
        }
      }

      statement(h = h).executeUpdate() aka "update" must throwA[SQLException](
        message = "Cannot update with query")

    }
  }

  "Execution" should {
    lazy val h = new StatementHandler {
      def getGeneratedKeys = null
      def isQuery(s: String) = true
      def whenSQLUpdate(s: String,p:Params) = -1
      def whenSQLQuery(s: String,p:Params) = {
        AbstractResultSet.EMPTY
      }
    }

    "fail with missing parameter at start" in {
      lazy val u = statement()
      lazy val q = statement(h = h)

      u.setString(2, "Test")
      q.setFloat(2, 1.23f)

      (u.executeUpdate() aka "update" must throwA[SQLException](
        message = "Missing parameter value: 1")).
        and(u.execute() aka "update" must throwA[SQLException](
          message = "Missing parameter value: 1")).
        and(q.executeQuery() aka "query" must throwA[SQLException](
          message = "Missing parameter value: 1")).
        and(q.execute() aka "query" must throwA[SQLException](
          message = "Missing parameter value: 1"))
    }

    "fail with missing parameter at middle" in {
      lazy val u = statement()
      lazy val q = statement(h = h)

      u.setNull(1, Types.LONGVARCHAR)
      u.setString(3, "Test")

      q.setLong(1, 1.toLong)
      q.setFloat(3, 1.23f)

      (u.executeUpdate() aka "update" must throwA[SQLException](
        message = "Missing parameter value: 2")).
        and(u.execute() aka "update" must throwA[SQLException](
          message = "Missing parameter value: 2")).
        and(q.executeQuery() aka "query" must throwA[SQLException](
          message = "Missing parameter value: 2")).
        and(q.execute() aka "query" must throwA[SQLException](
          message = "Missing parameter value: 2"))
    }
  }

  // ---

  def statement(c: Connection = defaultCon, s: String = "TEST", h: StatementHandler = defaultHandler.getStatementHandler) = new PreparedStatement(c, s, h)

  def executeUpdate[A](s: String, t: Int, v: A, c: Connection = defaultCon)(implicit set: SetParam[A]): String = {
    var sql: String = null
    lazy val h = new StatementHandler {
      def getGeneratedKeys = null
      def isQuery(s: String) = false
      def whenSQLUpdate(s: String,p:Params) = { sql = s; 1 }
      def whenSQLQuery(s: String,p:Params) = AbstractResultSet.EMPTY
    }
    val st = statement(c, s, h)

    set(st, 1, v, t)

    st.executeUpdate()
    sql
  }

  def executeQuery[A](s: String, t: Int, v: A, c: Connection = defaultCon)(implicit set: SetParam[A]): String = {
    var sql: String = null
    lazy val h = new StatementHandler {
      def getGeneratedKeys = null
      def isQuery(s: String) = true
      def whenSQLUpdate(s: String,p:Params) = -1
      def whenSQLQuery(s: String,p:Params) = { 
        sql = s; AbstractResultSet.EMPTY 
      }
    }
    val st = statement(c, s, h)

    set(st, 1, v, t)

    st.executeQuery()
    sql
  }

  val jdbcUrl = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    "jdbc:acolyte:test"
  }

  lazy val defaultCon = new acolyte.Connection(jdbcUrl, null, defaultHandler)
  lazy val defaultHandler = EmptyConnectionHandler
}

sealed trait SetParam[A] {
  def apply(s: PreparedStatement, i: Int, p: A, t: Int): PreparedStatement
}

sealed trait Setters {
  import java.math.BigDecimal
  import java.util.Calendar
  import java.sql.{ Date, Time, Timestamp }

  implicit def SetNull: SetParam[Null] = new SetParam[Null] {
    def apply(s: PreparedStatement, i: Int, p: Null, t: Int) = {
      s.setNull(i, t)
      s
    }
  }

  implicit def SetBool: SetParam[Boolean] = new SetParam[Boolean] {
    def apply(s: PreparedStatement, i: Int, p: Boolean, t: Int) = {
      s.setBoolean(i, p)
      s
    }
  }

  implicit def SetByte: SetParam[Byte] = new SetParam[Byte] {
    def apply(s: PreparedStatement, i: Int, p: Byte, t: Int) = {
      s.setByte(i, p)
      s
    }
  }

  implicit def SetShort: SetParam[Short] = new SetParam[Short] {
    def apply(s: PreparedStatement, i: Int, p: Short, t: Int) = {
      s.setShort(i, p)
      s
    }
  }

  implicit def SetInt: SetParam[Int] = new SetParam[Int] {
    def apply(s: PreparedStatement, i: Int, p: Int, t: Int) = {
      s.setInt(i, p)
      s
    }
  }

  implicit def SetLong: SetParam[Long] = new SetParam[Long] {
    def apply(s: PreparedStatement, i: Int, p: Long, t: Int) = {
      s.setLong(i, p)
      s
    }
  }

  implicit def SetFloat: SetParam[Float] = new SetParam[Float] {
    def apply(s: PreparedStatement, i: Int, p: Float, t: Int) = {
      s.setFloat(i, p)
      s
    }
  }

  implicit def SetDouble: SetParam[Double] = new SetParam[Double] {
    def apply(s: PreparedStatement, i: Int, p: Double, t: Int) = {
      s.setDouble(i, p)
      s
    }
  }

  implicit def SetBigDecimal: SetParam[BigDecimal] = new SetParam[BigDecimal] {
    def apply(s: PreparedStatement, i: Int, p: BigDecimal, t: Int) = {
      s.setBigDecimal(i, p);
      s
    }
  }

  implicit def SetString: SetParam[String] = new SetParam[String] {
    def apply(s: PreparedStatement, i: Int, p: String, t: Int) = {
      s.setString(i, p)
      s
    }
  }

  implicit def SetDate: SetParam[Date] = new SetParam[Date] {
    def apply(s: PreparedStatement, i: Int, p: Date, t: Int) = {
      s.setDate(i, p)
      s
    }
  }

  implicit def SetDateWithTZ: SetParam[(Date, TimeZone)] = 
    new SetParam[(Date, TimeZone)] {
    def apply(s: PreparedStatement, i: Int, p: (Date, TimeZone), t: Int) = {
      val (d, tz) = p
      val cal = Calendar.getInstance()
      
      cal.setTimeZone(tz)

      s.setDate(i, d, cal)
      s
    }
  }

  implicit def SetTime: SetParam[Time] = new SetParam[Time] {
    def apply(s: PreparedStatement, i: Int, p: Time, t: Int) = {
      s.setTime(i, p)
      s
    }
  }

  implicit def SetTimeWithTZ: SetParam[(Time, TimeZone)] = 
    new SetParam[(Time, TimeZone)] {
    def apply(s: PreparedStatement, i: Int, p: (Time, TimeZone), t: Int) = {
      val (d, tz) = p
      val cal = Calendar.getInstance()
      
      cal.setTimeZone(tz)

      s.setTime(i, d, cal)
      s
    }
  }

  implicit def SetTimestamp: SetParam[Timestamp] = new SetParam[Timestamp] {
    def apply(s: PreparedStatement, i: Int, p: Timestamp, t: Int) = {
      s.setTimestamp(i, p)
      s
    }
  }

  implicit def SetTimestampWithTZ: SetParam[(Timestamp, TimeZone)] = 
    new SetParam[(Timestamp, TimeZone)] {
    def apply(s: PreparedStatement, i: Int, p: (Timestamp, TimeZone), t: Int) = {
      val (d, tz) = p
      val cal = Calendar.getInstance()
      
      cal.setTimeZone(tz)

      s.setTimestamp(i, d, cal)
      s
    }
  }
}
