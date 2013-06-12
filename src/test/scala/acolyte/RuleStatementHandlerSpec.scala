package acolyte

import org.specs2.mutable.Specification

import acolyte.AbstractStatement.NO_PARAMS
import acolyte.ParameterMetaData.Parameter
import acolyte.RuleStatementHandler.{ QueryHandler, UpdateHandler }

import scala.language.implicitConversions

import StatementDSL._

object RuleStatementHandlerSpec extends Specification {
  "Rule-based statement handler" title

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
  }
}

object StatementDSL {
  import java.util.{ ArrayList, List ⇒ JList }
  import org.apache.commons.lang3.tuple.{ ImmutablePair ⇒ JTupple }
  import scala.collection.JavaConversions

  def handleStatement = new RuleStatementHandler()

  case class Execution(
    sql: String,
    parameters: List[(Parameter, AnyRef)])

  final class ScalaRuleStatementHandler(
      b: RuleStatementHandler) extends RuleStatementHandler {

    def withUpdateHandler(h: Execution ⇒ Int): RuleStatementHandler = {
      super.withUpdateHandler(new UpdateHandler {
        def apply(sql: String, p: JList[JTupple[Parameter, Object]]): Int = {
          val ps: List[(Parameter, AnyRef)] =
            JavaConversions.asScalaIterable(p).
              foldLeft(Nil: List[(Parameter, AnyRef)]) { (l, t) ⇒
                l :+ (t.left -> t.right)
              }

          h(Execution(sql, ps))
        }
      })
    }

    private def getJavaParams(e: Execution): JList[JTupple[Parameter, Object]] =
      e.parameters.foldLeft(new ArrayList[JTupple[Parameter, Object]]()) {
        (l, t) ⇒ l.add(JTupple.of(t._1, t._2)); l
      }

  }

  implicit def RuleStatementHandlerAsScala(h: RuleStatementHandler): ScalaRuleStatementHandler = new ScalaRuleStatementHandler(h)
}
