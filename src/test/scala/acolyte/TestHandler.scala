package acolyte

import java.sql.ResultSet

object EmptyConnectionHandler extends ConnectionHandler {
  def getGeneratedKeys(): ResultSet = AbstractResultSet.EMPTY
  def isQuery(sql: String): Boolean = false
  def whenSQLQuery(sql: String): ResultSet = AbstractResultSet.EMPTY
  def whenSQLUpdate(sql: String): Int = -1
}
