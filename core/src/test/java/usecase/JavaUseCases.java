package usecase;

import java.util.List;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Date;

import acolyte.AbstractResultSet;
import acolyte.ConnectionHandler;
import acolyte.CompositeHandler;
import acolyte.StatementHandler;

import acolyte.StatementHandler.Parameter;

import static acolyte.RowLists.rowList3;
import static acolyte.Rows.row3;

/**
 * Use cases for testing.
 * 
 * @author Cedric Chantepie
 */
public final class JavaUseCases {
    // Configure in anyway JDBC with following url,
    // declaring handler registered with 'my-handler-id' will be used.
    protected static final String jdbcUrl = 
        "jdbc:acolyte:anything-you-want?handler=my-handler-id";

    /**
     * Use case #1
     */
    public static Connection useCase1() throws SQLException {
        // Prepare handler
        final StatementHandler handler = new CompositeHandler().
            withQueryDetection("^SELECT "). // regex test from beginning
            withQueryDetection("EXEC that_proc"). // second detection regex
            withUpdateHandler(new CompositeHandler.UpdateHandler() {
                    // Handle execution of update statement (not query)
                    public int apply(String sql, List<Parameter> parameter) {
                        if (sql.startsWith("DELETE ")) {
                            // Process deletion ...

                            return /* deleted = */2;
                        }

                        // ... Process ...

                        return /* count = */1;
                    }
                }).
            withQueryHandler(new CompositeHandler.QueryHandler () {
                    public ResultSet apply(String sql, List<Parameter> parameters) {
                        if (sql.startsWith("SELECT ")) {
                            return AbstractResultSet.EMPTY;
                        }

                        // ... EXEC that_proc (see previous withQueryDetection)

                        // Prepare list of 2 rows
                        // with 3 columns of types String, Float, Date
                        return rowList3(String.class, Float.class, Date.class).
                            withLabel(1, "String"). // Optional: set labels
                            withLabel(3, "Date"). 
                            append(row3("str", 1.2f, new Date(1l))).
                            append(row3("val", 2.34f, new Date(2l))).
                            resultSet(); // convert to JDBC ResultSet
                    }
                });

        // Register prepared handler with expected ID 'my-handler-id'
        acolyte.Driver.register("my-handler-id", handler);

        // ... then connection is managed through |handler|
        return DriverManager.getConnection(jdbcUrl);
    } // end of useCase1
} // end of class JavaUseCases
