package acolyte.jdbc

import acolyte.jdbc.ParameterMetaData.ParameterDef

/** Execution information */
sealed trait Execution {
  /** Executed SQL */
  def sql: String

  /** Execution parameters */
  def parameters: List[ExecutedParameter]
}

/**
 * Information about the execution of a query.
 *
 * @param sql the SQL statement
 * @param parameter the parameters the query is executed with
 */
case class QueryExecution(
  sql: String, parameters: List[ExecutedParameter] = Nil
) extends Execution

case class UpdateExecution(
  sql: String, parameters: List[ExecutedParameter] = Nil
) extends Execution

/**
 * Statement extractor
 * @param p Statement pattern
 */
case class ExecutedStatement(val p: String) {
  val re = p.r

  def unapply(x: Execution): Option[(String, List[ExecutedParameter])] =
    re.findFirstIn(x.sql).map(_ ⇒ (x.sql → x.parameters))
}

/** Parameter used to executed a query or an update. */
sealed trait ExecutedParameter {
  /** The parameter value */
  def value: Any
}

/** Executed parameter companion */
object ExecutedParameter {
  def apply(v: Any): ExecutedParameter =
    new ExecutedParameter { val value = v }

  def unapply(p: ExecutedParameter): Option[Any] = Some(p.value)
}

/** Parameter along with its definition. */
case class DefinedParameter(
    value: Any, definition: ParameterDef
) extends ExecutedParameter {

  override lazy val toString = s"Param($value, ${definition.sqlTypeName})"
}
