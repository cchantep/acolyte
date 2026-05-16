package acolyte.jdbc.mcp

import java.math.BigDecimal

import java.sql.{ Connection, Date, PreparedStatement, Time, Timestamp, Types }

import scala.util.Try

final case class QueryColumn(
    index: Int,
    name: String,
    jdbcType: Int,
    jdbcTypeName: String,
    javaClassName: String)

final case class QueryExecution(
    columns: List[QueryColumn],
    rows: List[List[Any]],
    rowCount: Int)

object QueryExecutor {

  def executeQuery(
      connection: Connection,
      query: String,
      parameterSets: List[List[Any]]
    ): Try[List[QueryExecution]] = Try {
    parameterSets.map { params =>
      val stmt = connection.prepareStatement(query)

      try {
        setParameters(stmt, params)

        val rs = stmt.executeQuery()

        try {
          val metadata = rs.getMetaData
          val columnCount = metadata.getColumnCount
          val columns = (1 to columnCount).map { idx =>
            QueryColumn(
              index = idx,
              name = metadata.getColumnLabel(idx),
              jdbcType = metadata.getColumnType(idx),
              jdbcTypeName = metadata.getColumnTypeName(idx),
              javaClassName = metadata.getColumnClassName(idx)
            )
          }.toList

          val rows = Iterator
            .continually(rs.next())
            .takeWhile(identity)
            .map { _ => (1 to columnCount).map(rs.getObject).toList }
            .toList

          QueryExecution(columns = columns, rows = rows, rowCount = rows.length)
        } finally {
          rs.close()
        }
      } finally {
        stmt.close()
      }
    }
  }

  private def setParameters(
      stmt: PreparedStatement,
      params: List[Any]
    ): Unit = {
    params.zipWithIndex.foreach {
      case (param, idx) =>
        val position = idx + 1

        param match {
          case null            => stmt.setNull(position, Types.NULL)
          case s: String       => stmt.setString(position, s)
          case i: Int          => stmt.setInt(position, i)
          case l: Long         => stmt.setLong(position, l)
          case d: Double       => stmt.setDouble(position, d)
          case f: Float        => stmt.setFloat(position, f)
          case b: Boolean      => stmt.setBoolean(position, b)
          case bd: BigDecimal  => stmt.setBigDecimal(position, bd)
          case ba: Array[Byte] => stmt.setBytes(position, ba)
          case d: Date         => stmt.setDate(position, d)
          case t: Time         => stmt.setTime(position, t)
          case ts: Timestamp   => stmt.setTimestamp(position, ts)
          case obj             => stmt.setObject(position, obj)
        }
    }
  }
}
