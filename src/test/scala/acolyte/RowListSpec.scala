package acolyte

import java.math.{ BigDecimal ⇒ JBigDec }

import java.sql.{ Date, ResultSet, SQLException, Time, Timestamp }

import org.specs2.mutable.Specification

import acolyte.Row._
import acolyte.Acolyte._

object RowListSpec extends Specification with RowListTest {
  "Row list" title

  "Creation" should {
    "not accept null list" in {
      new RowList(null, new java.util.HashMap[String, Integer]()).
        aka("ctor") must throwA[IllegalArgumentException](
          message = "Invalid rows")
    }

    "not accept null map" in {
      new RowList[Row1[String]](new java.util.ArrayList[Row1[String]](), null).
        aka("ctor") must throwA[IllegalArgumentException](
          message = "Invalid names")
    }
  }

  "Result set fetch size" should {
    "be immutable" in {
      rowList[Row.Row1[String]].resultSet.setFetchSize(1).
        aka("setter") must throwA[UnsupportedOperationException]

    }

    "be 1" in {
      (rowList[Row1[String]].append(row1("str")).
        resultSet.getFetchSize aka "size" mustEqual 1).
        and(rowList[Row2[String, Float]].append(row2("str", 1.23.toFloat)).
          resultSet.getFetchSize aka "size" mustEqual 1)

    }

    "be 2" in {
      (rowList[Row1[String]].append(row1("a")).append(row1("b")).
        resultSet.getFetchSize aka "size" mustEqual 2)

    }
  }

  "Object column by index" should {
    "not be read when not on a row" in {
      rowList[Row1[String]].append(row1("str")).resultSet.
        getObject(1) aka "getObject" must throwA[SQLException](
          message = "Not on a row")

    }

    "be expected one" in {
      lazy val rs = (rowList[Row1[Long]] :+ row1(123.toLong)).resultSet
      rs.next

      rs.getObject(1) aka "cell1" mustEqual 123.toLong
    }

    "be null" in {
      lazy val rs = rowList[Row1[Float]].
        append(row1(null.asInstanceOf[Float])).resultSet

      rs.next

      rs.getObject(1) aka "cell1" must beNull
    }

    "not be read with invalid index" in {
      lazy val rs = rowList[Row1[Long]].append(row1(123.toLong)).resultSet
      rs.next

      rs.getObject(2) aka "getObject" must throwA[SQLException](
        message = "Invalid column index: 2")

    }
  }

  "Object column by name" should {
    "not be read when not on a row" in {
      rowList[Row1[String]].withLabel(1, "n").append(row1("str")).resultSet.
        getObject("n") aka "getObject" must throwA[SQLException](
          message = "Not on a row")

    }

    "be expected one" in {
      lazy val rs = (rowList[Row1[Long]].
        withLabel(1, "l") :+ row1(123.toLong)).resultSet
      rs.next

      rs.getObject("l") aka "cell1" mustEqual 123.toLong
    }

    "be null" in {
      lazy val rs = rowList[Row1[Float]].withLabel(1, "name").
        append(row1(null.asInstanceOf[Float])).resultSet

      rs.next

      rs.getObject("name") aka "cell1" must beNull
    }

    "not be read with invalid name" in {
      lazy val rs = rowList[Row1[Long]].
        withLabel(1, "n").append(row1(123.toLong)).resultSet
      rs.next

      (rs.getObject(null).
        aka("getObject") must throwA[SQLException]("Invalid label: null")).
        and(rs.getObject("label").
          aka("getObject") must throwA[SQLException]("Invalid label: label"))
    }

    "not be read with invalid name mapping" in {
      lazy val rs = rowList[Row1[Long]].
        withLabels(0 -> "before", 2 -> "after").
        append(row1(123.toLong)).resultSet
      rs.next

      (rs.getLong("before") aka "get" must throwA[SQLException](
        message = "Invalid column index: 0")).
        and(rs.getLong("after") aka "get" must throwA[SQLException](
          message = "Invalid column index: 2"))
    }
  }

