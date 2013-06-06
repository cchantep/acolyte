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
        and(m.getParameterType(1) aka "SQL type" mustEqual Types.VARCHAR).
        and(m.getParameterTypeName(1) aka "SQL name" mustEqual "VARCHAR").
        and(m.getParameterClassName(1).
          aka("class") mustEqual "java.lang.String").
        and(m.isSigned(1) aka "sign" must beFalse).
        and(m.getPrecision(1) aka "precision" mustEqual -1).
        and(m.getScale(1) aka "scale" mustEqual -1).
        and(m.isNullable(1).
          aka("nullable") mustEqual java.sql.ParameterMetaData.
          parameterNullableUnknown)

    }
  }

  // ---

  def statement(c: Connection = defaultCon, h: StatementHandler = defaultHandler) = new PreparedStatement(c, h)

  val jdbcUrl = "jdbc:acolyte:test"
  lazy val defaultCon = new acolyte.Connection(jdbcUrl, null, defaultHandler)
  lazy val defaultHandler = EmptyConnectionHandler
}
