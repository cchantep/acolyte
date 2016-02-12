// -*- mode: scala -*-
package acolyte.jdbc.play

import acolyte.jdbc.{ AcolyteDSL, QueryResult, ScalaCompositeHandler }

import play.api.db.Database

/** Acolyte DSL for JDBC. */
object PlayJdbcDSL {

  /**
   * Returns a Play JDBC context, able to apply function `Database => A`.
   * The `result` is given to any query.
   *
   * {{{
   * import acolyte.jdbc.PlayJdbcDSL.withQueryResult
   *
   * val str: String = withPlayDBResult(queryRes) { con => "str" }
   * }}}
   */
  def withPlayDBResult(res: QueryResult) =
    withPlayDB(AcolyteDSL.handleQuery(_ ⇒ res))

  /**
   * Returns a Play JDBC context, able to apply function `Database => A`.
   * The `handler` is used to delegates query execution,
   * as soon as the statement is matching `withQueryDetection`.
   *
   * {{{
   * import acolyte.jdbc.QueryExecution
   * import acolyte.jdbc.AcolyteDSL.handleStatement
   *
   * handleStatement withPlayDBHandler { e: QueryExecution => aQueryResult }
   *
   * // With pattern matching ...
   * import acolyte.jdbc.ParameterVal
   *
   * val runner = handleStatement withPlayDB {
   *   _ match {
   *     case QueryExecution("SELECT * FROM Test WHERE id = ?", ParameterVal(1) :: Nil) => aQueryResult
   *     case _ => otherResult
   *   }
   * }
   *
   * runner { db =>
   *   // Any code using Play Database
   * }
   * }}}
   */
  def withPlayDB(handler: ScalaCompositeHandler) =
    new PlayJdbcContext(handler)
}

/** Acolyte handler for Play JDBC. */
final class PlayJdbcContext(handler: ScalaCompositeHandler) {

  /**
   * @param f the function applied on the Acolyte/Play `Database`
   */
  def apply[A](f: Database ⇒ A): A = {
    lazy val db = new AcolyteDatabase(handler)

    try {
      f(db)
    } catch {
      case e: Throwable ⇒ sys.error(s"error: $e")
    } finally {
      db.shutdown()
    }
  }
}