  "String column from result set" should {
    "not be read by index when not on a row" in {
      (rowList[Row1[String]].append(row1("str")).resultSet.
        getString(1) aka "getString" must throwA[SQLException](
          message = "Not on a row")).
          and(rowList[Row1[String]].withLabel(1, "n").
            append(row1("str")).resultSet.getString("n").
            aka("getString") must throwA[SQLException]("Not on a row"))

    }

    "be expected one" in {
      val rs = rowList[Row1[String]].withLabel(1, "n").:+(row1("str")).resultSet
      rs.next

      (rs.getString(1) aka "string by index" mustEqual "str").
        and(rs.getString("n") aka "string by name" mustEqual "str")
    }

    "be null" in {
      val rs = (rowList[Row1[String]].
        withLabel(1, "n") :+ row1(null.asInstanceOf[String])).resultSet

      rs.next

      (rs.getString(1) aka "boolean" must beNull).
        and(rs.getString("n") aka "boolean" must beNull)
    }
  }

  "Boolean column from result set" should {
    "not be read by index when not on a row" in {
      (rowList[Row1[Boolean]].append(row1(true)).resultSet.
        getBoolean(1) aka "getBoolean" must throwA[SQLException](
          message = "Not on a row")).
          and(rowList[Row1[Boolean]].withLabel(1, "n").
            append(row1(false)).resultSet.getBoolean("n").
            aka("getBoolean") must throwA[SQLException]("Not on a row"))

    }

    "be expected one" in {
      val rs = rowList[Row1[Boolean]].withLabel(1, "n").:+(row1(true)).resultSet
      rs.next

      (rs.getBoolean(1) aka "boolean by index" mustEqual true).
        and(rs.getBoolean("n") aka "boolean by name" mustEqual true)
    }

    "be null (false)" in {
      val rs = rowList[Row1[Boolean]].withLabel(1, "n").
        append(row1(null.asInstanceOf[Boolean])).resultSet

      rs.next

      (rs.getBoolean(1) aka "boolean" must beFalse).
        and(rs.getBoolean("n") aka "boolean" must beFalse)
    }

    "converted to false by index" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (rowList[Row1[Char]] :+ row1('0')).resultSet,
        "byte" -> (rowList[Row1[Byte]] :+ row1(0.toByte)).resultSet,
        "short" -> (rowList[Row1[Short]] :+ row1(0.toShort)).resultSet,
        "int" -> (rowList[Row1[Int]] :+ row1(0)).resultSet,
        "long" -> (rowList[Row1[Long]] :+ row1(0.toLong)).resultSet)

      rs.foreach(_._2.next)

