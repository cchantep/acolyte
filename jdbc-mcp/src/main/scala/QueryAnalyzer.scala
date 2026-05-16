package acolyte.jdbc.mcp

import scala.util.Try

final case class Condition(field: String, operator: String, placeholder: String)

final case class QueryStructure(
    selectFields: List[String],
    whereConditions: List[Condition],
    parameterCount: Int)

object QueryAnalyzer {
  private val whereClauseRegex = """(?i)\bWHERE\b""".r

  private val conditionRegex =
    """([a-zA-Z_][a-zA-Z0-9_\.]*)\s*([<>=!]+|LIKE|IN|BETWEEN|IS)\s*(\?|'[^']*'|[0-9]+)""".r

  private val selectRegex = """(?i)\bSELECT\b\s+(.*?)\s+FROM""".r

  def analyzeQuery(sqlQuery: String): Try[QueryStructure] = Try {
    val normalized = sqlQuery.trim

    val selectFields = extractSelectFields(normalized)
    val (whereConditions, paramCount) = extractWhereConditions(normalized)

    QueryStructure(selectFields, whereConditions, paramCount)
  }

  private def extractSelectFields(query: String): List[String] =
    selectRegex.findFirstMatchIn(query) match {
      case Some(m) =>
        val fieldList = m.group(1)
        fieldList.split(",").map(_.trim).filter(_.nonEmpty).toList

      case None => List("*")
    }

  private def extractWhereConditions(query: String): (List[Condition], Int) = {
    val whereStart =
      whereClauseRegex.findFirstMatchIn(query).map(_.end).getOrElse(-1)

    if (whereStart == -1) {
      List.empty -> 0
    } else {
      query
        .substring(whereStart)
        .split("(?i)(?:ORDER\\s+BY|GROUP\\s+BY|LIMIT|;)")
        .headOption match {
        case Some(whereClause) => {
          val conditions = conditionRegex
            .findAllMatchIn(whereClause)
            .flatMap { m =>
              val field = m.group(1)
              val operator = m.group(2).toUpperCase
              val placeholder = m.group(3)

              if (placeholder == "?")
                List(Condition(field, operator, placeholder))
              else List()
            }
            .toList

          conditions -> conditions.length
        }

        case None =>
          List.empty -> 0
      }
    }
  }

  def identifyParameterPositions(whereClause: String): List[Int] = {
    def findPositions(startIdx: Int, acc: List[Int]): List[Int] = {
      val nextPos = whereClause.indexOf('?', startIdx)
      if (nextPos == -1) acc else findPositions(nextPos + 1, acc :+ nextPos)
    }

    findPositions(0, List.empty)
  }
}
