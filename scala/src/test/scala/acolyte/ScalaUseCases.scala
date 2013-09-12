package acolyte

import java.sql.{ Connection ⇒ SqlConnection, Date, DriverManager }

import acolyte.{ Driver ⇒ AcolyteDriver }
import acolyte.RowLists.{ rowList1, rowList3 }
import acolyte.Rows.{ row1, row3 }
import Acolyte._ // import DSL

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
      "jdbc:acolyte:anything-you-want?handler=handler1";

    // Prepare handler
    val handler: CompositeHandler = handleStatement.
      withQueryDetection(
        "^SELECT ", // regex test from beginning
        "EXEC that_proc"). // second detection regex
      withUpdateHandler({ e: Execution ⇒
        if (e.sql.startsWith("DELETE ")) {
          // Process deletion ...
          /* deleted = */ 2;
        } else {
          // ... Process ...
          /* count = */ 1;
        }
      }).withQueryHandler({ e: Execution ⇒
        if (e.sql.startsWith("SELECT ")) {
          RowLists.rowList1(classOf[String]).asResult
        } else {
          // ... EXEC that_proc 
          // (see previous withQueryDetection)

          // Prepare list of 2 rows
          // with 3 columns of types String, Float, Date
          val rows: RowList3[String, Float, Date] =
            rowList3(classOf[String], classOf[Float], classOf[Date]).
              withLabels( // Optional: set labels
                1 -> "String",
                3 -> "Date") :+
                row3("str", 1.2f, new Date(1l)) :+
                row3("val", 2.34f, new Date(2l))

          rows.asResult
        }
      })

    // Register prepared handler with expected ID 'handler1'
    AcolyteDriver.register("handler1", handler)

    // ... then connection is managed through |handler|
    DriverManager.getConnection(jdbcUrl)
  } // end of useCase1

  /**
   * Use case #2 - Columns definition
   */
  def useCase2: SqlConnection = {
    val jdbcUrl = "jdbc:acolyte:anything-you-want?handler=handler2";

    val handler: CompositeHandler = handleStatement.
      withQueryDetection("^SELECT ").
      withQueryHandler({ e: Execution ⇒
        rowList3(classOf[String] -> "str",
          classOf[Float] -> "f",
          classOf[Date] -> "date").
          append(row3("text", 2.3f, new Date(3l))).
          append(row3("label", 4.56f, new Date(4l))).
          asResult()
      })

    // Register prepared handler with expected ID 'handler2'
    acolyte.Driver.register("handler2", handler)

    // ... then connection is managed through |handler|
    return DriverManager.getConnection(jdbcUrl);
  } // end of useCase2

  /**
   * Use case #3 - Pattern matching
   */
  def useCase3: SqlConnection = {
    val jdbcUrl = "jdbc:acolyte:anything-you-want?handler=handler3";

    val handler: CompositeHandler = handleStatement.
      withQueryDetection("^SELECT ").
      withQueryHandler({ e: Execution ⇒
        e match {
          case Execution(s, ParameterVal("id") :: Nil) ⇒
            (rowList1(classOf[String]) :+ row1("useCase_3a")).asResult

          case Execution(s,
            DefinedParameter("id", _) :: DefinedParameter(3, _) :: Nil) ⇒
            (rowList3(classOf[String], classOf[Int], classOf[Long]) :+ row3(
              "useCase_3str", 2, 3l)).asResult

          case _ ⇒ sys.error("Unsupported")
        }
      })

    // Register prepared handler with expected ID 'handler3'
    acolyte.Driver.register("handler3", handler)

    // ... then connection is managed through |handler|
    return DriverManager.getConnection(jdbcUrl);
  } // end of useCase3

  /**
   * Use case #4 - Row list convinience constructor
   * and query handler convertion.
   */
  def useCase4: SqlConnection = connection {
    handleStatement.
      withQueryDetection("^SELECT ").
      withQueryHandler({ e: Execution ⇒ RowLists.booleanList :+ true })

  } // end of useCase4
} // end of class ScalaUseCases
