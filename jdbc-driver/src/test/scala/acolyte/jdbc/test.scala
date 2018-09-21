package acolyte.jdbc

import java.util.{ List ⇒ JList }

import acolyte.jdbc.StatementHandler.Parameter

package object test {
  type Params = JList[Parameter]

  object EmptyConnectionHandler extends ConnectionHandler {
    def getStatementHandler = EmptyStatementHandler
    def getResourceHandler = new ResourceHandler.Default()

    def withResourceHandler(h: ResourceHandler): ConnectionHandler =
      new ConnectionHandler.Default(EmptyStatementHandler, h)
  }

  object EmptyStatementHandler extends StatementHandler {
    def isQuery(sql: String): Boolean = false
    def whenSQLQuery(sql: String, params: Params) =
      RowLists.rowList1(classOf[String]).asResult

    def whenSQLUpdate(sql: String, params: Params) = UpdateResult.Nothing
  }
}
