package acolyte

import org.specs2.mutable.Specification

object CallableStatementSpec 
    extends Specification with StatementSpecification[CallableStatement] {

  "Callable statement specification" title

  def statement(c: Connection = defaultCon, s: String = "TEST", h: StatementHandler = defaultHandler.getStatementHandler) = new CallableStatement(c, s, h)

}
