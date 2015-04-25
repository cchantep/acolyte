package acolyte.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Date;

import static acolyte.jdbc.RowLists.stringList;
import static acolyte.jdbc.RowLists.rowList3;
import static acolyte.jdbc.RowLists.intList;
import static acolyte.jdbc.RowList.Column;

import static acolyte.jdbc.AcolyteDSL.handleStatement;
import static acolyte.jdbc.AcolyteDSL.handleQuery2;
import static acolyte.jdbc.AcolyteDSL.handleQuery;
import static acolyte.jdbc.AcolyteDSL.connection;

/**
 * Use cases for testing.
 *
 * @author Cedric Chantepie
 */
final class Java8UseCases {
    /**
     * Use case #1
     */
    public static java.sql.Connection useCase1() throws SQLException {
        // Configure in anyway JDBC with following url,
        // declaring handler registered with 'handler1' will be used.
        final String jdbcUrl =
            "jdbc:acolyte:anything-you-want?handler=handler1";

        // Prepare handler
        final Java8CompositeHandler handler = handleStatement.
            withQueryDetection("^SELECT ", // regex test from beginning
                               "EXEC that_proc"). // second detection regex
            withUpdateHandler1((sql, ps) -> {
                    if (sql.startsWith("DELETE ")) {
                        /* Process deletion ... deleted = */ return 2;
                    } else {
                        /* ... Process ... count = */ return 1;
                    }
                }).
            withQueryHandler((sql, ps) -> {
                    if (sql.startsWith("SELECT ")) {
                        return RowLists.rowList1(String.class).asResult();
                    } else {
                        // ... EXEC that_proc 
                        // (see previous withQueryDetection)

                        // Prepare list of 2 rows
                        // with 3 columns of types String, Float, Date
                        RowList3.Impl<String, Float, Date> rows =
                        rowList3(String.class, Float.class, Date.class).
                        // Optional: set labels
                        withLabel(1, "String").withLabel(3, "Date").
                        append("str", 1.2F, new Date(1l)).
                        append("val", 2.34F, null);

                        return rows.asResult();
                    }
                });

        // Register prepared handler with expected ID 'handler1'
        acolyte.jdbc.Driver.register("handler1", handler);

        // ... then connection is managed through |handler|
        return DriverManager.getConnection(jdbcUrl);
    } // end of useCase1

    /**
     * Use case #2 - Columns definition
     */
    public static java.sql.Connection useCase2() throws SQLException {
        final String jdbcUrl =
            "jdbc:acolyte:anything-you-want?handler=handler2";

        final Java8CompositeHandler handler = handleStatement.
            withQueryDetection("^SELECT ").
            withQueryHandler1((sql, ps) ->
                              rowList3(Column(String.class, "str"),
                                       Column(Float.class, "f"),
                                       Column(Date.class, "date")).
                              append("text", 2.3f, new Date(3l)).
                              append("label", 4.56f, new Date(4l)));

        // Register prepared handler with expected ID 'handler2'
        acolyte.jdbc.Driver.register("handler2", handler);

        // ... then connection is managed through |handler|
        return DriverManager.getConnection(jdbcUrl);
    } // end of useCase2

    /**
     * Use case #3 - Pattern matching
     */
    public static java.sql.Connection useCase3() throws SQLException {
        final String jdbcUrl =
            "jdbc:acolyte:anything-you-want?handler=handler3";

        final Java8CompositeHandler handler =
            handleStatement.withQueryDetection("^SELECT ").
            withQueryHandler((sql, ps) -> {
                    if (ps.size() == 1 && "id".equals(ps.get(0).right)) {
                        return stringList().append("useCase_3a").asResult();
                    } else if (ps.size() == 2 &&
                               "id".equals(ps.get(0).right) &&
                               (new Integer(3)).equals(ps.get(1).right)) {
                        return rowList3(String.class, Integer.class, Long.class).append("useCase_3str", 2, 3l).asResult();
                        
                    }

                    // otherwise...
                    return QueryResult.Nil.withWarning("Now you're warned");
                });

        // Register prepared handler with expected ID 'handler3'
        acolyte.jdbc.Driver.register("handler3", handler);
    
        // ... then connection is managed through |handler|
        return DriverManager.getConnection(jdbcUrl);
    } // end of useCase3

    /**
     * Use case #4 - Connection with only query handler.
     */
    public static java.sql.Connection useCase4() {
        return connection(handleQuery2((sql, ps) -> true));
    }

    /**
     * Use case #5 - Generated keys
     */
    public static java.sql.Connection useCase5() {
        return connection(handleStatement.withUpdateHandler((sql, ps) -> UpdateResult.One.withGeneratedKeys(intList().append(100))));
    }
} // end of class Java8UseCases
