package acolyte.jdbc

import java.lang.{ Float ⇒ JFloat }
import java.sql.Types

import org.specs2.matcher.{ Expectable, Matcher, MatchersImplicits }

object ImmutableArraySpec
  extends org.specs2.mutable.Specification with MatchersImplicits {

  "Immutable array" title

  "Creation of empty array" should {
    "fail with null base class" in {
      ImmutableArray.getInstance(null).
        aka("null class") must throwA[IllegalArgumentException]("No base class")
    }

    "fail with unsupported base class" in {
      ImmutableArray.getInstance(classOf[Seq[Int]]).
        aka("unsupported class") must throwA[IllegalArgumentException](
          message = "Unsupported base class")
    }

    "be successful for String as base class" in {
      ImmutableArray.getInstance(classOf[String]).
        aka("string array") must beLike {
          case strArr ⇒
            (strArr.baseClass aka "base class" mustEqual classOf[String]) and
              (strArr.getBaseType aka "base type" mustEqual Types.VARCHAR) and
              (strArr.getBaseTypeName aka "base type name" mustEqual "VARCHAR").
              and(strArr.elements.isEmpty aka "empty list" must beTrue)
        }
    }

    "be successful for Float as base class" in {
      ImmutableArray.getInstance(classOf[JFloat]).
        aka("string array") must beLike {
          case strArr ⇒
            (strArr.baseClass aka "base class" mustEqual classOf[JFloat]) and
              (strArr.getBaseType aka "base type" mustEqual Types.FLOAT) and
              (strArr.getBaseTypeName aka "base type name" mustEqual "FLOAT").
              and(strArr.elements.isEmpty aka "empty list" must beTrue)
        }
    }
  }

  "Creation of array copy" should {
    "fail with null array" in {
      ImmutableArray.getInstance(
        classOf[String],
        null.asInstanceOf[Array[String]]) aka "creation" must (
          throwA[IllegalArgumentException]("Invalid element array"))
    }

    "be successful with given array" in mustBeExpectedArray("array copy") {
      ImmutableArray.getInstance(classOf[String], Array("Ab", "cD", "EF"))
    }
  }

  "Creation of list copy" should {
    "fail with null array" in {
      ImmutableArray.getInstance(
        classOf[String],
        null.asInstanceOf[java.util.List[String]]) aka "creation" must (
          throwA[IllegalArgumentException]("Invalid element list"))
    }

    "be successful with given list" in {
      lazy val list = {
        val l = new java.util.ArrayList[String]()
        l.add("Ab"); l.add("cD"); l.add("EF"); l
      }

      mustBeExpectedArray("list copy") {
        ImmutableArray.getInstance(classOf[String], list)
      }
    }
  }

  "Free" should {
    "be successful" in {
      ImmutableArray.getInstance(classOf[String]).free().
        aka("free operation") must not(throwA[Exception])

    }
  }

  "Equals operation" should {
    "respect Object contract" in {
      ImmutableArray.getInstance(classOf[String], Array("a", "b")).
        aka("string array") must_== ImmutableArray.getInstance(
          classOf[String], Array("a", "b"))
    }
  }

  @inline def mustBeExpectedArray[T](label: String)(array: ImmutableArray[T]) =
    array aka label must beLike {
      case strArr ⇒
        (strArr.baseClass aka "base class" mustEqual classOf[String]) and
          (strArr.getBaseType aka "base type" mustEqual Types.VARCHAR) and
          (strArr.getBaseTypeName aka "base type name" mustEqual "VARCHAR").
          and(strArr.getArray aka "element array" must beLike {
            case elmts: Array[String] ⇒
              (elmts.size aka "size" must_== 3) and
                (elmts(0) aka "1st element" must_== "Ab") and
                (elmts(1) aka "2nd element" must_== "cD") and
                (elmts(2) aka "3rd element" must_== "EF")
          }).and(strArr.getArray(1, 2) aka "element sub-array #1" must beLike {
            case elmts: Array[String] ⇒
              (elmts.size aka "size" must_== 2) and
                (elmts(0) aka "1st element" must_== "cD") and
                (elmts(1) aka "2nd element" must_== "EF")
          }).and(strArr.getArray(0, 2) aka "element sub-array #2" must beLike {
            case elmts: Array[String] ⇒
              (elmts.size aka "size" must_== 2) and
                (elmts(0) aka "1st element" must_== "Ab") and
                (elmts(1) aka "2nd element" must_== "cD")
          }).and(strArr.getArray(2, 1) aka "element sub-array #3" must beLike {
            case elmts: Array[String] ⇒
              (elmts.size aka "size" must_== 1) and
                (elmts(0) aka "1st element" must_== "EF")
          }).and(strArr.getResultSet aka "result set" must beLike {
            case rs ⇒
              (rs.next aka "has 1st element" must beTrue) and
                (rs.getString(1) aka "1st element" must_== "Ab") and
                (rs.next aka "has 2nd element" must beTrue) and
                (rs.getString(1) aka "2nd element" must_== "cD") and
                (rs.next aka "has 3rd element" must beTrue) and
                (rs.getString(1) aka "3rd element" must_== "EF")
          }).and(strArr.getResultSet(1, 2) aka "result set #1" must beLike {
            case rs ⇒
              (rs.next aka "has 1st element" must beTrue) and
                (rs.getString(1) aka "1st element" must_== "cD") and
                (rs.next aka "has 2nd element" must beTrue) and
                (rs.getString(1) aka "2nd element" must_== "EF") and
                (rs.next aka "has 3rd element" must beFalse)
          }).and(strArr.getResultSet(0, 2) aka "result set #2" must beLike {
            case rs ⇒
              (rs.next aka "has 1st element" must beTrue) and
                (rs.getString(1) aka "1st element" must_== "Ab") and
                (rs.next aka "has 2nd element" must beTrue) and
                (rs.getString(1) aka "2nd element" must_== "cD") and
                (rs.next aka "has 3rd element" must beFalse)
          }).and(strArr.getResultSet(2, 1) aka "result set #3" must beLike {
            case rs ⇒
              (rs.next aka "has 1st element" must beTrue) and
                (rs.getString(1) aka "1st element" must_== "EF") and
                (rs.next aka "has 2nd element" must beFalse)
          })
    }

}
