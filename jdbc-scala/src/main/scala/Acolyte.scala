// -*- mode: scala -*-
package acolyte

import java.util.{ ArrayList, List ⇒ JList }
import java.util.regex.Pattern
import java.sql.{ Connection ⇒ SqlConnection, Statement, SQLWarning }

import scala.language.implicitConversions
import scala.collection.JavaConversions

import acolyte.StatementHandler.Parameter
import acolyte.AbstractCompositeHandler.{ QueryHandler, UpdateHandler }
import acolyte.RowList.{ Column ⇒ Col }

/**
 * Acolyte DSL.
 *
 * {{{
 * import acolyte.Acolyte.{ connection, handleStatement }
 * import acolyte.Implicits._
 *
 * connection {
 *   handleStatement.withQueryDetection("...").
 *     withQueryHandler({ e: QueryExecution => ... }).
 *     withUpdateHandler({ e: UpdateExecution => ... })
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

  /**
   * Executes |f| using connection accepting only queries,
   * and answering with |result| to any query.
   *
   * {{{
   * import acolyte.Acolyte.withQueryResult
   *
   * val str: String = withQueryResult(queryRes) { con => "str" }
   * }}}
   */
  def withQueryResult[A](res: QueryResult)(f: SqlConnection ⇒ A): A =
    f(connection(handleQuery(_ ⇒ res)))

}

/**
 * Acolyte implicit conversions for Scala use.
 *
 * {{{
 * import acolyte.Implicits._
 * }}}
 */
object Implicits extends ScalaRowLists with CompositeHandlerImplicits {

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
    Col(c._1, c._2)

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
  implicit def IntUpdateResult(updateCount: Int) = new UpdateResult(updateCount)

  /**
   * Allows to directly use row list as query result.
   *
   * {{{
   * val qr: QueryResult = stringList
   * }}}
   */
  implicit def RowListAsResult[R <: RowList[_]](r: R): QueryResult = r.asResult

  /**
   * Allows to directly use string as query result.
   *
   * {{{
   * val qr: QueryResult = "str"
   * }}}
   */
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

}

private object ScalaCompositeHandler {
  def empty = new ScalaCompositeHandler(null, null, null)
}

/**
 * Convertions from Java datatypes to Scala.
 */
@deprecated("Direct manipulation of row is no longer required", "1.0.12")
object JavaConverters {

  /**
   * Pimps result row.
   *
   * {{{
   * row.list // Scala list equivalent to .cells
   * }}}
   */
  implicit def rowAsScala[R <: Row](r: R): ScalaRow = new ScalaRow(r)

  final class ScalaRow(r: Row) extends Row {
    lazy val cells = r.cells

    lazy val list: List[Any] =
      JavaConversions.iterableAsScalaIterable(cells).foldLeft(List[Any]()) {
        (l, v) ⇒ l :+ v
      }
  }
}
