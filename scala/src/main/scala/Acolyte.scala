// -*- mode: scala -*-
package acolyte

import java.util.{ ArrayList, List ⇒ JList }
import java.util.regex.Pattern
import java.sql.{ Statement, SQLWarning }

import scala.language.implicitConversions
import scala.collection.JavaConversions

import acolyte.ParameterMetaData.ParameterDef
import acolyte.StatementHandler.Parameter
import acolyte.AbstractCompositeHandler.{ QueryHandler, UpdateHandler }
import acolyte.RowList.Column

/**
 * Acolyte DSL.
 *
 * {{{
 * import acolyte.Acolyte.{ connection, handleStatement }
 * import acolyte.Implicits._
 *
 * connection {
 *   handleStatement.withQueryDetection("...").
 *     withQueryHandler({ e: Execution => ... }).
 *     withUpdateHandler({ e: Execution => ... })
 * }
 * }}}
 */
object Acolyte {

  /**
   * Creates a connection, whose statement will be passed to given handler.
   *
   * @param h statement handler
   * @param p connection properties
   * @return a new Acolyte connection
   *
   * {{{
   * connection(handler) // without connection properties
   *
   * // With connection property to fallback untyped null
   * connection(handler, "acolyte.parameter.untypedNull" -> "true")
   * }}}
   */
  def connection(h: AbstractCompositeHandler[_], p: (String, String)*) =
    Driver.connection(h, p.foldLeft(new java.util.Properties()) { (ps, t) ⇒
      ps.put(t._1, t._2); ps
    })

  /**
   * Creates a connection, managed with given handler.
   *
   * @param h connection handler
   * @param p connection properties
   * @return a new Acolyte connection
   *
   * {{{
   * connection(handler) // without connection properties
   *
   * // With connection property to fallback untyped null
   * connection(handler, "acolyte.parameter.untypedNull" -> "true")
   * }}}
   */
  def connection(h: ConnectionHandler, p: (String, String)*) =
    Driver.connection(h, p.foldLeft(new java.util.Properties()) { (ps, t) ⇒
      ps.put(t._1, t._2); ps
    })

  /**
   * Creates an empty handler.
   *
   * {{{
   * import acolyte.Acolyte.{ connection, handleStatement }
   *
   * connection { handleStatement }
   * }}}
   */
  def handleStatement = ScalaCompositeHandler.empty

  /**
   * Creates a new handler detecting all statements as queries
   * (like `handleStatement.withQueryDetection(".*").withQueryHandler(h)`).
   *
   * {{{
   * import acolyte.Acolyte.{ connection, handleQuery }
   *
   * connection { handleQuery { _ => res }
   * }}}
   */
  def handleQuery(h: QueryExecution ⇒ QueryResult): ScalaCompositeHandler =
    handleStatement withQueryDetection ".*" withQueryHandler h
  // TODO? (h: QueryExec => A)(implicit c: A => QueryResult)

}

/**
 * Acolyte implicit conversions for Scala use.
 *
 * {{{
 * import acolyte.Implicits._
 * }}}
 */
object Implicits
    extends ScalaRowLists with ScalaRows with CompositeHandlerImplicits {

  /**
   * Pimps result row.
   *
   * {{{
   * row.list // Scala list equivalent to .cells
   * }}}
   */
  implicit def ResultRowAsScala[R <: Row](r: R): ScalaResultRow =
    new ScalaResultRow(r)

  /**
   * Converts tuple to column definition.
   *
   * {{{
   * import acolyte.Implicits.PairAsColumn
   *
   * rowList1(classOf[Int] -> "name") // rowList(new Column(...))
   * }}}
   */
  implicit def PairAsColumn[T](c: (Class[T], String)): Column[T] =
    Column(c._1, c._2)

}

sealed trait Execution

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

final class ScalaCompositeHandler(qd: Array[Pattern], qh: QueryHandler, uh: UpdateHandler) extends AbstractCompositeHandler[ScalaCompositeHandler](qd, qh, uh) {

  /**
   * Returns handler that detects statement matching given pattern(s)
   * as query.
   *
   * {{{
   * import acolyte.Acolyte.handleStatement
   *
   * // Created handle will detect as query statements
   * // either starting with 'SELECT ' or containing 'EXEC proc'.
   * handleStatement.withQueryDetection("^SELECT ", "EXEC proc")
   * }}}
   */
  def withQueryDetection(pattern: Pattern*) = new ScalaCompositeHandler(
    queryDetectionPattern(pattern: _*), queryHandler, updateHandler)

  /**
   * Returns handler that delegates query execution to |h| function.
   * Given function will be used only if executed statement is detected
   * as a query by withQueryDetection.
   *
   * {{{
   * import acolyte.QueryExecution
   * import acolyte.Acolyte.handleStatement
   *
   * handleStatement withQueryHandler { e: QueryExecution => aQueryResult }
   *
   * // With pattern matching ...
   * import acolyte.ParameterVal
   *
   * handleStatement withQueryHandler {
   *   _ match {
   *     case QueryExecution("SELECT * FROM Test WHERE id = ?", ParameterVal(1) :: Nil) => aQueryResult
   *     case _ => otherResult
   *   }
   * }
   * }}}
   */
  def withQueryHandler(h: QueryExecution ⇒ QueryResult): ScalaCompositeHandler = new ScalaCompositeHandler(queryDetection, new QueryHandler {
    def apply(sql: String, p: JList[Parameter]): QueryResult =
      h(QueryExecution(sql, scalaParameters(p)))
  }, updateHandler)

