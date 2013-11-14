// -*- mode: scala -*-
package acolyte

import java.util.{ ArrayList, List ⇒ JList }
import java.sql.{ Statement, SQLWarning }

import scala.language.implicitConversions
import scala.collection.JavaConversions

import acolyte.ParameterMetaData.ParameterDef
import acolyte.StatementHandler.Parameter
import acolyte.CompositeHandler.{ QueryHandler, UpdateHandler }
import acolyte.RowList.Column

// Acolyte DSL
object Acolyte extends ScalaRowLists with ScalaRows {
  def handleStatement = new CompositeHandler()

  def connection(h: ConnectionHandler) = Driver.connection(h)
  def connection(h: StatementHandler) = Driver.connection(h)

  implicit def CompositeHandlerAsScala(h: CompositeHandler): ScalaCompositeHandler = new ScalaCompositeHandler(h)

  implicit def ResultRowAsScala[R <: Row](r: R): ScalaResultRow =
    new ScalaResultRow(r)

  implicit def PairAsColumn[T](c: (Class[T], String)): Column[T] =
    Column.defineCol(c._1, c._2)

  implicit def IntUpdateHandler(h: UpdateExecution ⇒ Int): UpdateHandler =
    new UpdateHandler {
      def apply(sql: String, p: JList[Parameter]): UpdateResult =
        new UpdateResult(h(UpdateExecution(sql, scalaParameters(p))))
    }

  implicit def FunctionUpdateHandler(h: UpdateExecution ⇒ UpdateResult): UpdateHandler = new UpdateHandler {
    def apply(sql: String, p: JList[Parameter]): UpdateResult =
      h(UpdateExecution(sql, scalaParameters(p)))
  }

  implicit def WarningUpdateHandler(h: UpdateExecution ⇒ SQLWarning): UpdateHandler =
    new UpdateHandler {
      def apply(sql: String, p: JList[Parameter]): UpdateResult =
        UpdateResult.Nothing.
          withWarning(h(UpdateExecution(sql, scalaParameters(p))))
    }

  implicit def RowListQueryHandler[R <: RowList[_]](h: QueryExecution ⇒ R): QueryHandler = new QueryHandler {
    def apply(sql: String, params: JList[Parameter]): QueryResult =
      h(QueryExecution(sql, scalaParameters(params))).asResult
  }

  implicit def SingleValueRow[A, B](value: A)(implicit f: A ⇒ B): Row.Row1[B] = Rows.row1[B](f(value))

  private def scalaParameters(p: JList[Parameter]): List[ExecutedParameter] =
    JavaConversions.collectionAsScalaIterable(p).
      foldLeft(Nil: List[ExecutedParameter]) { (l, t) ⇒
        l :+ DefinedParameter(t.right, t.left)
      }

}

trait Execution

case class QueryExecution(
  sql: String,
  parameters: List[ExecutedParameter]) extends Execution

case class UpdateExecution(
  sql: String,
  parameters: List[ExecutedParameter]) extends Execution

trait ExecutedParameter {
  def value: Any
}

case class DefinedParameter(
    value: Any,
    definition: ParameterDef) extends ExecutedParameter {

  override lazy val toString = s"Param($value, ${definition.sqlTypeName})"
}

object ParameterVal {
  def apply(v: Any): ExecutedParameter = new ExecutedParameter {
    val value = v
    override lazy val toString = s"Param($value)"
  }

  def unapply(p: ExecutedParameter): Option[Any] = Some(p.value)
}

final class ScalaCompositeHandler(
    b: CompositeHandler) extends CompositeHandler {

  def withQueryHandler(h: QueryExecution ⇒ QueryResult): CompositeHandler = {
    b.withQueryHandler(new QueryHandler {
      def apply(sql: String, p: JList[Parameter]): QueryResult = {
        val ps = JavaConversions.collectionAsScalaIterable(p).
          foldLeft(Nil: List[ExecutedParameter]) { (l, t) ⇒
            l :+ DefinedParameter(t.right, t.left)
          }

        h(QueryExecution(sql, ps))
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

final class ScalaRowList[L <: RowList[R], R <: Row](l: L) {
  def :+[V](values: V)(implicit conv: V ⇒ R): L =
    l.append(conv(values)).asInstanceOf[L]

  def :+(row: R): L = l.append(row).asInstanceOf[L]

  def withLabels(labels: (Int, String)*): L =
    labels.foldLeft(l) { (l, t) ⇒ l.withLabel(t._1, t._2).asInstanceOf[L] }

}
