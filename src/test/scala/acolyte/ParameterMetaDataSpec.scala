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
}

sealed trait ParameterMetaDataFixtures {
  import scala.collection.JavaConversions
  import acolyte.ParameterMetaData.Parameter

  def param(cn: String, m: Int = IN, st: Int = -1, stn: String, p: Int = -1, s: Int = -1, n: Int = UNKNOWN_NULL, sg: Boolean = false) = new Parameter(cn, m, st, stn, p, s, n, sg)

  def metadata(p: Seq[Parameter] = Nil) = new ParameterMetaData(JavaConversions seqAsJavaList p)

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
