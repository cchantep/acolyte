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

  def rowList[R <: Row]: RowList[R] = new RowList[R]

  def row1[A](c1: A): Row.Row1[A] = RowList.row1(c1)
  def row2[A, B](c1: A, c2: B): Row.Row2[A, B] = RowList.row2(c1, c2)
  def row2[A, B, C](c1: A, c2: B, c3: C): Row.Row3[A, B, C] =
    RowList.row3(c1, c2, c3)
}