      rs foreach { r ⇒
        s"from ${r._1}" in { r._2.getBoolean(1) aka "boolean" must beFalse }
      }
    }

    "converted to true by index" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (rowList[Row1[Char]] :+ row1('1')).resultSet,
        "byte" -> (rowList[Row1[Byte]] :+ row1(2.toByte)).resultSet,
        "short" -> (rowList[Row1[Short]] :+ row1(3.toShort)).resultSet,
        "int" -> (rowList[Row1[Int]] :+ row1(4)).resultSet,
        "long" -> (rowList[Row1[Long]] :+ row1(5.toLong)).resultSet)

      rs.foreach(_._2.next)

      rs foreach { r ⇒
        s"from ${r._1}" in { r._2.getBoolean(1) aka "boolean" must beTrue }
      }
    }

    "converted to false by label" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (rowList[Row1[Char]].
          withLabel(1, "n") :+ row1('0')).resultSet,
        "byte" -> (rowList[Row1[Byte]].
          withLabel(1, "n") :+ row1(0.toByte)).resultSet,
        "short" -> (rowList[Row1[Short]].
          withLabel(1, "n") :+ row1(0.toShort)).resultSet,
        "int" -> (rowList[Row1[Int]].
          withLabel(1, "n") :+ row1(0)).resultSet,
        "long" -> (rowList[Row1[Long]].
          withLabel(1, "n") :+ row1(0.toLong)).resultSet)

      rs.foreach(_._2.next)

      rs foreach { r ⇒
        s"from ${r._1}" in { r._2.getBoolean("n") aka "boolean" must beFalse }
      }
    }

    "converted to true by label" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (rowList[Row1[Char]].
          withLabel(1, "n") :+ row1('1')).resultSet,
        "byte" -> (rowList[Row1[Byte]].
          withLabel(1, "n") :+ row1(2.toByte)).resultSet,
        "short" -> (rowList[Row1[Short]].
          withLabel(1, "n") :+ row1(3.toShort)).resultSet,
        "int" -> (rowList[Row1[Int]].
          withLabel(1, "n") :+ row1(4)).resultSet,
        "long" -> (rowList[Row1[Long]].
          withLabel(1, "n") :+ row1(5.toLong)).resultSet)

      rs.foreach(_._2.next)

      rs foreach { r ⇒
        s"from ${r._1}" in { r._2.getBoolean("n") aka "boolean" must beTrue }
      }
    }
  }

  numberGetterSpec[Byte]("Byte", 1.toByte)
  numberGetterSpec[Short]("Short", 1.toShort)
  numberGetterSpec[Int]("Int", 1)
  numberGetterSpec[Long]("Long", 1.toLong)
  numberGetterSpec[Float]("Float", 1.toFloat)
  numberGetterSpec[Double]("Double", 1.toDouble)

  "BigDecimal column from result set" should {
    val v = new JBigDec("1")

    "not be read by index when not on a row" in {
      (rowList[Row1[JBigDec]].append(row1(v)).resultSet.
        getBigDecimal(1) aka "get" must throwA[SQLException](
          message = "Not on a row")).
          and(rowList[Row1[JBigDec]].withLabel(1, "n").
            append(row1(v)).resultSet.getBigDecimal("n").
            aka("get") must throwA[SQLException]("Not on a row"))

    }

    "be expected one" in {
      val rs = (rowList[Row1[JBigDec]].withLabel(1, "n") :+ (row1(v))).resultSet
      rs.next

      (rs.getBigDecimal(1) aka "big decimal by index" mustEqual v).
        and(rs.getBigDecimal("n") aka "big decimal by name" mustEqual v)
    }

    "be scaled one" in {
      val rs = rowList[Row1[JBigDec]].withLabel(1, "n").
        append(row1(new JBigDec("1.2345"))).resultSet

      rs.next

      (rs.getBigDecimal(1, 2).
        aka("big decimal by index") mustEqual new JBigDec("1.23")).
        and(rs.getBigDecimal("n", 3).
          aka("big decimal by name") mustEqual new JBigDec("1.234"))
    }

    "be null" in {
      val rs = (rowList[Row1[JBigDec]].withLabel(1, "n") :+
        row1(null.asInstanceOf[JBigDec])).resultSet

      rs.next

      (rs.getBigDecimal(1) aka "big decimal" must beNull).
        and(rs.getBigDecimal(1, 1) aka "big decimal" must beNull).
        and(rs.getBigDecimal("n") aka "big decimal" must beNull).
        and(rs.getBigDecimal("n", 1) aka "big decimal" must beNull)
    }

    "be undefined" in {
      val rs = rowList[Row1[String]].withLabel(1, "n").
        append(row1("str")).resultSet
      rs.next

      (rs.getBigDecimal(1) aka "getBigDecimal" must throwA[SQLException](
        message = "Not a BigDecimal: 1")).
        and(rs.getBigDecimal(1, 1) aka "getBigDecimal" must {
          throwA[SQLException]("Not a BigDecimal: 1")
        }).
        and(rs.getBigDecimal("n").aka("getBigDecimal").must {
          throwA[SQLException]("Not a BigDecimal: n")
        }).
        and(rs.getBigDecimal("n", 1).aka("getBigDecimal").must {
          throwA[SQLException]("Not a BigDecimal: n")
        })

    }

    "converted by index" >> {
      val rs = Seq[(String, ResultSet)](
        "byte" -> (rowList[Row1[Byte]] :+ row1(v.intValue.toByte)).resultSet,
        "short" -> (rowList[Row1[Short]] :+ row1(v.intValue.toShort)).resultSet,
        "int" -> (rowList[Row1[Int]] :+ row1(v.intValue)).resultSet,
        "long" -> (rowList[Row1[Long]] :+ row1(v.longValue)).resultSet,
        "float" -> (rowList[Row1[Float]] :+ row1(v.floatValue)).resultSet,
        "double" -> (rowList[Row1[Double]] :+ row1(v.doubleValue)).resultSet)

      rs.foreach(_._2.next)

      rs foreach { r ⇒
        s"from ${r._1}" in {
          r._2.getBigDecimal(1).doubleValue.
            aka("big decimal") mustEqual v.doubleValue
        }
      }
    }

    "converted by label" >> {
      val rs = Seq[(String, ResultSet)](
        "byte" -> (rowList[Row1[Byte]].
          withLabel(1, "n") :+ row1(v.intValue.toByte)).resultSet,
        "short" -> (rowList[Row1[Short]].
          withLabel(1, "n") :+ row1(v.intValue.toShort)).resultSet,
        "int" -> (rowList[Row1[Int]].
          withLabel(1, "n") :+ row1(v.intValue)).resultSet,
        "long" -> (rowList[Row1[Long]].
          withLabel(1, "n") :+ row1(v.longValue)).resultSet,
        "float" -> (rowList[Row1[Float]].
          withLabel(1, "n") :+ row1(v.floatValue)).resultSet,
        "double" -> (rowList[Row1[Double]].
          withLabel(1, "n") :+ row1(v.doubleValue)).resultSet)

      rs.foreach(_._2.next)

      rs foreach { r ⇒
        s"from ${r._1}" in {
          r._2.getBigDecimal("n").doubleValue.
            aka("big decimal") mustEqual v.doubleValue
        }
      }
    }
  }

  temporalGetterSpec[Date]("Date", new Date(1, 2, 3))
  temporalGetterSpec[Time]("Time", new Time(1, 2, 3))
  temporalGetterSpec[Timestamp]("Timestamp", new Timestamp(1, 2, 3, 5, 6, 7, 8))
}

