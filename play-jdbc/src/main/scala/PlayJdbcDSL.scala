// -*- mode: scala -*-
package acolyte.jdbc.play

import acolyte.jdbc.{ AcolyteDSL, QueryResult, ResourceHandler, ScalaCompositeHandler }

import play.api.db.Database

/** Acolyte DSL for JDBC. */
object PlayJdbcDSL {

  /**
   * Returns a Play JDBC context, able to apply function `Database => A`.
   * The `result` is given to any query.
   *
   * {{{
   * import acolyte.jdbc.QueryResult
   * import acolyte.jdbc.Implicits._
   *
   * import acolyte.jdbc.play.PlayJdbcDSL.withPlayDBResult
   *
   * def queryRes: QueryResult = "foo"
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
   * import acolyte.jdbc.{ QueryExecution, QueryResult }
   * import acolyte.jdbc.Implicits._
   *
   * import acolyte.jdbc.AcolyteDSL.handleStatement
   * import acolyte.jdbc.play.PlayJdbcDSL.withPlayDB
   *
   * def aQueryResult: QueryResult = "lorem"
   * val otherResult = "ipsum"
   *
   * withPlayDB(
   *   handleStatement withQueryHandler { e: QueryExecution => aQueryResult })
   *
   * // With pattern matching ...
   * import acolyte.jdbc.{ ExecutedParameter => P }
   *
   * def runner = withPlayDB(handleStatement withQueryHandler {
   *   _ match {
   *     case QueryExecution(
   *       "SELECT * FROM Test WHERE id = ?", P(1) :: Nil) =>
   *       aQueryResult
   *
   *     case _ => otherResult
   *   }
   * })
   *
   * runner { db: play.api.db.Database =>
   *   // Any code using Play Database
   * }
   * }}}
   */
  def withPlayDB(handler: ScalaCompositeHandler) =
    new PlayJdbcContext(handler)
}

/** Acolyte handler for Play JDBC. */
final class PlayJdbcContext(
    handler: ScalaCompositeHandler,
    resourceHandler: ResourceHandler = new ResourceHandler.Default()) {

  /**
   * @param f the function applied on the Acolyte/Play `Database`
   */
  def apply[A](f: Database ⇒ A): A = {
    lazy val db = new AcolyteDatabase(handler, resourceHandler)

    try {
      f(db)
    } finally {
      db.shutdown()
    }
  }
}
