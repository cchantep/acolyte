package acolyte.jdbc

import java.sql.{ Connection => SqlConnection, Date, DriverManager }

import acolyte.jdbc.{ Driver => AcolyteDriver }
import acolyte.jdbc.AcolyteDSL.{ connection, handleQuery, handleStatement }
import acolyte.jdbc.Implicits._
import acolyte.jdbc.RowLists.{ intList, rowList3, stringList }

/**
 * Use cases for testing.
 *
 * @author Cedric Chantepie
 */
object ScalaUseCases {

  /**
   * Use case #1
   */
  def useCase1: SqlConnection = {
    // Configure in anyway JDBC with following url,
    // declaring handler registered with 'handler1' will be used.
    val jdbcUrl =
      "jdbc:acolyte:anything-you-want?handler=handler1"

    // Prepare handler
    val handler: ScalaCompositeHandler = handleStatement
      .withQueryDetection(
        "^SELECT ", // regex test from beginning
        "EXEC that_proc"
      )
      . // second detection regex
      withUpdateHandler { x =>
        val Executed = ExecutedStatement("^DELETE ")

        x match {
          case Executed((_ /*sql*/, _ /*ps*/ )) =>
            /* Process deletion ... deleted = */
            2

          case _ => /* ... Process ... count = */ 1
        }
      } withQueryHandler { (e: QueryExecution) =>
      if (e.sql.startsWith("SELECT ")) {
        RowLists.rowList1(classOf[String]).asResult
      } else {
        // ... EXEC that_proc
        // (see previous withQueryDetection)

        // Prepare list of 2 rows
        // with 3 columns of types String, Float, Date
        val rows: ScalaRowList3[String, Float, Date] =
          rowList3(classOf[String], classOf[Float], classOf[Date])
            .withLabels( // Optional: set labels
              1 -> "String",
              3 -> "Date"
            ).append("str", 1.2F, new Date(1L)).append("val", 2.34F, null)

        rows.asResult
      }
    }

    // Register prepared handler with expected ID 'handler1'
    AcolyteDriver.register("handler1", handler)

    // ... then connection is managed through |handler|
    DriverManager.getConnection(jdbcUrl)
  } // end of useCase1

  /**
   * Use case #2 - Columns definition
   */
  def useCase2: SqlConnection = {
    val jdbcUrl = "jdbc:acolyte:anything-you-want?handler=handler2"

    val handler: ScalaCompositeHandler = handleStatement.withQueryDetection(
      "^SELECT "
    ).withQueryHandler { _ =>
      rowList3(
        classOf[String] -> "str",
        classOf[Float] -> "f",
        classOf[Date] -> "date"
      ).append("text", 2.3F, new Date(3L)).append("label", 4.56F, new Date(4L))
    }

    // Register prepared handler with expected ID 'handler2'
    acolyte.jdbc.Driver.register("handler2", handler)

    // ... then connection is managed through |handler|
    return DriverManager.getConnection(jdbcUrl)
  } // end of useCase2

  /**
   * Use case #3 - Pattern matching
   */
  def useCase3: SqlConnection = {
    val jdbcUrl = "jdbc:acolyte:anything-you-want?handler=handler3"

    val handler: ScalaCompositeHandler =
      handleStatement.withQueryDetection("^SELECT ") withQueryHandler {
        case QueryExecution(_, ExecutedParameter("id") :: Nil) =>
          (stringList :+ "useCase_3a").asResult

        case QueryExecution(
              _,
              DefinedParameter("id", _) :: DefinedParameter(3, _) :: Nil
            ) =>
          (rowList3(
            classOf[String],
            classOf[Int],
            classOf[Long]
          ).append("useCase_3str", 2, 3L)).asResult

        case _ => QueryResult.Nil withWarning "Now you're warned"
      }

    // Register prepared handler with expected ID 'handler3'
    acolyte.jdbc.Driver.register("handler3", handler)

    // ... then connection is managed through |handler|
    return DriverManager.getConnection(jdbcUrl)
  } // end of useCase3

  /**
   * Use case #4 - Connection with only query handler.
   */
  def useCase4: SqlConnection = connection { handleQuery { _ => true } }

  /**
   * Use case #5 - Generated keys
   */
  def useCase5: SqlConnection = connection {
    handleStatement withUpdateHandler { _ =>
      UpdateResult.One.withGeneratedKeys(intList :+ 100)
    }
  }
} // end of class ScalaUseCases
