package acolyte

import java.util.{ List ⇒ JList }

import java.sql.ResultSet

import acolyte.StatementHandler.Parameter

package object test {
  type Params = JList[Parameter]

  object EmptyConnectionHandler extends ConnectionHandler {
    def getStatementHandler = EmptyStatementHandler
  }

  object EmptyStatementHandler extends StatementHandler {
    def getGeneratedKeys(): ResultSet = 
      RowLists.rowList1(classOf[String]).resultSet

    def isQuery(sql: String): Boolean = false
    def whenSQLQuery(sql: String, params: Params): ResultSet = 
      RowLists.rowList1(classOf[String]).resultSet

    def whenSQLUpdate(sql: String, params: Params): Int = -1
  }
}
