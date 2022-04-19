package acolyte.jdbc

import java.io.{ ByteArrayInputStream, InputStream }

import java.util.{ Properties, TimeZone }

import java.sql.{
  BatchUpdateException,
  SQLException,
  SQLFeatureNotSupportedException,
  Types,
  ResultSet
}
import java.sql.Statement.EXECUTE_FAILED

import scala.collection.JavaConverters

import org.specs2.mutable.Specification

import acolyte.jdbc.StatementHandler.Parameter
import acolyte.jdbc.test.{ EmptyConnectionHandler, Params }
import org.apache.commons.io.IOUtils.contentEquals

object PreparedStatementSpec
  extends Specification with StatementSpecification[PreparedStatement] {

  "Prepared statement specification".title

  def statement(c: Connection = defaultCon, s: String = "TEST", h: StatementHandler = defaultHandler.getStatementHandler) = new PreparedStatement(c, s, java.sql.Statement.RETURN_GENERATED_KEYS, null, null, h)

}

trait StatementSpecification[S <: PreparedStatement] extends Setters {
  specs: Specification =>

  "Test time zone" should {
    "be UTC" in {
      TimeZone.getDefault aka "default TZ" must_=== TimeZone.getTimeZone("UTC")
    }
  }

  "Statement" should {
    "not support resultset metadata" in {
      statement().getMetaData must throwA[SQLFeatureNotSupportedException]
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
        and(statement().setRef(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setClob(0, null.asInstanceOf[java.sql.Clob]).
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
        and(statement().setNClob(0, null, 1.toLong).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setSQLXML(0, null).
          aka("setter") must throwA[SQLFeatureNotSupportedException]).
        and(statement().setAsciiStream(0, null, 1.toLong).
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
        and(statement().setNClob(0, null.asInstanceOf[java.io.Reader]).
          aka("setter") must throwA[SQLFeatureNotSupportedException])

    }
  }

  "Batch" should {
    class Handler extends StatementHandler {
      var exed = Seq[(String, Array[Parameter])]()
      def isQuery(s: String) = false
      def whenSQLUpdate(s: String, p: Params) = {
        exed = exed :+ (s -> p.toArray(Array[Parameter]()))
        new UpdateResult(exed.size)
      }
      def whenSQLQuery(s: String, p: Params) = sys.error("TEST")
    }

    "not be added from raw SQL" in {
      statement().addBatch("RAW") aka "add batch" must throwA[SQLException](
        "Cannot add distinct SQL to prepared statement")
    }

    "not be added on closed statement" in {
      lazy val s = statement()
      s.close()

      s.addBatch() aka "add batch" must throwA[SQLException](
        message = "Statement is closed")
    }

    "be executed with 2 elements" in {
      val h = new Handler()
      lazy val s = statement(h = h)
      s.setString(1, "A"); s.setInt(2, 3); s.addBatch()
      s.setString(1, "B"); s.setInt(2, 4); s.addBatch()

      val a = Array[Parameter](
        Parameter.of(ParameterMetaData.Str, "A"),
        Parameter.of(ParameterMetaData.Int, 3))

      val b = Array[Parameter](
        Parameter.of(ParameterMetaData.Str, "B"),
        Parameter.of(ParameterMetaData.Int, 4))

      s.executeBatch() aka "batch execution" must_=== Array[Int](1, 2) and (
        h.exed aka "executed" must beLike {
          case ("TEST", x) :: ("TEST", y) :: Nil =>
            (x aka "x" must_=== a) and (y aka "y" must_=== b)
        })
    }

    "throw exception as error is raised while executing first element" in {
      val h = new Handler {
        override def whenSQLUpdate(s: String, p: Params) =
          sys.error("Batch error")
      }
      lazy val s = statement(h = h)
      s.setString(1, "A"); s.setInt(2, 3); s.addBatch()
      s.setString(1, "B"); s.setInt(2, 4); s.addBatch()

      s.executeBatch() aka "batch execution" must throwA[BatchUpdateException].
        like {
          case ex: BatchUpdateException =>
            (ex.getUpdateCounts aka "update count" must_=== Array[Int](
              EXECUTE_FAILED, EXECUTE_FAILED)).
              and(ex.getCause.getMessage aka "cause" must_=== "Batch error")
        }
    }

    "continue after error on first element (batch.continueOnError)" in {
      val props = new Properties()
      props.put("acolyte.batch.continueOnError", "true")

      var i = 0
      val h = new Handler {
        override def whenSQLUpdate(s: String, p: Params) = {
          i = i + 1
          if (i == 1) sys.error(s"Batch error: $i")
          new UpdateResult(i)
        }
      }
      lazy val s = statement(
        c = new acolyte.jdbc.Connection(jdbcUrl, props, defaultHandler), h = h)
      s.setString(1, "A"); s.setInt(2, 3); s.addBatch()
      s.setString(1, "B"); s.setInt(2, 4); s.addBatch()

      s.executeBatch() aka "batch execution" must throwA[BatchUpdateException].
        like {
          case ex: BatchUpdateException =>
            (ex.getUpdateCounts aka "update count" must_=== Array[Int](
              EXECUTE_FAILED, 2)).
              and(ex.getCause.getMessage aka "cause" must_=== "Batch error: 1")
        }
    }

    "throw exception as error is raised while executing second element" in {
      var i = 0
      val h = new Handler {
        override def whenSQLUpdate(s: String, p: Params) = {
          i = i + 1
          if (i == 2) sys.error(s"Batch error: $i")
          new UpdateResult(i)
        }
      }
      lazy val s = statement(h = h)
      s.setString(1, "A"); s.setInt(2, 3); s.addBatch()
      s.setString(1, "B"); s.setInt(2, 4); s.addBatch()

      s.executeBatch() aka "batch execution" must throwA[BatchUpdateException].
        like {
          case ex: BatchUpdateException =>
            (ex.getUpdateCounts aka "update count" must_=== Array[Int](
              1, EXECUTE_FAILED)).
              and(ex.getCause.getMessage aka "cause" must_=== "Batch error: 2")
        }
    }

    "throw exception executing second element (batch.continueOnError)" in {
      val props = new Properties()
      props.put("acolyte.batch.continueOnError", "true")

      var i = 0
      val h = new Handler {
        override def whenSQLUpdate(s: String, p: Params) = {
          i = i + 1
          if (i == 2) sys.error(s"Batch error: $i")
          new UpdateResult(i)
        }
      }
      lazy val s = statement(
        c = new acolyte.jdbc.Connection(jdbcUrl, props, defaultHandler), h = h)
      s.setString(1, "A"); s.setInt(2, 3); s.addBatch()
      s.setString(1, "B"); s.setInt(2, 4); s.addBatch()

      s.executeBatch() aka "batch execution" must throwA[BatchUpdateException].
        like {
          case ex: BatchUpdateException =>
            (ex.getUpdateCounts aka "update count" must_=== Array[Int](
              1, EXECUTE_FAILED)).
              and(ex.getCause.getMessage aka "cause" must_=== "Batch error: 2")
        }
    }

    "be cleared and not executed" in {
      val h = new Handler()
      lazy val s = statement(h = h)
      s.setString(1, "A"); s.setInt(2, 3); s.addBatch()
      s.setString(1, "B"); s.setInt(2, 4); s.addBatch()

      s.clearBatch() aka "clear batch" must not(throwA[SQLException]) and (
        h.exed.size aka "executed" must_=== 0)
    }
  }

  "Null" should {
    "be set as first parameter (VARCHAR)" in {
      lazy val s = statement()
      s.setNull(1, Types.VARCHAR)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.VARCHAR)

    }

    "be set as first parameter with type name VARCHAR" in {
      lazy val s = statement()
      s.setNull(1, Types.VARCHAR, "VARCHAR")

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.VARCHAR)

    }

    "be set as object" in {
      lazy val s = statement()
      s.setObject(1, null, Types.FLOAT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.FLOAT)

    }

    "be set as object with scale" in {
      lazy val s = statement()
      s.setObject(1, null, Types.FLOAT, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.FLOAT)

    }

    "cannot be set as object without SQL type" in {
      statement().setObject(1, null).
        aka("set null object") must throwA[SQLException](
          message = "Cannot set parameter from null object")

    }

    "fallback null object to null string parameter" in {
      val ps = new java.util.Properties()
      ps.put("acolyte.parameter.untypedNull", "true")

      val s = statement(connection(ps))
      s.setObject(1, null)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.VARCHAR)

    }

    "be NULL as SQL" in {
      (executeUpdate("TEST ?, y", Types.VARCHAR, null).
        aka("SQL update") must_=== ("TEST ?, y" -> null)).
        and(executeQuery("SELECT ? WHERE true", Types.FLOAT, null).
          aka("SQL query") must_=== ("SELECT ? WHERE true" -> null))
    }
  }

