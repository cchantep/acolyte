package acolyte

import acolyte.ParameterMetaData.ParameterDef

/** Execution information */
@deprecated("Use [[acolyte.jdbc.Execution]]", "1.0.21")
sealed trait Execution {
  /** Executed SQL */
  def sql: String

  /** Execution parameters */
  def parameters: List[ExecutedParameter]
}

@deprecated("Use [[acolyte.jdbc.QueryExecution]]", "1.0.21")
case class QueryExecution(
  sql: String, parameters: List[ExecutedParameter] = Nil) extends Execution

@deprecated("Use [[acolyte.jdbc.UpdateExecution]]", "1.0.21")
case class UpdateExecution(
  sql: String, parameters: List[ExecutedParameter] = Nil) extends Execution

/** Statement extractor */
@deprecated("Use [[acolyte.jdbc.ExecutedStatement]]", "1.0.21")
case class ExecutedStatement(
    /** Statement pattern */
    val p: String) {

  val re = p.r

  def unapply(x: Execution): Option[(String, List[ExecutedParameter])] =
    re.findFirstIn(x.sql).map(_ ⇒ (x.sql -> x.parameters))
}

@deprecated("Use [[acolyte.jdbc.ExecutedParameter]]", "1.0.21")
sealed trait ExecutedParameter { def value: Any }

/** Executed parameter companion */
@deprecated("Use [[acolyte.jdbc.ExecutedParameter]]", "1.0.21")
object ExecutedParameter {
  def apply(v: Any): ExecutedParameter =
    new ExecutedParameter { val value = v }

  def unapply(p: ExecutedParameter): Option[Any] = Some(p.value)
}

/** Parameter along with its definition. */
@deprecated("Use [[acolyte.jdbc.DefinedParameter]]", "1.0.21")
case class DefinedParameter(
    value: Any, definition: ParameterDef) extends ExecutedParameter {

  override lazy val toString = s"Param($value, ${definition.sqlTypeName})"
}
