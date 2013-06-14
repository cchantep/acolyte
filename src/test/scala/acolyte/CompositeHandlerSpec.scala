package acolyte

import org.specs2.mutable.Specification

import acolyte.AbstractStatement.NO_PARAMS
import acolyte.CompositeHandler.{ QueryHandler, UpdateHandler }
import acolyte.Row.Row2

import acolyte.Acolyte._

object CompositeHandlerSpec extends Specification {
  "Composite statement handler" title

  "Query detection" should {
    "not be inited" in {
      handleStatement.withQueryDetection(null.
        asInstanceOf[java.util.regex.Pattern]).
        aka("init") must throwA[IllegalArgumentException]

    }

    "always match" in {
      lazy val h = handleStatement.withQueryDetection(".*")

      (h.isQuery("TEST") aka "detection 1" must beTrue).
        and(h.isQuery("SELECT * FROM table") aka "detection 2" must beTrue)
    }

    "match with a single pattern" in {
      handleStatement.withQueryDetection("^SELECT ").
        isQuery("SELECT * FROM table") aka "detection" must beTrue

    }

    "not match" in {
      handleStatement.withQueryDetection("^SELECT ").
        isQuery("TEST") aka "detection" must beFalse

    }

    "match with multiple patterns" in {
      handleStatement.withQueryDetection("^SELECT ").
        withQueryDetection("EXEC that_proc").
        isQuery("EXEC that_proc('test')") aka "detection" must beTrue

    }
  }

  "Update handling" should {
    "not be inited" in {
      handleStatement.withUpdateHandler(null.asInstanceOf[UpdateHandler]).
        aka("init") must throwA[IllegalArgumentException]

    }

    "be successful" in {
      ((handleStatement withUpdateHandler { e: Execution ⇒ 1 }).
        whenSQLUpdate("TEST", NO_PARAMS) aka "count" mustEqual 1).
        and((handleStatement withUpdateHandler { e: Execution ⇒ 3 }).
          whenSQLUpdate("TEST", NO_PARAMS) aka "count" mustEqual 3).
        and((handleStatement withUpdateHandler { e: Execution ⇒ 10 }).
          whenSQLUpdate("TEST", NO_PARAMS) aka "count" mustEqual 10)

    }
  }

  "Query handling" should {
    "not be inited" in {
      handleStatement.withQueryHandler(null.asInstanceOf[QueryHandler]).
        aka("init") must throwA[IllegalArgumentException]

    }

    "be successful for empty resultset" in {
      (handleStatement withQueryHandler { e: Execution ⇒
        AbstractResultSet.EMPTY
      }).whenSQLQuery("SELECT *", NO_PARAMS).
        aka("resultset") mustEqual AbstractResultSet.EMPTY
    }

    "be successful for not-empty resultset" in {
      lazy val rs = (rowList[Row2[String, Float]] :+ row2("str", 1.23.toFloat)).
        resultSet

      (handleStatement withQueryHandler { e: Execution ⇒ rs }).
        whenSQLQuery("SELECT *", NO_PARAMS) aka "resultset" mustEqual rs

    }
  }
}
