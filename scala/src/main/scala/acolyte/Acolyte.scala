package acolyte

import java.util.{ ArrayList, List ⇒ JList }
import java.sql.ResultSet

import scala.language.implicitConversions
import scala.collection.JavaConversions

import acolyte.ParameterMetaData.ParameterDef
import acolyte.StatementHandler.Parameter
import acolyte.CompositeHandler.{ QueryHandler, UpdateHandler }
import acolyte.Row.Row1

// Acolyte DSL
object Acolyte {
  def handleStatement = new CompositeHandler()

  implicit def CompositeHandlerAsScala(h: CompositeHandler): ScalaCompositeHandler = new ScalaCompositeHandler(h)

  implicit def ResultRowAsScala[R <: Row](r: R): ScalaResultRow =
    new ScalaResultRow(r)

  implicit def RowListAsScala[R <: Row](l: RowList[R]): ScalaRowList[R] =
    new ScalaRowList(l)

  // Row lists creation @todo import RowLists._
  def rowList1[A]: RowList1[A] = new RowList1[A]()
  def rowList2[A, B]: RowList2[A, B] = new RowList2[A, B]()
  def rowList3[A, B, C]: RowList3[A, B, C] = new RowList3[A, B, C]()

}

case class Execution(
  sql: String,
  parameters: List[(ParameterDef, AnyRef)])

final class ScalaCompositeHandler(
    b: CompositeHandler) extends CompositeHandler {

  def withUpdateHandler(h: Execution ⇒ Int): CompositeHandler = {
    b.withUpdateHandler(new UpdateHandler {
      def apply(sql: String, p: JList[Parameter]): Int = {
        val ps: List[(ParameterDef, AnyRef)] =
          JavaConversions.asScalaIterable(p).
            foldLeft(Nil: List[(ParameterDef, AnyRef)]) { (l, t) ⇒
              l :+ (t.left -> t.right)
            }

        h(Execution(sql, ps))
      }
    })
  }

  def withQueryHandler(h: Execution ⇒ ResultSet): CompositeHandler = {
    b.withQueryHandler(new QueryHandler {
      def apply(sql: String, p: JList[Parameter]): ResultSet = {
        val ps: List[(ParameterDef, AnyRef)] =
          JavaConversions.asScalaIterable(p).
            foldLeft(Nil: List[(ParameterDef, AnyRef)]) { (l, t) ⇒
              l :+ (t.left -> t.right)
            }

        h(Execution(sql, ps))
      }
    })
  }
}

final class ScalaResultRow(r: Row) extends Row {
  lazy val cells = r.cells

  lazy val list: List[Any] =
    JavaConversions.iterableAsScalaIterable(cells).foldLeft(List[Any]()) {
      (l, v) ⇒ l :+ v
    }

}

final class ScalaRowList[R <: Row](l: RowList[R]) extends RowList[R] {
  override def append(row: R) = l.append(row)
  override def resultSet = l.resultSet

  def getColumnClasses = l.getColumnClasses
  def getColumnLabels = l.getColumnLabels
  def getRows = l.getRows

  def withLabel(i: Int, label: String): RowList[R] = l.withLabel(i, label)

  // Extension
  def :+(row: R) = append(row)

  def withLabels(labels: (Int, String)*): RowList[R] =
    labels.foldLeft[RowList[R]](this) { (l, t) ⇒ l.withLabel(t._1, t._2) }
}