  "Array" should {
    val stringArray = ImmutableArray.getInstance(
      classOf[String],
      JavaConverters.seqAsJavaListConverter(List("A", "B")).asJava)

    "be set as first parameter" in {
      lazy val s = statement()
      s.setArray(1, stringArray)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.ARRAY)

    }

    "be set as first object with SQL type" in {
      lazy val s = statement()
      s.setObject(1, stringArray, Types.ARRAY)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.ARRAY)

    }

    "be set as first object with SQL type and scale" in {
      lazy val s = statement()
      s.setObject(1, stringArray, Types.ARRAY, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.ARRAY)

    }

    "be set as first object without SQL type" in {
      lazy val s = statement()
      s.setObject(1, stringArray)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.ARRAY)

    }

    "be properly prepared" in {
      (executeUpdate("TEST ?, y", Types.ARRAY, stringArray).
        aka("SQL update") must_=== ("TEST ?, y" -> stringArray)).
        and(executeQuery("SELECT ? WHERE true", Types.ARRAY, stringArray).
          aka("SQL query") must_=== ("SELECT ? WHERE true" -> stringArray))

    }
  }

  "Binary Large Object" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setBlob(1, Blob.Nil)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BLOB)

    }

    "be set as first object with SQL type" in {
      lazy val s = statement()
      s.setObject(1, Blob.Nil, Types.BLOB)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BLOB)

    }

    "be set as first object from stream with length" in {
      lazy val s = statement()
      s.setBlob(1, new ByteArrayInputStream(Array[Byte](1, 3, 5)), 2)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BLOB)

    }

    "be set as first object with SQL type and scale" in {
      lazy val s = statement()
      s.setObject(1, Blob.Nil, Types.BLOB, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BLOB)

    }

    "be set as first object without SQL type" in {
      lazy val s = statement()
      s.setObject(1, Blob.Nil)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BLOB)

    }

    "be properly prepared" in {
      (executeUpdate("TEST ?, y", Types.BLOB, Blob.Nil).
        aka("SQL update") must_=== ("TEST ?, y" -> Blob.Nil)).
        and(executeQuery("SELECT ? WHERE true", Types.BLOB, Blob.Nil).
          aka("SQL query") must_=== ("SELECT ? WHERE true" -> Blob.Nil))

    }
  }

  "Boolean" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setBoolean(1, true)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BOOLEAN)

    }

    "be set as first object with SQL type" in {
      lazy val s = statement()
      s.setObject(1, true, Types.BOOLEAN)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BOOLEAN)

    }

    "be set as first object with SQL type and scale" in {
      lazy val s = statement()
      s.setObject(1, true, Types.BOOLEAN, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BOOLEAN)

    }

    "be set as first object without SQL type" in {
      lazy val s = statement()
      s.setObject(1, true)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BOOLEAN)

    }

    "be properly prepared" >> {
      "when true" in {
        (executeUpdate("TEST ?, y", Types.BOOLEAN, true).
          aka("SQL update") must_=== ("TEST ?, y" -> true)).
          and(executeQuery("SELECT ? WHERE true", Types.BOOLEAN, true).
            aka("SQL query") must_=== ("SELECT ? WHERE true" -> true))

      }

      "when false" in {
        (executeUpdate("TEST ?, y", Types.BOOLEAN, false).
          aka("SQL update") must_=== ("TEST ?, y" -> false)).
          and(executeQuery("SELECT ? WHERE false", Types.BOOLEAN, false).
            aka("SQL query") must_=== ("SELECT ? WHERE false" -> false))

      }
    }
  }

  "Byte" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setByte(1, 1.toByte)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TINYINT)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.toByte, Types.TINYINT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TINYINT)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.toByte, Types.TINYINT, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TINYINT)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.toByte)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TINYINT)

    }

    "be properly prepared" in {
      (executeUpdate("TEST ?, y", Types.TINYINT, 2.toByte).
        aka("SQL update") must_=== ("TEST ?, y" -> 2.toByte)).
        and(executeQuery("SELECT ? WHERE false", Types.TINYINT, 100.toByte).
          aka("SQL query") must_=== ("SELECT ? WHERE false" -> 100.toByte))

    }
  }

  "Short" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setShort(1, 1.toShort)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.SMALLINT)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.toShort, Types.SMALLINT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.SMALLINT)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.toShort, Types.SMALLINT, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.SMALLINT)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.toShort)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.SMALLINT)

    }

    "be property prepared" in {
      (executeUpdate("TEST ?, y", Types.SMALLINT, 5.toShort).
        aka("SQL update") must_=== ("TEST ?, y" -> 5.toShort)).
        and(executeQuery("SELECT ? WHERE false", Types.SMALLINT, 256.toShort).
          aka("SQL query") must_=== ("SELECT ? WHERE false" -> 256.toShort))

    }
  }

  "Integer" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setInt(1, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.INTEGER)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1, Types.INTEGER)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.INTEGER)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1, Types.INTEGER, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.INTEGER)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.INTEGER)

    }

    "be property prepared" in {
      (executeUpdate("TEST ?, y", Types.INTEGER, 7).
        aka("SQL update") must_=== ("TEST ?, y" -> 7)).
        and(executeQuery("SELECT ? WHERE false", Types.INTEGER, 1001).
          aka("SQL query") must_=== ("SELECT ? WHERE false" -> 1001))

    }
  }

  "Long" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setLong(1, 1.toLong)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BIGINT)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.toLong, Types.BIGINT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BIGINT)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.toLong, Types.BIGINT, 2)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BIGINT)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.toLong)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BIGINT)

    }

    "be property prepared" in {
      (executeUpdate("TEST ?, y", Types.BIGINT, 9.toLong).
        aka("SQL update") must_=== ("TEST ?, y" -> 9.toLong)).
        and(executeQuery("SELECT ? WHERE false", Types.BIGINT, 67598.toLong).
          aka("SQL query") must_=== ("SELECT ? WHERE false" -> 67598.toLong))

    }
  }

  "Float" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setFloat(1, 1.2.toFloat)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.FLOAT).
        and(m.getScale(1) aka "scale" must_=== 1)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.2.toFloat, Types.FLOAT)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.FLOAT).
        and(m.getScale(1) aka "scale" must_=== 1)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.2.toFloat, Types.FLOAT, 3)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.FLOAT).
        and(m.getScale(1) aka "scale" must_=== 3)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.2.toFloat)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.FLOAT).
        and(m.getScale(1) aka "scale" must_=== 1)

    }

    "be set as REAL null object" in {
      lazy val s = statement()
      s.setObject(1, null, Types.REAL)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.REAL)

    }

    "be set as REAL object" in {
      lazy val s = statement()
      s.setObject(1, 1.23f, Types.REAL)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.REAL).
        and(m.getScale(1) aka "scale" must_=== 2)

    }

    "be set as REAL object with scale" in {
      lazy val s = statement()
      s.setObject(1, 1.23f, Types.REAL, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.REAL).
        and(m.getScale(1) aka "scale" must_=== 1)

    }

    "be property prepared" >> {
      "when FLOAT" in {
        (executeUpdate("TEST ?, y", Types.FLOAT, 1.23.toFloat).
          aka("SQL update") must_=== ("TEST ?, y" -> 1.23.toFloat)).
          and(executeQuery("SELECT ? WHERE false", Types.FLOAT, 34.561.toFloat).
            aka("SQL query").
            must_===("SELECT ? WHERE false" -> 34.561.toFloat))

      }

      "when REAL" in {
        (executeUpdate("TEST ?, y", Types.REAL, 1.23.toFloat).
          aka("SQL update") must_=== ("TEST ?, y" -> 1.23.toFloat)).
          and(executeQuery("SELECT ? WHERE false", Types.REAL, 34.561.toFloat).
            aka("SQL query").
            must_===("SELECT ? WHERE false" -> 34.561.toFloat))

      }
    }
  }

  "Double" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setDouble(1, 1.234.toDouble)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DOUBLE).
        and(m.getScale(1) aka "scale" must_=== 3)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, 1.234.toDouble, Types.DOUBLE)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DOUBLE).
        and(m.getScale(1) aka "scale" must_=== 3)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, 1.234.toDouble, Types.DOUBLE, 5)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DOUBLE).
        and(m.getScale(1) aka "scale" must_=== 5)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, 1.234.toDouble)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DOUBLE).
        and(m.getScale(1) aka "scale" must_=== 3)

    }

    "be prepared" in {
      (executeUpdate("TEST ?, y", Types.DOUBLE, 1.23.toDouble).
        aka("SQL update") must_=== ("TEST ?, y" -> 1.23.toDouble)).
        and(executeQuery("SELECT ? WHERE false", Types.DOUBLE, 34.561.toDouble).
          aka("SQL query") must_=== ("SELECT ? WHERE false" -> 34.561.toDouble))

    }
  }

  "Numeric" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setBigDecimal(1, new java.math.BigDecimal("1.2345678"))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.NUMERIC).
        and(m.getScale(1) aka "scale" must_=== 7)

    }

    "be set as first numeric object with type" in {
      lazy val s = statement()
      s.setObject(1, new java.math.BigDecimal("1.2345678"), Types.NUMERIC)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.NUMERIC).
        and(m.getScale(1) aka "scale" must_=== 7)

    }

    "be set as first numeric object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, new java.math.BigDecimal("1.2345678"), Types.NUMERIC, 2)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.NUMERIC).
        and(m.getScale(1) aka "scale" must_=== 2)

    }

    "be set as first numeric object without type" in {
      lazy val s = statement()
      s.setObject(1, new java.math.BigDecimal("1.2345678"))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.NUMERIC).
        and(m.getScale(1) aka "scale" must_=== 7)

    }

    "be set as first decimal object" in {
      lazy val s = statement()
      s.setObject(1, new java.math.BigDecimal("1.2345678"), Types.DECIMAL)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DECIMAL).
        and(m.getScale(1) aka "scale" must_=== 7)

    }

    "be prepared" in {
      val d1 = new java.math.BigDecimal("876.78")
      val d2 = new java.math.BigDecimal("9007.2")

      (executeUpdate("TEST ?, y", Types.DECIMAL, d1).
        aka("SQL update") must_=== ("TEST ?, y" -> d1)).
        and(executeQuery("SELECT ? WHERE false", Types.DECIMAL, d2).
          aka("SQL query") must_=== ("SELECT ? WHERE false" -> d2))

    }

    "be null NUMERIC" in {
      lazy val s = statement()
      s.setBigDecimal(1, null.asInstanceOf[java.math.BigDecimal])

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.NUMERIC)
    }

    "be null DECIMAL" in {
      lazy val s = statement()
      s.setDecimal(1, null.asInstanceOf[java.math.BigDecimal])

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DECIMAL)
    }
  }

  "String" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setString(1, "str")

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.VARCHAR)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, "str", Types.VARCHAR)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.VARCHAR)

    }

    "be set as first object with type and length" in {
      lazy val s = statement()
      s.setObject(1, "str", Types.VARCHAR, 2)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.VARCHAR)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, "str")

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.VARCHAR)

    }

    "be prepared" >> {
      "when VARCHAR" in {
        (executeUpdate("TEST ?, y", Types.VARCHAR, "l'étoile").
          aka("SQL update") must_=== ("TEST ?, y" -> "l'étoile")).
          and(executeQuery("SELECT ? WHERE false", Types.VARCHAR, "val").
            aka("SQL query") must_=== ("SELECT ? WHERE false" -> "val"))

      }

      "when LONGVARCHAR" in {
        (executeUpdate("TEST ?, y", Types.LONGVARCHAR, "l'étoile").
          aka("SQL update") must_=== ("TEST ?, y" -> "l'étoile")).
          and(executeQuery("SELECT ? WHERE false", Types.LONGVARCHAR, "val").
            aka("SQL query") must_=== ("SELECT ? WHERE false" -> "val"))

      }
    }
  }

  "Byte array" should {
    val bindata = Array[Byte](1, 3, 7)
    def binstream: InputStream = new ByteArrayInputStream(bindata)

    "be set as first parameter" in {
      lazy val s = statement()
      s.setBytes(1, bindata)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BINARY)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, bindata, Types.BINARY)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BINARY)

    }

    "be set as first object with type and length" in {
      lazy val s = statement()
      s.setObject(1, bindata, Types.BINARY, 2)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BINARY)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, bindata)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BINARY)

    }

    "be set as first parameter from input stream" in {
      lazy val s = statement()
      s.setBinaryStream(1, binstream)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BINARY)

    }

    "be set as first parameter from input stream with integer length" in {
      lazy val s = statement()
      s.setBinaryStream(1, binstream, 3)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BINARY)

    }

    "be set as first parameter from input stream with long length" in {
      lazy val s = statement()
      s.setBinaryStream(1, binstream, 4L)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.BINARY)

    }

    "be prepared" >> {
      "when BINARY" in {
        (executeUpdate("TEST ?, y", Types.BINARY, bindata).
          aka("SQL update") must_=== ("TEST ?, y" -> bindata)).
          and(executeQuery("SELECT ? WHERE false", Types.BINARY, bindata).
            aka("SQL query") must_=== ("SELECT ? WHERE false" -> bindata))

      }

      "when VARBINARY" in {
        (executeUpdate("TEST ?, y", Types.VARBINARY, bindata).
          aka("SQL update") must_=== ("TEST ?, y" -> bindata)).
          and(executeQuery("SELECT ? WHERE false", Types.VARBINARY, bindata).
            aka("SQL query") must_=== ("SELECT ? WHERE false" -> bindata))

      }

      "when LONGVARBINARY" in {
        (executeUpdate("TEST ?, y", Types.LONGVARBINARY, bindata).
          aka("SQL update") must_=== ("TEST ?, y" -> bindata)).
          and(executeQuery(
            "SELECT ? WHERE false",
            Types.LONGVARBINARY, bindata) aka "SQL query" must_=== (
              "SELECT ? WHERE false" -> bindata))

      }

      "when BINARY as stream" in {
        (executeUpdate("TEST ?, y", Types.BINARY, binstream).
          aka("SQL update") must beLike {
            case ("TEST ?, y", s) => contentEquals(binstream, s).
              aka("same content") must beTrue

          }) and (executeQuery("SELECT ? WHERE false", Types.BINARY, binstream).
            aka("SQL query") must beLike {
              case ("SELECT ? WHERE false", s) => contentEquals(binstream, s).
                aka("same content") must beTrue
            })

      }

      "when VARBINARY as stream" in {
        (executeUpdate("TEST ?, y", Types.VARBINARY, binstream).
          aka("SQL update") must beLike {
            case ("TEST ?, y", s) => contentEquals(binstream, s).
              aka("same content") must beTrue
          }) and (executeQuery("SELECT ? WHERE false", Types.VARBINARY,
            binstream).aka("SQL query") must beLike {
            case ("SELECT ? WHERE false", s) => contentEquals(binstream, s).
              aka("same content") must beTrue
          })

      }

      "when LONGVARBINARY as stream" in {
        (executeUpdate("TEST ?, y", Types.LONGVARBINARY, binstream).
          aka("SQL update") must beLike {
            case ("TEST ?, y", s) => contentEquals(binstream, s).
              aka("same content") must beTrue
          }) and (executeQuery(
            "SELECT ? WHERE false",
            Types.LONGVARBINARY, binstream) aka "SQL query" must beLike {
              case ("SELECT ? WHERE false", s) => contentEquals(binstream, s).
                aka("same content") must beTrue
            })

      }
    }
  }

  "Date" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setDate(1, new java.sql.Date(System.currentTimeMillis()))

      lazy val m = s.getParameterMetaData

      m.getParameterCount aka "count" must_=== 1 and (
        m.getParameterType(1) aka "SQL type" must_=== Types.DATE)

    }

    "be set as first parameter with calendar" in {
      lazy val s = statement()
      s.setDate(1, new java.sql.Date(System.currentTimeMillis()), java.util.Calendar.getInstance)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DATE)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Date(System.currentTimeMillis()), Types.DATE)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DATE)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Date(System.currentTimeMillis()), Types.DATE, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DATE)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Date(System.currentTimeMillis()))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.DATE)

    }

    "be prepared" >> {
      "with default TZ" in {
        val d1 = new java.sql.Date(System.currentTimeMillis())
        val d2 = new java.sql.Date(System.currentTimeMillis() + 100L)

        (executeUpdate("TEST ?, y", Types.DATE, d1).
          aka("SQL update") must_=== ("TEST ?, y" -> d1)).
          and(executeQuery("SELECT ? WHERE false", Types.DATE, d2).
            aka("SQL query") must_=== ("SELECT ? WHERE false" -> d2))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")
        val d1 = new java.sql.Date(System.currentTimeMillis())
        val d2 = new java.sql.Date(System.currentTimeMillis() + 100L)

        (executeUpdate("TEST ?, y", Types.DATE, (d1 -> tz)).
          aka("SQL update") must beTypedEqualTo("TEST ?, y" -> (d1 -> tz))).
          and(executeQuery("SELECT ? WHERE false", Types.DATE, (d2 -> tz)).
            aka("SQL query") must beTypedEqualTo(
              "SELECT ? WHERE false" -> (d2 -> tz)))

      }
    }
  }

  "Time" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setTime(1, new java.sql.Time(System.currentTimeMillis()))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIME)

    }

    "be set as first parameter with calendar" in {
      lazy val s = statement()
      s.setTime(1, new java.sql.Time(System.currentTimeMillis()), java.util.Calendar.getInstance)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIME)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Time(System.currentTimeMillis()), Types.TIME)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIME)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Time(System.currentTimeMillis()), Types.TIME, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIME)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Time(System.currentTimeMillis()))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIME)

    }

    "be prepared" >> {
      "with default TZ" in {
        val t1 = new java.sql.Time(System.currentTimeMillis())
        val t2 = new java.sql.Time(System.currentTimeMillis() + 100L)

        (executeUpdate("TEST ?, y", Types.TIME, t1).
          aka("SQL update") must_=== ("TEST ?, y" -> t1)).
          and(executeQuery("SELECT ? WHERE false", Types.TIME, t2).
            aka("SQL query") must_=== ("SELECT ? WHERE false" -> t2))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")
        val t1 = new java.sql.Time(System.currentTimeMillis())
        val t2 = new java.sql.Time(System.currentTimeMillis() + 100L)

        (executeUpdate("TEST ?, y", Types.TIME, (t1 -> tz)).
          aka("SQL update") must beTypedEqualTo("TEST ?, y" -> (t1 -> tz))).
          and(executeQuery("SELECT ? WHERE false", Types.TIME, (t2 -> tz)).
            aka("SQL query") must beTypedEqualTo(
              "SELECT ? WHERE false" -> (t2 -> tz)))

      }
    }
  }

  "Timestamp" should {
    "be set as first parameter" in {
      lazy val s = statement()
      s.setTimestamp(1, new java.sql.Timestamp(1L))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIMESTAMP)

    }

    "be set as first parameter with calendar" in {
      lazy val s = statement()
      s.setTimestamp(1, new java.sql.Timestamp(1L),
        java.util.Calendar.getInstance)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIMESTAMP)

    }

    "be set as first object with type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Timestamp(1L), Types.TIMESTAMP)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIMESTAMP)

    }

    "be set as first object with type and scale" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Timestamp(1L), Types.TIMESTAMP, 1)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIMESTAMP)

    }

    "be set as first object without type" in {
      lazy val s = statement()
      s.setObject(1, new java.sql.Timestamp(1L))

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "SQL type" must_=== Types.TIMESTAMP)

    }

    "be prepared" >> {
      "with default TZ" in {
        val ts1 = new java.sql.Timestamp(2L)
        val ts2 = new java.sql.Timestamp(4L)

        (executeUpdate("TEST ?, y", Types.TIMESTAMP, ts1).
          aka("SQL update") must beTypedEqualTo("TEST ?, y" -> ts1)).
          and(executeQuery("SELECT ? WHERE false", Types.TIMESTAMP, ts2).
            aka("SQL query") must beTypedEqualTo("SELECT ? WHERE false" -> ts2))

      }

      "with 'Europe/Paris' TZ" in {
        val tz = TimeZone.getTimeZone("Europe/Paris")
        val ts1 = new java.sql.Timestamp(2L)
        val ts2 = new java.sql.Timestamp(4L)

        (executeUpdate("TEST ?, y", Types.TIMESTAMP, (ts1 -> tz)).
          aka("SQL update") must beTypedEqualTo("TEST ?, y" -> (ts1 -> tz))).
          and(executeQuery(
            "SELECT ? WHERE false", Types.TIMESTAMP, (ts2 -> tz)).
            aka("SQL query") must beTypedEqualTo(
              "SELECT ? WHERE false" -> (ts2 -> tz)))

      }
    }
  }

  "Unknown value" should {
    "be set as OTHER object" in {
      val v = new AnyRef {}
      val s = statement()

      s.setObject(1, v, Types.OTHER)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 1).
        and(m.getParameterType(1) aka "first type" must_=== Types.OTHER)

    }
  }

  "Parameters" should {
    "be kept in order when not set orderly" in {
      lazy val s = statement()
      s.setBoolean(2, false)
      s.setNull(1, Types.INTEGER)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 2).
        and(m.getParameterType(1) aka "first type" must_=== Types.INTEGER).
        and(m.getParameterType(2) aka "second type" must_=== Types.BOOLEAN)

    }

    "be kept in order when partially set at end" in {
      lazy val s = statement()
      s.setObject(2, null, Types.DOUBLE)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 2).
        and(m.getParameterType(2) aka "SQL type" must_=== Types.DOUBLE).
        and(m.getParameterType(1) aka "missing" must throwA[SQLException](
          message = "Parameter is not set: 1"))

    }

    "be kept in order when partially set at middle" in {
      lazy val s = statement()
      s.setObject(3, null, Types.DOUBLE)
      s.setNull(1, Types.INTEGER)

      lazy val m = s.getParameterMetaData

      (m.getParameterCount aka "count" must_=== 3).
        and(m.getParameterType(3) aka "third type" must_=== Types.DOUBLE).
        and(m.getParameterType(1) aka "first type" must_=== Types.INTEGER).
        and(m.getParameterType(2) aka "missing" must throwA[SQLException](
          message = "Parameter is not set: 2"))

    }

    "be cleared" in {
      lazy val s = statement()
      s.setBoolean(2, false)
      s.setNull(1, Types.INTEGER)
      s.clearParameters()

      s.getParameterMetaData.getParameterCount aka "count" must_=== 0
    }
  }

  "Query execution" should {
    lazy val h = new StatementHandler {
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
      lazy val s = statement(h = h)
      lazy val query = s.executeQuery()

      (query aka "execution" must not(throwA[SQLException])).
        and(query aka "resultset" must not(beNull)).
        and(s.getGeneratedKeys aka "generated keys" must beLike {
          case genKeys =>
            (genKeys.getStatement aka "keys statement" must_=== s).
              and(genKeys.next aka "has keys" must beFalse)
        })
    }

    "fail with update statement" in {
      lazy val s = statement()

      s.executeQuery() aka "query" must throwA[SQLException]("Not a query")
    }

    "fail on runtime exception" in {
      lazy val h = new StatementHandler {
        def isQuery(s: String) = true
        def whenSQLUpdate(s: String, p: Params) = UpdateResult.Nothing
        def whenSQLQuery(s: String, p: Params) = sys.error("Unexpected")
      }

      statement(h = h).executeQuery("SELECT").
        aka("execution") must throwA[SQLException](message = "Unexpected")
    }
  }

  "Update execution" should {
    "be detected" in {
      !statement().execute() aka "update" must beTrue
    }

    "return update count" in {
      statement().executeUpdate() aka "execution" must not(throwA[SQLException])
    }

    "has generated keys" in {
      lazy val h = new StatementHandler {
        def isQuery(s: String) = false
        def whenSQLQuery(s: String, p: Params) = sys.error("Not")
        def whenSQLUpdate(s: String, p: Params) =
          UpdateResult.One.withGeneratedKeys(RowLists.intList.append(200))

      }
      lazy val s = statement(h = h)

      (s.executeUpdate aka "update count" must_=== 1).
        and(s.getGeneratedKeys aka "generated keys" must beLike {
          case ks => (ks.getStatement aka "keys statement" must_=== s).
            and(ks.next aka "has first key" must beTrue).
            and(ks.getInt(1) aka "first key" must_=== 200).
            and(ks.next aka "has second key" must beFalse)
        })
    }

    "fail with query statement" in {
      lazy val h = new StatementHandler {
        def isQuery(s: String) = true
        def whenSQLUpdate(s: String, p: Params) = UpdateResult.Nothing
        def whenSQLQuery(s: String, p: Params) = {
          RowLists.rowList1(classOf[String]).asResult
        }
      }

      statement(h = h).executeUpdate() aka "update" must throwA[SQLException](
        message = "Cannot update with query")

    }

    "fail on runtime exception" in {
      lazy val h = new StatementHandler {
        def isQuery(s: String) = false
        def whenSQLUpdate(s: String, p: Params) = sys.error("Unexpected")
        def whenSQLQuery(s: String, p: Params) = sys.error("Not query")
      }

      statement(h = h).executeUpdate("UPDATE").
        aka("execution") must throwA[SQLException](message = "Unexpected")
    }
  }

  "Execution" should {
    lazy val h = new StatementHandler {
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
        def isQuery(s: String) = true
        def whenSQLUpdate(s: String, p: Params) = sys.error("Not")
        def whenSQLQuery(s: String, p: Params) =
          RowLists.rowList1(classOf[String]).asResult.withWarning(warning)

      }

      lazy val s = statement(h = h)
      s.executeQuery("TEST")

      (s.getWarnings aka "warning" must_=== warning).
        and(Option(s.getResultSet) must beSome[ResultSet].which {
          _.getWarnings aka "result warning" must_=== warning
        })

    }

    "be found for update" in {
      lazy val h = new StatementHandler {
        def isQuery(s: String) = false
        def whenSQLQuery(s: String, p: Params) = sys.error("Not")
        def whenSQLUpdate(s: String, p: Params) =
          UpdateResult.Nothing.withWarning(warning)

      }

      lazy val s = statement(h = h)
      s.executeUpdate()

      s.getWarnings aka "warning" must_=== warning
    }
  }

  // ---

  def executeUpdate[A](s: String, t: Int, v: A, c: Connection = defaultCon)(implicit stmt: StatementParam[A]): (String, A) = {
    var sql: String = null
    var param: Parameter = null
    lazy val h = new StatementHandler {
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

  def connection(ps: java.util.Properties) =
    new acolyte.jdbc.Connection(jdbcUrl, ps, defaultHandler)

  lazy val defaultCon = connection(null)
  lazy val defaultHandler = EmptyConnectionHandler
}

sealed trait StatementParam[A] {
  def set(s: PreparedStatement, i: Int, p: A, t: Int): PreparedStatement
  def get(p: Parameter): A
}

sealed trait Setters {
  import java.math.BigDecimal
  import java.util.Calendar
  import java.sql.{ Array => SqlArray, Date, Time, Timestamp }
  import org.apache.commons.lang3.tuple.ImmutablePair

  implicit def StmtArray[A <: SqlArray]: StatementParam[A] =
    new StatementParam[A] {
      def set(s: PreparedStatement, i: Int, p: A, t: Int) = {
        s.setArray(i, p)
        s
      }

      def get(p: Parameter): A = p.right.asInstanceOf[A]
    }

  implicit def StmtBytes: StatementParam[Array[Byte]] =
    new StatementParam[Array[Byte]] {
      def set(s: PreparedStatement, i: Int, p: Array[Byte], t: Int) = {
        s.setBytes(i, p)
        s
      }

      def get(p: Parameter): Array[Byte] = p.right.asInstanceOf[Array[Byte]]
    }

  implicit def StmtNull: StatementParam[Null] = new StatementParam[Null] {
    def set(s: PreparedStatement, i: Int, p: Null, t: Int) = {
      s.setNull(i, t)
      s
    }

    def get(p: Parameter): Null = null
  }

  implicit def StmtBlob: StatementParam[Blob] = new StatementParam[Blob] {
    def set(s: PreparedStatement, i: Int, p: Blob, t: Int) = {
      s.setBlob(i, p)
      s
    }

    def get(p: Parameter): Blob = p.right.asInstanceOf[Blob]
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

  implicit def StmtBinStream: StatementParam[InputStream] =
    new StatementParam[InputStream] {
      def set(s: PreparedStatement, i: Int, p: InputStream, t: Int) = {
        s.setBinaryStream(i, p)
        s
      }

      def get(p: Parameter): InputStream = {
        // stream set as byte internally
        val bytes = p.right.asInstanceOf[Array[Byte]]
        new ByteArrayInputStream(bytes)
      }
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
