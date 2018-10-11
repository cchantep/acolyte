package acolyte.jdbc.play

import java.sql.SQLException

import acolyte.jdbc.{ AcolyteDSL, RowLists }
import acolyte.jdbc.UpdateExecution
import acolyte.jdbc.Implicits._

case object PlayJdbcUseCases {

  val useCase1: PlayJdbcContext = PlayJdbcDSL.withPlayDBResult("foo")

  def useCase2(onUpdate: ⇒ Unit): PlayJdbcContext = new PlayJdbcContext(
    AcolyteDSL.handleStatement.withUpdateHandler {
      case UpdateExecution("insert into foo values (1, 'foo value')", Nil) ⇒
        AcolyteDSL.updateResult(1, RowLists.longList.append(1L))
      case UpdateExecution("insert into bar values (1, 'bar value')", Nil) ⇒
        AcolyteDSL.updateResult(2, RowLists.longList.append(1L))
      case u ⇒ throw new SQLException(s"Unexpected update: $u")
    },
    AcolyteDSL.handleTransaction(whenCommit = { _ ⇒ onUpdate }))

  def useCase3(onUpdate: ⇒ Unit): PlayJdbcContext = new PlayJdbcContext(
    AcolyteDSL.handleStatement.withUpdateHandler {
      case UpdateExecution("insert into foo values (1, 'foo value')", Nil) ⇒
        AcolyteDSL.updateResult(1, RowLists.longList.append(1L))
      case UpdateExecution("insert into bar values (1, 'bar value')", Nil) ⇒
        throw new SQLException("Simulating on error.")
      case u ⇒ throw new SQLException(s"Unexpected update: $u")
    },
    AcolyteDSL.handleTransaction(whenRollback = { _ ⇒ onUpdate }))

}
