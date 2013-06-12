package acolyte

import java.util.{ ArrayList, List ⇒ JList }
import org.apache.commons.lang3.tuple.{ ImmutablePair ⇒ JTupple }

import scala.language.implicitConversions
import scala.collection.JavaConversions

import acolyte.ParameterMetaData.Parameter
import acolyte.RuleStatementHandler.{ QueryHandler, UpdateHandler }

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

// Acolyte DSL
object Acolyte {
  def handleStatement = new RuleStatementHandler()

  implicit def RuleStatementHandlerAsScala(h: RuleStatementHandler): ScalaRuleStatementHandler = new ScalaRuleStatementHandler(h)

  def rowList(rows: Row*) = new RowList(JavaConversions seqAsJavaList rows)
}