sealed trait RowListTest { specs: Specification ⇒
  import java.util.Calendar

  implicit def dateByIndex(rs: ResultSet, i: Int): Date = rs.getDate(i)
  implicit def dateByLabel(rs: ResultSet, n: String): Date = rs.getDate(n)
  implicit def dateByIndexWithCal(rs: ResultSet, i: Int, c: Calendar): Date =
    rs.getDate(i, c)
  implicit def dateByLabelWithCal(rs: ResultSet, n: String, c: Calendar): Date =
    rs.getDate(n, c)

  implicit def timeByIndex(rs: ResultSet, i: Int): Time = rs.getTime(i)
  implicit def timeByLabel(rs: ResultSet, n: String): Time = rs.getTime(n)
  implicit def timeByIndexWithCal(rs: ResultSet, i: Int, c: Calendar): Time =
    rs.getTime(i, c)
  implicit def timeByLabelWithCal(rs: ResultSet, n: String, c: Calendar): Time =
    rs.getTime(n, c)

  implicit def tsByIndex(rs: ResultSet, i: Int): Timestamp =
    rs.getTimestamp(i)
  implicit def tsByLabel(rs: ResultSet, n: String): Timestamp =
    rs.getTimestamp(n)
  implicit def tsByIndexWithCal(rs: ResultSet, i: Int, c: Calendar): Timestamp =
    rs.getTimestamp(i, c)
  implicit def tsByLabelWithCal(rs: ResultSet, n: String, c: Calendar): Timestamp = rs.getTimestamp(n, c)