  /**
   * Returns handler that delegates update execution to |h| function.
   * Given function will be used only if executed statement is not detected
   * as a query by withQueryDetection.
   *
   * {{{
   * import acolyte.UpdateExecution
   * import acolyte.Acolyte.handleStatement
   *
   * handleStatement withUpdateHandler { e: UpdateExecution => aQueryResult }
   *
   * // With pattern matching ...
   * import acolyte.ParameterVal
   *
   * handleStatement withUpdateHandler {
   *   _ match {
   *     case UpdateExecution("INSERT INTO Country (code, name) VALUES (?, ?)", ParameterVal(code) :: ParameterVal(name) :: Nil) => 1 /* update count */
   *     case _ => otherResult
   *   }
   * }
   * }}}
   */
  def withUpdateHandler(h: UpdateExecution ⇒ UpdateResult): ScalaCompositeHandler = new ScalaCompositeHandler(queryDetection, queryHandler, new UpdateHandler {
    def apply(sql: String, p: JList[Parameter]): UpdateResult =
      h(UpdateExecution(sql, scalaParameters(p)))
  })

  private def scalaParameters(p: JList[Parameter]): List[ExecutedParameter] =
    JavaConversions.collectionAsScalaIterable(p).
      foldLeft(Nil: List[ExecutedParameter]) { (l, t) ⇒
        l :+ DefinedParameter(t.right, t.left)
      }

}

trait CompositeHandlerImplicits { srl: ScalaRowLists ⇒

  /**
   * Allows to directly use update count as update result.
   *
   * {{{
   * ScalaCompositeHandler.empty withUpdateHandler { exec ⇒ 1/*count*/ }
   * }}}
   */
  implicit def IntUpdateResult(updateCount: Int): UpdateResult =
    new UpdateResult(updateCount)

  /**
   * Allows to directly use row list as query result.
   *
   * {{{
   * val qr: QueryResult = stringList
   * }}}
   */
  implicit def RowListAsResult[R <: RowList[_]](r: R): QueryResult = r.asResult

  implicit def StringAsResult(v: String): QueryResult =
    (RowLists.stringList :+ v).asResult

  implicit def BooleanAsResult(v: Boolean): QueryResult =
    (RowLists.booleanList :+ v).asResult

  implicit def ByteAsResult(v: Byte): QueryResult =
    (RowLists.byteList :+ v).asResult

  implicit def ShortAsResult(v: Short): QueryResult =
    (RowLists.shortList :+ v).asResult

  implicit def IntAsResult(v: Int): QueryResult =
    (RowLists.intList :+ v).asResult

  implicit def LongAsResult(v: Long): QueryResult =
    (RowLists.longList :+ v).asResult

  implicit def FloatAsResult(v: Float): QueryResult =
    (RowLists.floatList :+ v).asResult

  implicit def DoubleAsResult(v: Double): QueryResult =
    (RowLists.doubleList :+ v).asResult

  implicit def ScalaBigDecimalAsResult(v: BigDecimal): QueryResult =
    (RowLists.bigDecimalList :+ v.bigDecimal).asResult

  implicit def JavaBigDecimalAsResult(v: java.math.BigDecimal): QueryResult =
    (RowLists.bigDecimalList :+ v).asResult

  implicit def DateAsTimestampResult(v: java.util.Date): QueryResult =
    (RowLists.timestampList :+ new java.sql.Timestamp(v.getTime)).asResult

  implicit def SqlDateAsTimestampResult(v: java.sql.Timestamp): QueryResult =
    (RowLists.timestampList :+ v).asResult

  /**
   * Converts a single value as row, so that it can be added to row list.
   *
   * {{{
   * stringList :+ "singleVal" // SingleValueRow("singleVal")
   * }}}
   */
  implicit def SingleValueRow[A, B](value: A)(implicit f: A ⇒ B): Row.Row1[B] = Rows.row1[B](f(value))

}

private object ScalaCompositeHandler {
  def empty = new ScalaCompositeHandler(null, null, null)
}

final class ScalaResultRow(r: Row) extends Row {
  lazy val cells = r.cells

  lazy val list: List[Any] =
    JavaConversions.iterableAsScalaIterable(cells).foldLeft(List[Any]()) {
      (l, v) ⇒ l :+ v
    }

}

/**
 * Pimped row list.
 */
final class ScalaRowList[L <: RowList[R], R <: Row](l: L) {

  /**
   * Symbolic alias for `append` operation.
   *
   * {{{
   * rowList :+ row
   * }}}
   */
  def :+(row: R): L = l.append(row).asInstanceOf[L]

  /**
   * Defines column label(s) per position(s) (> 0).
   *
   * {{{
   * rowList.withLabels(1 -> "label1", 2 -> "label2")
   * }}}
   */
  def withLabels(labels: (Int, String)*): L =
    labels.foldLeft(l) { (l, t) ⇒ l.withLabel(t._1, t._2).asInstanceOf[L] }

}
