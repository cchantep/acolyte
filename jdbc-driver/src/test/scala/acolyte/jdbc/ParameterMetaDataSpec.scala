package acolyte.jdbc

import java.sql.{SQLException, Types}
import java.sql.ParameterMetaData.{parameterModeIn => IN, parameterModeInOut => IN_OUT, parameterNoNulls => NOT_NULL, parameterNullableUnknown => UNKNOWN_NULL}

import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment

import acolyte.jdbc.ParameterMetaData.{Array => ArrayP, Binary, Blob => BlobP, Bool => BoolP, Byte => ByteP, Date => DateP, Decimal => DecimalP, Default => DefaultP, Double => DoubleP, Float => FloatP, Int => IntP, Long => LongP, Null => NullP, Numeric => NumericP, ParameterDef => Param, Real => RealP, Scaled => ScaledP, Short => ShortP, Str => StrP, Time => TimeP, Timestamp => TimestampP}

object ParameterMetaDataSpec
  extends Specification with ParameterMetaDataFixtures {

  "Parameter metadata specification".title

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
      metadata().getParameterCount aka "count" must_=== 0
    }

    "be two" in {
      twoParams.getParameterCount aka "count" must_=== 2
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
      (twoParams.isNullable(1) aka "first" must_=== NOT_NULL).
        and(twoParams.isNullable(2) aka "second" must_=== UNKNOWN_NULL)

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
      twoParams.getPrecision(1) aka "first" must_=== -1 and (
        twoParams.getPrecision(2) aka "second" must_=== 10)

    }
  }

  "Scale" should {
    "fail without parameter" in {
      metadata().getScale(1).
        aka("scale") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (-1, 1)" in {
      twoParams.getScale(1) aka "first" must_=== -1 and (
        twoParams.getScale(2) aka "second" must_=== 1)

    }
  }

  "Parameter type" should {
    "fail without parameter" in {
      metadata().getParameterType(1).
        aka("type") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (VARCHAR, INTEGER)" in {
      (twoParams.getParameterType(1) aka "first" must_=== Types.VARCHAR).
        and(twoParams.getParameterType(2) aka "second" must_=== Types.INTEGER)

    }
  }

  "Parameter type name" should {
    "fail without parameter" in {
      metadata().getParameterTypeName(1).
        aka("type") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (VARCHAR, INTEGER)" in {
      (twoParams.getParameterTypeName(1) aka "first" must_=== "VARCHAR").
        and(twoParams.getParameterTypeName(2) aka "second" must_=== "INTEGER")

    }
  }

  "Parameter class" should {
    "fail without parameter" in {
      metadata().getParameterClassName(1).
        aka("class") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (String, Integer)" in {
      (twoParams.getParameterClassName(1).
        aka("first") must_=== "java.lang.String").
        and(twoParams.getParameterClassName(2).
          aka("second") must_=== "java.lang.Integer")

    }
  }

  "Parameter mode" should {
    "fail without parameter" in {
      metadata().getParameterMode(1).
        aka("mode") must throwA[SQLException]("Parameter out of bounds: 1")
    }

    "be (INOUT, IN)" in {
      (twoParams.getParameterMode(1) aka "first" must_=== IN_OUT).
        and(twoParams.getParameterMode(2) aka "second" must_=== IN)

    }
  }

  "Wrapping" should {
    "be valid for java.sql.ParameterMetaData" in {
      metadata().isWrapperFor(classOf[java.sql.ParameterMetaData]).
        aka("is wrapper for java.sql.ParameterMetaData") must beTrue

    }

    "be unwrapped to java.sql.ParameterMetaData" in {
      Option(metadata().unwrap(classOf[java.sql.ParameterMetaData])).
        aka("unwrapped") must beSome[java.sql.ParameterMetaData]

    }
  }

  "Defaults" should {
    Fragment.foreach(jdbcTypeMap.toList) {
      case (k, v) =>
        s"be expected one for ${typeName(k)}" in {
          DefaultP(k) aka "default parameter" must_=== param(v, IN, k,
            typeName(k), typePrecision(k), typeScale(k), UNKNOWN_NULL,
            typeSign(k))

        }
    }
  }

  "Decimal" should {
    Fragment.foreach(
      Seq(Types.DECIMAL, Types.DOUBLE, Types.FLOAT, Types.REAL)) { t =>
        s"be ok for $t" in {
          (1 to 32).foldLeft(ok) { (prev, s) =>
            prev and {
              ScaledP(t, s) must_=== param(
                jdbcTypeMap(t),
                IN, t, typeName(t), typePrecision(t), s, UNKNOWN_NULL, typeSign(t))
            }
          }
        }
      }
  }

  "Null parameter" should {
    Fragment.foreach(jdbcTypeMap.keys.toSeq) { k =>
      s"be expected ${typeName(k)}" in {
        NullP(k) aka "null parameter" must_=== DefaultP(k)
      }
    }
  }

  "Array parameter" should {
    "be default one" in {
      ArrayP aka "array parameter" must_=== DefaultP(Types.ARRAY)
    }
  }

  "Binary parameter" should {
    "be default one" in {
      Binary aka "binary parameter" must_=== DefaultP(Types.BINARY)
    }
  }

  "Blob parameter" should {
    "be default one" in {
      BlobP aka "blob parameter" must_=== DefaultP(Types.BLOB)
    }
  }

  "Boolean parameter" should {
    "be default one" in {
      BoolP aka "boolean parameter" must_=== DefaultP(Types.BOOLEAN)
    }
  }

  "Byte parameter" should {
    "be default one" in {
      ByteP aka "byte parameter" must_=== DefaultP(Types.TINYINT)
    }
  }

  "Short parameter" should {
    "be default one" in {
      ShortP aka "short parameter" must_=== DefaultP(Types.SMALLINT)
    }
  }

  "Integer parameter" should {
    "be default one" in {
      IntP aka "integer parameter" must_=== DefaultP(Types.INTEGER)
    }
  }

  "Long parameter" should {
    "be default one" in {
      LongP aka "long parameter" must_=== DefaultP(Types.BIGINT)
    }
  }

  "Float parameter" should {
    "have scale 1" in {
      (FloatP(1.2f) aka "float parameter" must_=== ScaledP(Types.FLOAT, 1)).
        and(RealP(1.2f) aka "real parameter" must_=== ScaledP(Types.REAL, 1))
    }

    "have scale 2" in {
      (FloatP(1.23f) aka "float parameter" must_=== ScaledP(Types.FLOAT, 2)).
        and(RealP(1.23f) aka "real parameter" must_=== ScaledP(Types.REAL, 2))
    }

    "have scale 6" in {
      (FloatP(1.234567f).
        aka("float parameter") must_=== ScaledP(Types.FLOAT, 6)).
        and(RealP(1.234567f).
          aka("real parameter") must_=== ScaledP(Types.REAL, 6))
    }
  }

  "Double parameter" should {
    "have scale 1" in {
      DoubleP(1.2f) aka "double parameter" must_=== ScaledP(Types.DOUBLE, 1)
    }

    "have scale 5" in {
      DoubleP(1.23456f).
        aka("double parameter") must_=== ScaledP(Types.DOUBLE, 5)
    }
  }

  "BigDecimal parameter" should {
    "have scale 3" in {
      (NumericP(new java.math.BigDecimal("1.234")).
        aka("numeric parameter") must_=== ScaledP(Types.NUMERIC, 3)).
        and(DecimalP(new java.math.BigDecimal("1.234")).
          aka("decimal parameter") must_=== ScaledP(Types.DECIMAL, 3))

    }
  }

  "String parameter" should {
    "be default one" in {
      StrP aka "string parameter" must_=== DefaultP(Types.VARCHAR)
    }
  }

  "Temporaral parameters" should {
    "be date" in {
      DateP aka "date parameter" must_=== DefaultP(Types.DATE)
    }

    "be time" in {
      TimeP aka "time parameter" must_=== DefaultP(Types.TIME)
    }

    "be timestamp" in {
      TimestampP aka "ts parameter" must_=== DefaultP(Types.TIMESTAMP)
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
  import scala.collection.JavaConverters
  import acolyte.jdbc.ParameterMetaData.ParameterDef

  lazy val jdbcTypeMap = JavaConverters.mapAsScalaMapConverter[Integer, String](
    Defaults.jdbcTypeMappings).asScala.foldLeft(Map[Int, String]()) { (m, p) =>
      m + (p._1.toInt -> p._2)
    }

  def typeName(t: Int): String = Defaults.jdbcTypeNames.get(t)
  def typeSign(t: Int): Boolean = Defaults.jdbcTypeSigns.get(t)
  def typePrecision(t: Int): Int = Defaults.jdbcTypePrecisions.get(t)
  def typeScale(t: Int): Int = Defaults.jdbcTypeScales.get(t)

  def param(cn: String, m: Int = IN, st: Int = -1, stn: String, p: Int = -1, s: Int = -1, n: Int = UNKNOWN_NULL, sg: Boolean = false) = new ParameterDef(cn, m, st, stn, p, s, n, sg)

  def metadata(p: Seq[ParameterDef] = Nil) = new ParameterMetaData(
    JavaConverters.seqAsJavaListConverter(p).asJava)

  lazy val twoParams = metadata(Seq(
    param(
      cn = "java.lang.String",
      m = IN_OUT,
      st = Types.VARCHAR,
      stn = "VARCHAR",
      n = NOT_NULL),
    param(
      cn = "java.lang.Integer",
      st = Types.INTEGER,
      stn = "INTEGER",
      p = 10,
      s = 1,
      sg = true)))

}