  def temporalGetterSpec[D <: java.util.Date](name: String, v: D)(implicit byIndex: (ResultSet, Int) ⇒ D, byLabel: (ResultSet, String) ⇒ D, byIndexWithCal: (ResultSet, Int, Calendar) ⇒ D, byLabelWithCal: (ResultSet, String, Calendar) ⇒ D) =
    s"$name column from result set" should {
      "not be read by index when not on a row" in {
        (byIndex(rowList[Row1[D]].append(row1(v)).resultSet, 1).
          aka("get") must throwA[SQLException](
            message = "Not on a row")).
            and(byLabel(rowList[Row1[D]].withLabel(1, "n").
              append(row1(v)).resultSet, "n").
              aka("get") must throwA[SQLException]("Not on a row"))

      }

      "be expected one" in {
        val c = Calendar.getInstance
        val rs = rowList[Row1[D]].withLabel(1, "n").:+(row1(v)).resultSet
        rs.next

        (byIndex(rs, 1) aka "big decimal by index" mustEqual v).
          and(byIndexWithCal(rs, 1, c) aka "big decimal by index" mustEqual v).
          and(byLabel(rs, "n") aka "big decimal by name" mustEqual v).
          and(byLabelWithCal(rs, "n", c) aka "big decimal by name" mustEqual v)
      }

      "be null" in {
        val c = Calendar.getInstance
        val rs = (rowList[Row1[D]].
          withLabel(1, "n") :+ row1(null.asInstanceOf[D])).resultSet

        rs.next

        (byIndex(rs, 1) aka "time" must beNull).
          and(byIndexWithCal(rs, 1, c) aka "time" must beNull).
          and(byLabel(rs, "n") aka "time" must beNull).
          and(byLabelWithCal(rs, "n", c) aka "time" must beNull)
      }

      "be undefined" in {
        val c = Calendar.getInstance
        val rs = rowList[Row1[String]].withLabel(1, "n").
          append(row1("str")).resultSet
        rs.next

        (byIndex(rs, 1) aka "get" must throwA[SQLException](
          message = s"Not a $name: 1")).
          and(byIndexWithCal(rs, 1, c) aka "get" must throwA[SQLException](
            message = s"Not a $name: 1")).
          and(byLabel(rs, "n").
            aka("get") must throwA[SQLException](s"Not a $name: n")).
          and(byLabelWithCal(rs, "n", c).
            aka("get") must throwA[SQLException](s"Not a $name: n"))

      }

      "converted by index" >> {
        val rs = Seq[(String, ResultSet)](
          "date" ->
            (rowList[Row1[Date]] :+ row1(new Date(v.getTime))).resultSet,
          "time" ->
            (rowList[Row1[Time]] :+ row1(new Time(v.getTime))).resultSet,
          "ts" -> (rowList[Row1[Timestamp]].
            append(row1(new Timestamp(v.getTime)))).resultSet)

        rs.foreach(_._2.next)

        rs foreach { r ⇒
          s"from ${r._1}" in {
            byIndex(r._2, 1) aka "get" must not(throwA[SQLException])
          }
        }
      }

      "converted by label" >> {
        val rs = Seq[(String, ResultSet)](
          "date" -> (rowList[Row1[Date]].
            withLabel(1, "n") :+ row1(new Date(v.getTime))).resultSet,
          "time" -> (rowList[Row1[Time]].
            withLabel(1, "n") :+ row1(new Time(v.getTime))).resultSet,
          "ts" -> (rowList[Row1[Timestamp]].withLabel(1, "n").
            append(row1(new Timestamp(v.getTime)))).resultSet)

        rs.foreach(_._2.next)

        rs foreach { r ⇒
          s"from ${r._1}" in {
            byLabel(r._2, "n") aka "get" must not(throwA[SQLException])
          }
        }
      }
    }

  // ---

  implicit def byteByIndex(rs: ResultSet, i: Int): Byte = rs.getByte(i)
  implicit def byteByLabel(rs: ResultSet, n: String): Byte = rs.getByte(n)
  implicit def shortByIndex(rs: ResultSet, i: Int): Short = rs.getShort(i)
  implicit def shortByLabel(rs: ResultSet, n: String): Short = rs.getShort(n)
  implicit def intByIndex(rs: ResultSet, i: Int): Int = rs.getInt(i)
  implicit def intByLabel(rs: ResultSet, n: String): Int = rs.getInt(n)
  implicit def longByIndex(rs: ResultSet, i: Int): Long = rs.getLong(i)
  implicit def longByLabel(rs: ResultSet, n: String): Long = rs.getLong(n)
  implicit def floatByIndex(rs: ResultSet, i: Int): Float = rs.getFloat(i)
  implicit def floatByLabel(rs: ResultSet, n: String): Float = rs.getFloat(n)
  implicit def doubleByIndex(rs: ResultSet, i: Int): Double = rs.getDouble(i)
  implicit def doubleByLabel(rs: ResultSet, n: String): Double = rs.getDouble(n)

