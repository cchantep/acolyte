package acolyte

import java.sql.{ Connection ⇒ SqlConnection, Date, DriverManager }

import acolyte.{ Driver ⇒ AcolyteDriver }
import Acolyte._ // import DSL

/**
 * Use cases for testing.
 *
 * @author Cedric Chantepie
 */
object ScalaUseCases {
  // Configure in anyway JDBC with following url,
  // declaring handler registered with 'my-handler-id' will be used.
  val jdbcUrl =
    "jdbc:acolyte:anything-you-want?handler=my-handler-id";

  /**
   * Use case #1
   */
  def useCase1(): SqlConnection = {
    // Prepare handler
    val handler: CompositeHandler = handleStatement.
      withQueryDetection("^SELECT "). // regex test from beginning
      withQueryDetection("EXEC that_proc"). // second detection regex
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
          AbstractResultSet.EMPTY;
        } else {
          // ... EXEC that_proc 
          // (see previous withQueryDetection)

          // Prepare list of 2 rows
          // with 3 columns of types String, Float, Date
          (rowList[Row3[String, Float, Date]].
            withLabels( // Optional: set labels
              1 -> "String",
              3 -> "Date")
              :+ row3("str", 1.2f, new Date(1l))
              :+ row3("val", 2.34f, new Date(2l))).
              resultSet // convert to JDBC ResultSet
        }
      })

    // Register prepared handler with expected ID 'my-handler-id'
    AcolyteDriver.register("my-handler-id", handler)

    // ... then connection is managed through |handler|
    DriverManager.getConnection(jdbcUrl)
  } // end of useCase1
} // end of class ScalaUseCases
