package acolyte.jdbc.play

import java.sql.Connection

import play.api.db.TransactionIsolationLevel

private[play] trait AcolyteDatabaseCompat { db: AcolyteDatabase ⇒
  def withTransaction[A](isolationLevel: TransactionIsolationLevel)(block: Connection ⇒ A): A = {
    lazy val con = getConnection(false)

    try {
      con.setTransactionIsolation(isolationLevel.id)

      block(con)
    } catch {
      case e: Throwable ⇒ sys.error(s"error: $e")
    } finally {
      con.close()
    }
  }
}