  def numberGetterSpec[N](name: String, v: N)(implicit num: Numeric[N], byIndex: (ResultSet, Int) ⇒ N, byLabel: (ResultSet, String) ⇒ N) =
    s"$name column from result set" should {
      "not be read by index when not on a row" in {
        (byIndex(rowList[Row1[N]].append(row1(v)).resultSet, 1).
          aka("get") must throwA[SQLException](
            message = "Not on a row")).
            and(byLabel(rowList[Row1[N]].withLabel(1, "n").
              append(row1(v)).resultSet, "n").
              aka("get") must throwA[SQLException]("Not on a row"))

      }

      "be expected one" in {
        val rs = (rowList[Row1[N]].withLabel(1, "n") :+ row1(v)).resultSet
        rs.next

        (byIndex(rs, 1) aka s"$name by index" mustEqual v).
          and(byLabel(rs, "n") aka s"$name by name" mustEqual v)
      }

      "be null (0)" in {
        val rs = (rowList[Row1[N]].
          withLabel(1, "n") :+ row1(null.asInstanceOf[N])).resultSet

        rs.next

        (byIndex(rs, 1) aka s"$name" mustEqual 0).
          and(byLabel(rs, "n") aka s"$name" mustEqual 0)
      }

      "be undefined (-1)" in {
        val rs = rowList[Row1[String]].withLabel(1, "n").
          append(row1("str")).resultSet

        rs.next

        (byIndex(rs, 1) aka s"$name" mustEqual -1).
          and(byLabel(rs, "n") aka s"$name" mustEqual -1)

      }

      "converted by index" >> {
        val rs = Seq[(String, ResultSet)](
          "byte" -> (rowList[Row1[Byte]] :+
            row1(num.toInt(v).toByte)).resultSet,
          "short" ->
            (rowList[Row1[Short]] :+ row1(num.toInt(v).toShort)).resultSet,
          "int" -> (rowList[Row1[Int]] :+ row1(num.toInt(v))).resultSet,
          "long" -> (rowList[Row1[Long]] :+ row1(num.toLong(v))).resultSet,
          "float" -> (rowList[Row1[Float]] :+ row1(num.toFloat(v))).resultSet,
          "double" ->
            (rowList[Row1[Double]] :+ row1(num.toDouble(v))).resultSet)

        rs.foreach(_._2.next)

        rs foreach { r ⇒
          s"from ${r._1}" in { byIndex(r._2, 1) aka s"$name" mustEqual 1 }
        }
      }

      "converted by label" >> {
        val rs = Seq[(String, ResultSet)](
          "byte" -> (rowList[Row1[Byte]].
            withLabel(1, "n") :+ row1(num.toInt(v).toByte)).resultSet,
          "short" -> (rowList[Row1[Short]].
            withLabel(1, "n") :+ row1(num.toInt(v).toShort)).resultSet,
          "int" -> (rowList[Row1[Int]].
            withLabel(1, "n") :+ row1(num.toInt(v))).resultSet,
          "long" -> (rowList[Row1[Long]].
            withLabel(1, "n") :+ row1(num.toLong(v))).resultSet,
          "float" -> (rowList[Row1[Float]].
            withLabel(1, "n") :+ row1(num.toFloat(v))).resultSet,
          "double" -> (rowList[Row1[Double]].
            withLabel(1, "n") :+ row1(num.toDouble(v))).resultSet)

        rs.foreach(_._2.next)

        rs foreach { r ⇒
          s"from ${r._1}" in { byLabel(r._2, "n") aka s"$name" mustEqual 1 }
        }
      }
    }

}
