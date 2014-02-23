package acolyte

import java.sql.Types
import java.sql.SQLException
import java.sql.ParameterMetaData.{
  parameterModeIn ⇒ IN,
  parameterModeInOut ⇒ IN_OUT,
  parameterNullableUnknown ⇒ UNKNOWN_NULL,
  parameterNoNulls ⇒ NOT_NULL
}

import org.specs2.mutable.Specification

import acolyte.ParameterMetaData.{
  ParameterDef ⇒ Param,
  Bool ⇒ BoolP,
  Byte ⇒ ByteP,
  Decimal ⇒ DecimalP,
  Default ⇒ DefaultP,
  Long ⇒ LongP,
  Null ⇒ NullP,
  Short ⇒ ShortP,
  Int ⇒ IntP,
  Float ⇒ FloatP,
  Double ⇒ DoubleP,
  Numeric ⇒ NumericP,
  Str ⇒ StrP,
  Date ⇒ DateP,
  Time ⇒ TimeP,
  Timestamp ⇒ TimestampP,
  Real ⇒ RealP,
  Scaled ⇒ ScaledP
}

object ParameterMetaDataSpec
    extends Specification with ParameterMetaDataFixtures {

  "Parameter metadata specification" title

  "Parameter definition" should {
    "not be maid with invalid class name" in {
      param(cn = null, stn = "VARCHAR").
        aka("def") must throwA[IllegalArgumentException](
          message = "Missing class name")
    }

    "not be maid with invalid SQL type name" in {
      param(cn = "java.lang.String", stn = null).
        aka("def") must throwA[IllegalArgumentException](
          message = "Missing SQL type name")
    }
  }

  "Meta-data" should {
    "refuse missing definition" in {
      new ParameterMetaData(null).
        aka("ctor") must throwA[IllegalArgumentException]("Missing definition")
    }
  }

  "Parameter count" should {
    "be zero" in {
      metadata().getParameterCount aka "count" mustEqual 0
    }

    "be two" in {
      twoParams.getParameterCount aka "count" mustEqual 2
    }
  }

  "Nullable check" should {
    "fail without parameter" in {
      metadata().isNullable(1).
        aka("nullable") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "fail before 1" in {
      twoParams.isNullable(0).
        aka("nullable") must throwA[SQLException]("Parameter out of bounds: 0")

    }

    "be (NOT NULL, UNKNOWN)" in {
      (twoParams.isNullable(1) aka "first" mustEqual NOT_NULL).
        and(twoParams.isNullable(2) aka "second" mustEqual UNKNOWN_NULL)

    }
  }

  "Signed check" should {
    "fail without parameter" in {
      metadata().isSigned(1).
        aka("signed") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (false, true)" in {
      (twoParams.isSigned(1) aka "first" must beFalse).
        and(twoParams.isSigned(2) aka "second" must beTrue)

    }
  }

  "Precision" should {
    "fail without parameter" in {
      metadata().getPrecision(1).
        aka("precision") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (-1, 10)" in {
      (twoParams.getPrecision(1) aka "first" mustEqual -1).
        and(twoParams.getPrecision(2) aka "second" mustEqual 10)

    }
  }

  "Scale" should {
    "fail without parameter" in {
      metadata().getScale(1).
        aka("scale") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (-1, 1)" in {
      (twoParams.getScale(1) aka "first" mustEqual -1).
        and(twoParams.getScale(2) aka "second" mustEqual 1)

    }
  }

  "Parameter type" should {
    "fail without parameter" in {
      metadata().getParameterType(1).
        aka("type") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (VARCHAR, INTEGER)" in {
      (twoParams.getParameterType(1) aka "first" mustEqual Types.VARCHAR).
        and(twoParams.getParameterType(2) aka "second" mustEqual Types.INTEGER)

    }
  }

  "Parameter type name" should {
    "fail without parameter" in {
      metadata().getParameterTypeName(1).
        aka("type") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (VARCHAR, INTEGER)" in {
      (twoParams.getParameterTypeName(1) aka "first" mustEqual "VARCHAR").
        and(twoParams.getParameterTypeName(2) aka "second" mustEqual "INTEGER")

    }
  }

  "Parameter class" should {
    "fail without parameter" in {
      metadata().getParameterClassName(1).
        aka("class") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (String, Integer)" in {
      (twoParams.getParameterClassName(1).
        aka("first") mustEqual "java.lang.String").
        and(twoParams.getParameterClassName(2).
          aka("second") mustEqual "java.lang.Integer")

    }
  }

  "Parameter mode" should {
    "fail without parameter" in {
      metadata().getParameterMode(1).
        aka("mode") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (INOUT, IN)" in {
      (twoParams.getParameterMode(1) aka "first" mustEqual IN_OUT).
        and(twoParams.getParameterMode(2) aka "second" mustEqual IN)

    }
  }

  "Wrapping" should {
    "be valid for java.sql.ParameterMetaData" in {
      metadata().isWrapperFor(classOf[java.sql.ParameterMetaData]).
        aka("is wrapper for java.sql.ParameterMetaData") must beTrue

    }

    "be unwrapped to java.sql.ParameterMetaData" in {
      Option(metadata().unwrap(classOf[java.sql.ParameterMetaData])).
        aka("unwrapped") must beSome.
        which(_.isInstanceOf[java.sql.ParameterMetaData])

    }
  }

  "Defaults" should {
    jdbcTypeMap foreach { p ⇒
      val (k, v) = p
      s"be expected one for ${typeName(k)}" in {
        DefaultP(k) aka "default parameter" mustEqual param(v, IN, k,
          typeName(k), typePrecision(k), typeScale(k), UNKNOWN_NULL,
          typeSign(k))

      }
    }
  }

  "Decimal" should {
    Seq(Types.DECIMAL, Types.DOUBLE, Types.FLOAT, Types.REAL) foreach { t ⇒
      (1 to 32) foreach { s ⇒
        ScaledP(t, s) aka "decimal parameter" mustEqual param(jdbcTypeMap(t),
          IN, t, typeName(t), typePrecision(t), s, UNKNOWN_NULL, typeSign(t))

      }
    }
  }

  "Null parameter" should {
    jdbcTypeMap.keys foreach { k ⇒
      s"be expected ${typeName(k)}" in {
        NullP(k) aka "null parameter" mustEqual DefaultP(k)
      }
    }
  }

  "Boolean parameter" should {
    "be default one" in {
      BoolP aka "boolean parameter" mustEqual DefaultP(Types.BOOLEAN)
    }
  }

  "Byte parameter" should {
    "be default one" in {
      ByteP aka "byte parameter" mustEqual DefaultP(Types.TINYINT)
    }
  }

  "Short parameter" should {
    "be default one" in {
      ShortP aka "short parameter" mustEqual DefaultP(Types.SMALLINT)
    }
  }

  "Integer parameter" should {
    "be default one" in {
      IntP aka "integer parameter" mustEqual DefaultP(Types.INTEGER)
    }
  }

  "Long parameter" should {
    "be default one" in {
      LongP aka "long parameter" mustEqual DefaultP(Types.BIGINT)
    }
  }

  "Float parameter" should {
    "have scale 1" in {
      (FloatP(1.2f) aka "float parameter" mustEqual ScaledP(Types.FLOAT, 1)).
        and(RealP(1.2f) aka "real parameter" mustEqual ScaledP(Types.REAL, 1))
    }

    "have scale 2" in {
      (FloatP(1.23f) aka "float parameter" mustEqual ScaledP(Types.FLOAT, 2)).
        and(RealP(1.23f) aka "real parameter" mustEqual ScaledP(Types.REAL, 2))
    }

    "have scale 6" in {
      (FloatP(1.234567f).
        aka("float parameter") mustEqual ScaledP(Types.FLOAT, 6)).
        and(RealP(1.234567f).
          aka("real parameter") mustEqual ScaledP(Types.REAL, 6))
    }
  }

  "Double parameter" should {
    "have scale 1" in {
      DoubleP(1.2f) aka "double parameter" mustEqual ScaledP(Types.DOUBLE, 1)
    }

    "have scale 5" in {
      DoubleP(1.23456f).
        aka("double parameter") mustEqual ScaledP(Types.DOUBLE, 5)
    }
  }

  "BigDecimal parameter" should {
    "have scale 3" in {
      (NumericP(new java.math.BigDecimal("1.234")).
        aka("numeric parameter") mustEqual ScaledP(Types.NUMERIC, 3)).
        and(DecimalP(new java.math.BigDecimal("1.234")).
          aka("decimal parameter") mustEqual ScaledP(Types.DECIMAL, 3))

    }
  }

  "String parameter" should {
    "be default one" in {
      StrP aka "string parameter" mustEqual DefaultP(Types.VARCHAR)
    }
  }

  "Temporaral parameters" should {
    "be date" in {
      DateP aka "date parameter" mustEqual DefaultP(Types.DATE)
    }

    "be time" in {
      TimeP aka "time parameter" mustEqual DefaultP(Types.TIME)
    }

    "be timestamp" in {
      TimestampP aka "ts parameter" mustEqual DefaultP(Types.TIMESTAMP)
    }
  }

  "Missing parameter" should {
    lazy val m = metadata(Seq(null.asInstanceOf[Param]))

    "be handled for nullable check" in {
      m.isNullable(1) aka "check" must throwA[SQLException](
        message = "Parameter is not set: 1")
    }

    "be handled for sign check" in {
      m.isSigned(1) aka "check" must throwA[SQLException](
        message = "Parameter is not set: 1")
    }

    "be handled for precision check" in {
      m.getPrecision(1) aka "check" must throwA[SQLException](
        message = "Parameter is not set: 1")
    }

    "be handled for scale check" in {
      m.getScale(1) aka "check" must throwA[SQLException](
        message = "Parameter is not set: 1")
    }

    "be handled for type check" in {
      m.getParameterType(1) aka "check" must throwA[SQLException](
        message = "Parameter is not set: 1")
    }

    "be handled for type name check" in {
      m.getParameterTypeName(1) aka "check" must throwA[SQLException](
        message = "Parameter is not set: 1")
    }

    "be handled for class name check" in {
      m.getParameterClassName(1) aka "check" must throwA[SQLException](
        message = "Parameter is not set: 1")
    }

    "be handled for mode check" in {
      m.getParameterMode(1) aka "check" must throwA[SQLException](
        message = "Parameter is not set: 1")
    }
  }
}

sealed trait ParameterMetaDataFixtures {
  import scala.collection.JavaConversions
  import acolyte.ParameterMetaData.ParameterDef

  lazy val jdbcTypeMap = JavaConversions.mapAsScalaMap[Integer, String](
    Defaults.jdbcTypeMappings).foldLeft(Map[Int, String]()) { (m, p) ⇒
      m + (p._1.toInt -> p._2)
    }

  def typeName(t: Int): String = Defaults.jdbcTypeNames.get(t)
  def typeSign(t: Int): Boolean = Defaults.jdbcTypeSigns.get(t)
  def typePrecision(t: Int): Int = Defaults.jdbcTypePrecisions.get(t)
  def typeScale(t: Int): Int = Defaults.jdbcTypeScales.get(t)

  def param(cn: String, m: Int = IN, st: Int = -1, stn: String, p: Int = -1, s: Int = -1, n: Int = UNKNOWN_NULL, sg: Boolean = false) = new ParameterDef(cn, m, st, stn, p, s, n, sg)

  def metadata(p: Seq[ParameterDef] = Nil) = new ParameterMetaData(JavaConversions seqAsJavaList p)

  lazy val twoParams = metadata(Seq(
    param(cn = "java.lang.String",
      m = IN_OUT,
      st = Types.VARCHAR,
      stn = "VARCHAR",
      n = NOT_NULL),
    param(cn = "java.lang.Integer",
      st = Types.INTEGER,
      stn = "INTEGER",
      p = 10,
      s = 1,
      sg = true)))

}
