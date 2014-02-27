package acolyte

import acolyte.ParameterMetaData.ParameterDef

/** Execution information */
sealed trait Execution {
  /** Executed SQL */
  def sql: String

  /** Execution parameters */
  def parameters: List[ExecutedParameter]
}

case class QueryExecution(
  sql: String, parameters: List[ExecutedParameter] = Nil) extends Execution

case class UpdateExecution(
  sql: String, parameters: List[ExecutedParameter] = Nil) extends Execution

/** Statement extractor */
case class ExecutedStatement(
    /** Statement pattern */
    val p: String) {

  val re = p.r

  def unapply(x: Execution): Option[(String, List[ExecutedParameter])] =
    re.findFirstIn(x.sql).map(_ ⇒ (x.sql -> x.parameters))
}

sealed trait ExecutedParameter { def value: Any }

/** Executed parameter companion */
object ExecutedParameter {
  def apply(v: Any): ExecutedParameter =
    new ExecutedParameter { val value = v }

  def unapply(p: ExecutedParameter): Option[Any] = Some(p.value)
}

/** Parameter along with its definition. */
case class DefinedParameter(
    value: Any, definition: ParameterDef) extends ExecutedParameter {

  override lazy val toString = s"Param($value, ${definition.sqlTypeName})"
}

/** Value extractor for execution parameter. */
@deprecated(
  message = "Use [[acolyte.ExecutedParameter]] extractor", since = "1.0.15")
object ParameterVal {
  def unapply(p: ExecutedParameter): Option[Any] = ExecutedParameter.unapply(p)
}
