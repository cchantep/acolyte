package acolyte

import java.util.TimeZone // Force test default TZ

import java.sql.{ SQLException, SQLFeatureNotSupportedException, Types }

import org.specs2.mutable.Specification

import acolyte.StatementHandler.Parameter
import acolyte.test.{ EmptyConnectionHandler, Params }

object PreparedStatementSpec
    extends Specification with StatementSpecification[PreparedStatement] {

  "Prepared statement specification" title

  def statement(c: Connection = defaultCon, s: String = "TEST", h: StatementHandler = defaultHandler.getStatementHandler) = new PreparedStatement(c, s, h)

}

trait StatementSpecification[S <: PreparedStatement] extends Setters {
  specs: Specification ⇒

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
        aka("SQL update") mustEqual ("TEST ?, y" -> null)).
        and(executeQuery("SELECT ? WHERE true", Types.FLOAT, null).
          aka("SQL query") mustEqual ("SELECT ? WHERE true" -> null))
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

    "be properly prepared" >> {
      "when true" in {
        (executeUpdate("TEST ?, y", Types.BOOLEAN, true).
          aka("SQL update") mustEqual ("TEST ?, y" -> true)).
          and(executeQuery("SELECT ? WHERE true", Types.BOOLEAN, true).
            aka("SQL query") mustEqual ("SELECT ? WHERE true" -> true))

      }

      "when false" in {
        (executeUpdate("TEST ?, y", Types.BOOLEAN, false).
          aka("SQL update") mustEqual ("TEST ?, y" -> false)).
          and(executeQuery("SELECT ? WHERE false", Types.BOOLEAN, false).
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> false))

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

    "be properly prepared" in {
      (executeUpdate("TEST ?, y", Types.TINYINT, 2.toByte).
        aka("SQL update") mustEqual ("TEST ?, y" -> 2.toByte)).
        and(executeQuery("SELECT ? WHERE false", Types.TINYINT, 100.toByte).
          aka("SQL query") mustEqual ("SELECT ? WHERE false" -> 100.toByte))

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

    "be property prepared" in {
      (executeUpdate("TEST ?, y", Types.SMALLINT, 5.toShort).
        aka("SQL update") mustEqual ("TEST ?, y" -> 5.toShort)).
        and(executeQuery("SELECT ? WHERE false", Types.SMALLINT, 256.toShort).
          aka("SQL query") mustEqual ("SELECT ? WHERE false" -> 256.toShort))

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

    "be property prepared" in {
      (executeUpdate("TEST ?, y", Types.INTEGER, 7).
        aka("SQL update") mustEqual ("TEST ?, y" -> 7)).
        and(executeQuery("SELECT ? WHERE false", Types.INTEGER, 1001).
          aka("SQL query") mustEqual ("SELECT ? WHERE false" -> 1001))

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

    "be property prepared" in {
      (executeUpdate("TEST ?, y", Types.BIGINT, 9.toLong).
        aka("SQL update") mustEqual ("TEST ?, y" -> 9.toLong)).
        and(executeQuery("SELECT ? WHERE false", Types.BIGINT, 67598.toLong).
          aka("SQL query") mustEqual ("SELECT ? WHERE false" -> 67598.toLong))

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

    "be property prepared" >> {
      "when FLOAT" in {
        (executeUpdate("TEST ?, y", Types.FLOAT, 1.23.toFloat).
          aka("SQL update") mustEqual ("TEST ?, y" -> 1.23.toFloat)).
          and(executeQuery("SELECT ? WHERE false", Types.FLOAT, 34.561.toFloat).
            aka("SQL query").
            mustEqual(("SELECT ? WHERE false" -> 34.561.toFloat)))

      }

      "when REAL" in {
        (executeUpdate("TEST ?, y", Types.REAL, 1.23.toFloat).
          aka("SQL update") mustEqual ("TEST ?, y" -> 1.23.toFloat)).
          and(executeQuery("SELECT ? WHERE false", Types.REAL, 34.561.toFloat).
            aka("SQL query").
            mustEqual(("SELECT ? WHERE false" -> 34.561.toFloat)))

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

    "be prepared" in {
      (executeUpdate("TEST ?, y", Types.DOUBLE, 1.23.toDouble).
        aka("SQL update") mustEqual ("TEST ?, y", 1.23.toDouble)).
        and(executeQuery("SELECT ? WHERE false", Types.DOUBLE, 34.561.toDouble).
          aka("SQL query") mustEqual ("SELECT ? WHERE false", 34.561.toDouble))

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

    "be prepared" in {
      val d1 = new java.math.BigDecimal("876.78")
      val d2 = new java.math.BigDecimal("9007.2")

      (executeUpdate("TEST ?, y", Types.DECIMAL, d1).
        aka("SQL update") mustEqual ("TEST ?, y" -> d1)).
        and(executeQuery("SELECT ? WHERE false", Types.DECIMAL, d2).
          aka("SQL query") mustEqual ("SELECT ? WHERE false" -> d2))

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

    "be prepared" >> {
      "when VARCHAR" in {
        (executeUpdate("TEST ?, y", Types.VARCHAR, "l'étoile").
          aka("SQL update") mustEqual ("TEST ?, y" -> "l'étoile")).
          and(executeQuery("SELECT ? WHERE false", Types.VARCHAR, "val").
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> "val"))

      }

      "when LONGVARCHAR" in {
        (executeUpdate("TEST ?, y", Types.LONGVARCHAR, "l'étoile").
          aka("SQL update") mustEqual ("TEST ?, y" -> "l'étoile")).
          and(executeQuery("SELECT ? WHERE false", Types.LONGVARCHAR, "val").
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> "val"))

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

    "be prepared" >> {
      "with default TZ" in {
        val d1 = new java.sql.Date(1, 2, 3)
        val d2 = new java.sql.Date(13, 10, 5)

        (executeUpdate("TEST ?, y", Types.DATE, d1).
          aka("SQL update") mustEqual ("TEST ?, y" -> d1)).
          and(executeQuery("SELECT ? WHERE false", Types.DATE, d2).
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> d2))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")
        val d1 = new java.sql.Date(1, 2, 3)
        val d2 = new java.sql.Date(13, 10, 5)

        (executeUpdate("TEST ?, y", Types.DATE, (d1 -> tz)).
          aka("SQL update") mustEqual ("TEST ?, y" -> (d1, tz))).
          and(executeQuery("SELECT ? WHERE false", Types.DATE, (d2 -> tz)).
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> (d2, tz)))

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

    "be prepared" >> {
      "with default TZ" in {
        val t1 = new java.sql.Time(1, 2, 3)
        val t2 = new java.sql.Time(13, 10, 5)

        (executeUpdate("TEST ?, y", Types.TIME, t1).
          aka("SQL update") mustEqual ("TEST ?, y" -> t1)).
          and(executeQuery("SELECT ? WHERE false", Types.TIME, t2).
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> t2))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")
        val t1 = new java.sql.Time(1, 2, 3)
        val t2 = new java.sql.Time(13, 10, 5)

        (executeUpdate("TEST ?, y", Types.TIME, (t1 -> tz)).
          aka("SQL update") mustEqual ("TEST ?, y" -> (t1, tz))).
          and(executeQuery("SELECT ? WHERE false", Types.TIME, (t2 -> tz)).
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> (t2, tz)))

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

    "be prepared" >> {
      "with default TZ" in {
        val ts1 = new java.sql.Timestamp(1, 2, 3, 4, 5, 6, 7)
        val ts2 = new java.sql.Timestamp(13, 10, 5, 1, 1, 45, 0)

        (executeUpdate("TEST ?, y", Types.TIMESTAMP, ts1).
          aka("SQL update") mustEqual ("TEST ?, y" -> ts1)).
          and(executeQuery("SELECT ? WHERE false", Types.TIMESTAMP, ts2).
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> ts2))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")
        val ts1 = new java.sql.Timestamp(1, 2, 3, 4, 5, 6, 7)
        val ts2 = new java.sql.Timestamp(13, 10, 5, 1, 1, 45, 0)

        (executeUpdate("TEST ?, y", Types.TIMESTAMP, (ts1 -> tz)).
          aka("SQL update") mustEqual ("TEST ?, y" -> (ts1, tz))).
          and(executeQuery("SELECT ? WHERE false", Types.TIMESTAMP, (ts2 -> tz)).
            aka("SQL query") mustEqual ("SELECT ? WHERE false" -> (ts2, tz)))

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
      def whenSQLUpdate(s: String, p: Params) = UpdateResult.Nothing
      def whenSQLQuery(s: String, p: Params) = {
        RowLists.rowList1(classOf[String]).asResult
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
        def whenSQLUpdate(s: String, p: Params) = UpdateResult.Nothing
        def whenSQLQuery(s: String, p: Params) = {
          RowLists.rowList1(classOf[String]).asResult
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
      def whenSQLUpdate(s: String, p: Params) = UpdateResult.Nothing
      def whenSQLQuery(s: String, p: Params) = {
        RowLists.rowList1(classOf[String]).asResult
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

  "Warning" should {
    lazy val warning = new java.sql.SQLWarning("TEST")

    "be found for query" in {
      lazy val h = new StatementHandler {
        def getGeneratedKeys = null
        def isQuery(s: String) = true
        def whenSQLUpdate(s: String, p: Params) = sys.error("Not")
        def whenSQLQuery(s: String, p: Params) =
          RowLists.rowList1(classOf[String]).asResult.withWarning(warning)

      }

      lazy val s = statement(h = h)
      s.executeQuery("TEST")

      s.getWarnings aka "warning" mustEqual warning
    }

    "be found for update" in {
      lazy val h = new StatementHandler {
        def getGeneratedKeys = null
        def isQuery(s: String) = false
        def whenSQLQuery(s: String, p: Params) = sys.error("Not")
        def whenSQLUpdate(s: String, p: Params) =
          UpdateResult.Nothing.withWarning(warning)

      }

      lazy val s = statement(h = h)
      s.executeUpdate()

      s.getWarnings aka "warning" mustEqual warning
    }
  }

  // ---

  def executeUpdate[A](s: String, t: Int, v: A, c: Connection = defaultCon)(implicit stmt: StatementParam[A]): (String, A) = {
    var sql: String = null
    var param: Parameter = null
    lazy val h = new StatementHandler {
      def getGeneratedKeys = null
      def isQuery(s: String) = false
      def whenSQLUpdate(s: String, p: Params) = {
        sql = s; param = p.get(0); new UpdateResult(1)
      }
      def whenSQLQuery(s: String, p: Params) =
        RowLists.rowList1(classOf[String]).asResult
    }
    val st = statement(c, s, h)

    stmt.set(st, 1, v, t)

    st.executeUpdate()
    (sql -> stmt.get(param))
  }

  def executeQuery[A](s: String, t: Int, v: A, c: Connection = defaultCon)(implicit stmt: StatementParam[A]): (String, A) = {
    var sql: String = null
    var param: Parameter = null
    lazy val h = new StatementHandler {
      def getGeneratedKeys = null
      def isQuery(s: String) = true
      def whenSQLUpdate(s: String, p: Params) = UpdateResult.Nothing
      def whenSQLQuery(s: String, p: Params) = {
        sql = s; param = p.get(0); RowLists.rowList1(classOf[String]).asResult
      }
    }
    val st = statement(c, s, h)

    stmt.set(st, 1, v, t)

    st.executeQuery()
    (sql -> stmt.get(param))
  }

  val jdbcUrl = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    "jdbc:acolyte:test"
  }

  def statement(c: Connection = defaultCon, s: String = "TEST", h: StatementHandler = defaultHandler.getStatementHandler): S

  lazy val defaultCon = new acolyte.Connection(jdbcUrl, null, defaultHandler)
  lazy val defaultHandler = EmptyConnectionHandler
}

sealed trait StatementParam[A] {
  def set(s: PreparedStatement, i: Int, p: A, t: Int): PreparedStatement
  def get(p: Parameter): A
}

sealed trait Setters {
  import java.math.BigDecimal
  import java.util.Calendar
  import java.sql.{ Date, Time, Timestamp }
  import org.apache.commons.lang3.tuple.ImmutablePair

  implicit def StmtNull: StatementParam[Null] = new StatementParam[Null] {
    def set(s: PreparedStatement, i: Int, p: Null, t: Int) = {
      s.setNull(i, t)
      s
    }

    def get(p: Parameter): Null = null
  }

  implicit def StmtBool: StatementParam[Boolean] = new StatementParam[Boolean] {
    def set(s: PreparedStatement, i: Int, p: Boolean, t: Int) = {
      s.setBoolean(i, p)
      s
    }

    def get(p: Parameter): Boolean = p.right.asInstanceOf[Boolean]
  }

  implicit def StmtByte: StatementParam[Byte] = new StatementParam[Byte] {
    def set(s: PreparedStatement, i: Int, p: Byte, t: Int) = {
      s.setByte(i, p)
      s
    }

    def get(p: Parameter): Byte = p.right.asInstanceOf[Byte]
  }

  implicit def StmtShort: StatementParam[Short] = new StatementParam[Short] {
    def set(s: PreparedStatement, i: Int, p: Short, t: Int) = {
      s.setShort(i, p)
      s
    }

    def get(p: Parameter): Short = p.right.asInstanceOf[Short]
  }

  implicit def StmtInt: StatementParam[Int] = new StatementParam[Int] {
    def set(s: PreparedStatement, i: Int, p: Int, t: Int) = {
      s.setInt(i, p)
      s
    }

    def get(p: Parameter): Int = p.right.asInstanceOf[Int]
  }

  implicit def StmtLong: StatementParam[Long] = new StatementParam[Long] {
    def set(s: PreparedStatement, i: Int, p: Long, t: Int) = {
      s.setLong(i, p)
      s
    }

    def get(p: Parameter): Long = p.right.asInstanceOf[Long]
  }

  implicit def StmtFloat: StatementParam[Float] = new StatementParam[Float] {
    def set(s: PreparedStatement, i: Int, p: Float, t: Int) = {
      s.setFloat(i, p)
      s
    }

    def get(p: Parameter): Float = p.right.asInstanceOf[Float]
  }

  implicit def StmtDouble: StatementParam[Double] = new StatementParam[Double] {
    def set(s: PreparedStatement, i: Int, p: Double, t: Int) = {
      s.setDouble(i, p)
      s
    }

    def get(p: Parameter): Double = p.right.asInstanceOf[Double]
  }

  implicit def StmtBigDecimal: StatementParam[BigDecimal] =
    new StatementParam[BigDecimal] {
      def set(s: PreparedStatement, i: Int, p: BigDecimal, t: Int) = {
        s.setBigDecimal(i, p);
        s
      }

      def get(p: Parameter): BigDecimal = p.right.asInstanceOf[BigDecimal]
    }

  implicit def StmtString: StatementParam[String] = new StatementParam[String] {
    def set(s: PreparedStatement, i: Int, p: String, t: Int) = {
      s.setString(i, p)
      s
    }

    def get(p: Parameter): String = p.right.asInstanceOf[String]
  }

  implicit def StmtDate: StatementParam[Date] = new StatementParam[Date] {
    def set(s: PreparedStatement, i: Int, p: Date, t: Int) = {
      s.setDate(i, p)
      s
    }

    def get(p: Parameter): Date = p.right.asInstanceOf[Date]
  }

  implicit def StmtDateWithTZ: StatementParam[(Date, TimeZone)] =
    new StatementParam[(Date, TimeZone)] {
      def set(s: PreparedStatement, i: Int, p: (Date, TimeZone), t: Int) = {
        val (d, tz) = p
        val cal = Calendar.getInstance()

        cal.setTimeZone(tz)

        s.setDate(i, d, cal)
        s
      }

      def get(p: Parameter): (Date, TimeZone) = {
        val pair = p.right.asInstanceOf[ImmutablePair[Date, TimeZone]]
        (pair.left -> pair.right)
      }
    }

  implicit def StmtTime: StatementParam[Time] = new StatementParam[Time] {
    def set(s: PreparedStatement, i: Int, p: Time, t: Int) = {
      s.setTime(i, p)
      s
    }

    def get(p: Parameter): Time = p.right.asInstanceOf[Time]
  }

  implicit def StmtTimeWithTZ: StatementParam[(Time, TimeZone)] =
    new StatementParam[(Time, TimeZone)] {
      def set(s: PreparedStatement, i: Int, p: (Time, TimeZone), t: Int) = {
        val (d, tz) = p
        val cal = Calendar.getInstance()

        cal.setTimeZone(tz)

        s.setTime(i, d, cal)
        s
      }

      def get(p: Parameter): (Time, TimeZone) = {
        val pair = p.right.asInstanceOf[ImmutablePair[Time, TimeZone]]
        (pair.left -> pair.right)
      }
    }

  implicit def StmtTimestamp: StatementParam[Timestamp] =
    new StatementParam[Timestamp] {
      def set(s: PreparedStatement, i: Int, p: Timestamp, t: Int) = {
        s.setTimestamp(i, p)
        s
      }

      def get(p: Parameter): Timestamp = p.right.asInstanceOf[Timestamp]
    }

  implicit def StmtTimestampWithTZ: StatementParam[(Timestamp, TimeZone)] =
    new StatementParam[(Timestamp, TimeZone)] {
      def set(s: PreparedStatement, i: Int, p: (Timestamp, TimeZone), t: Int) = {
        val (d, tz) = p
        val cal = Calendar.getInstance()

        cal.setTimeZone(tz)

        s.setTimestamp(i, d, cal)
        s
      }

      def get(p: Parameter): (Timestamp, TimeZone) = {
        val pair = p.right.asInstanceOf[ImmutablePair[Timestamp, TimeZone]]
        (pair.left -> pair.right)
      }
    }
}
