package usecase;

import java.util.List;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Date;

import acolyte.ConnectionHandler;
import acolyte.CompositeHandler;
import acolyte.StatementHandler;
import acolyte.UpdateResult;
import acolyte.QueryResult;

import acolyte.StatementHandler.Parameter;

import static acolyte.RowList.Column.defineCol;
import static acolyte.RowLists.rowList1;
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
                    public UpdateResult apply(String sql, 
                                              List<Parameter> parameter) {

                        if (sql.startsWith("DELETE ")) {
                            // Process deletion ...

                            return /* deleted = */new UpdateResult(2);
                        }

                        // ... Process ...

                        return /* count = */UpdateResult.One;
                    }
                }).
            withQueryHandler(new CompositeHandler.QueryHandler () {
                    public QueryResult apply(String sql, 
                                             List<Parameter> params) {

                        if (sql.startsWith("SELECT ")) {
                            return rowList1(String.class).asResult();
                        }

                        // ... EXEC that_proc (see previous withQueryDetection)

                        // Prepare list of 2 rows
                        // with 3 columns of types String, Float, Date
                        return rowList3(String.class, Float.class, Date.class).
                            withLabel(1, "String"). // Optional: set labels
                            withLabel(3, "Date"). 
                            append(row3("str", 1.2f, new Date(1l))).
                            append(row3("val", 2.34f, new Date(2l))).
                            asResult();
                    }
                });

        // Register prepared handler with expected ID 'my-handler-id'
        acolyte.Driver.register("my-handler-id", handler);

        // ... then connection is managed through |handler|
        return DriverManager.getConnection(jdbcUrl);
    } // end of useCase1

    /**
     * Use case #2
     */
    public static Connection useCase2() throws SQLException {
        final StatementHandler handler = new CompositeHandler().
            withQueryDetection("^SELECT ").
            withQueryHandler(new CompositeHandler.QueryHandler () {
                    public QueryResult apply(String sql, 
                                             List<Parameter> params) {

                        // Prepare list of 2 rows
                        // with 3 columns of types String, Float, Date
                        return rowList3(defineCol(String.class, "str"),
                                        defineCol(Float.class, "f"), 
                                        defineCol(Date.class, "date")).
                            append(row3("text", 2.3f, new Date(3l))).
                            append(row3("label", 4.56f, new Date(4l))).
                            asResult();
                    }
                });

        // Register prepared handler with expected ID 'my-handler-id'
        acolyte.Driver.register("my-handler-id", handler);

        // ... then connection is managed through |handler|
        return DriverManager.getConnection(jdbcUrl);
    } // end of useCase2
} // end of class JavaUseCases
