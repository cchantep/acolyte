package acolyte

import java.util.{ List ⇒ JList }

import java.sql.ResultSet

import org.apache.commons.lang3.tuple.ImmutablePair

import acolyte.ParameterMetaData.Parameter

package object test {
  type Params = JList[ImmutablePair[Parameter, Object]]

  object EmptyConnectionHandler extends ConnectionHandler {
    def getStatementHandler = EmptyStatementHandler
  }

  object EmptyStatementHandler extends StatementHandler {
    def getGeneratedKeys(): ResultSet = acolyte.AbstractResultSet.EMPTY
    def isQuery(sql: String): Boolean = false
    def whenSQLQuery(sql: String, params: Params): ResultSet = AbstractResultSet.EMPTY
    def whenSQLUpdate(sql: String, params: Params): Int = -1
  }
}
