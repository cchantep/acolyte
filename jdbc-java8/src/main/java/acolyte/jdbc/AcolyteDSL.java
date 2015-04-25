package acolyte.jdbc;

import java.util.Properties;
import java.util.function.Function;

import static acolyte.jdbc.Driver.Property;
import static acolyte.jdbc.CompositeHandler.QueryHandler;

import acolyte.jdbc.Java8CompositeHandler.ScalarQueryHandler;

/**
 * Acolyte DSL for JDBC.
 */
public final class AcolyteDSL {
    /**
     * Creates a configuration property for the Acolyte driver.
     *
     * @param name the name of the configuration property
     * @param value the configuration value
     * @return the created property
     */
    public static Property prop(String name, String value) {
        return new Property(name, value);
    }
    
    /**
     * Creates a connection, whose statement will be passed to given handler.
     *
     * @param h statement handler
     * @return a new Acolyte connection
     *
     * <pre>
     * {@code
     * import static acolyte.jdbc.AcolyteDSL.connection;
     *
     * connection(handler); // without connection properties
     * }}}
     * }
     * </pre>
     */
    public static Connection connection(AbstractCompositeHandler<?> h) {
        return Driver.connection(h);
    }
    
    /**
     * Creates a connection, whose statement will be passed to given handler.
     *
     * @param h the statement handler
     * @param ps the configuration properties for the new connection
     * @return a new Acolyte connection
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.AcolyteDSL;
     * import static acolyte.jdbc.AcolyteDSL.connection;
     * import static acolyte.jdbc.AcolyteDSL.prop;
     *
     * // With connection property to fallback untyped null
     * connection(handler, prop("acolyte.parameter.untypedNull", "true"));
     * }
     * </pre>
     */
    public static Connection connection(AbstractCompositeHandler<?> h, Property... ps) { return Driver.connection(h, ps); }

    /**
     * Creates a connection, managed with given handler.
     *
     * @param h connection handler
     * @return a new Acolyte connection
     *
     * <pre>
     * {@code
     * connection(handler); // without connection properties
     * }
     * </pre>
     */
    public static Connection connection(ConnectionHandler h) {
        return Driver.connection(h);
    }    
    
    /**
     * Creates a connection, managed with given handler.
     *
     * @param h the connection handler
     * @param ps the connection properties
     * @return a new Acolyte connection
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.AcolyteDSL;
     * import static acolyte.jdbc.AcolyteDSL.connection;
     * import static acolyte.jdbc.AcolyteDSL.prop;
     *
     * // With connection property to fallback untyped null
     * connection(handler, prop("acolyte.parameter.untypedNull", "true"));
     * }
     * </pre>
     */
    public static Connection connection(ConnectionHandler h, Property... ps) {
        return Driver.connection(h, ps);
    }

    /**
     * Creates an empty handler.
     *
     * <pre>
     * {@code
     * import static acolyte.jdbc.AcolyteDSL.connection;
     * import static acolyte.jdbc.AcolyteDSL.handleStatement;
     *
     * connection(handleStatement);
     * }
     * </pre>
     */
    public static final Java8CompositeHandler handleStatement =
        new Java8CompositeHandler(null, null, null);

    /**
     * Creates a new handler detecting all statements as queries
     * (like `handleStatement.withQueryDetection(".*").withQueryHandler(h)`).
     *
     * @param h the new query handler
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.StatementHandler.Parameter;
     * import static acolyte.jdbc.AcolyteDSL.connection;
     * import static acolyte.jdbc.AcolyteDSL.handleQuery;
     *
     * connection(handleQuery((String sql, List<Parameter> ps) -> {
     *   if (sql == "SELECT * FROM Test WHERE id = ?") return aQueryResult;
     *   else return otherResult;
     * }));
     * }
     * </pre>
     */
    public static Java8CompositeHandler handleQuery(QueryHandler h) {
        return AcolyteDSL.handleStatement.
            withQueryDetection(".*").withQueryHandler(h);
    }

    /**
     * Creates a new handler detecting all statements as queries
     * (like `handleStatement.withQueryDetection(".*").withQueryHandler(h)`).
     *
     * @param h the new query handler
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.StatementHandler.Parameter;
     * import static acolyte.jdbc.AcolyteDSL.connection;
     * import static acolyte.jdbc.AcolyteDSL.handleQuery;
     *
     * connection(handleQuery((String sql, List<Parameter> ps) -> {
     *   if (sql == "SELECT * FROM Test WHERE id = ?") return "Foo";
     *   else return "Bar";
     * }));
     * }
     * </pre>
     */
    public static Java8CompositeHandler handleQuery2(ScalarQueryHandler h) {
        return AcolyteDSL.handleStatement.
            withQueryDetection(".*").withQueryHandler2(h);
    }

    /**
     * Executes |f| using a connection accepting only queries,
     * and answering with |result| to any query.
     *
     * <pre>
     * {@code
     * import static acolyte.jdbc.AcolyteDSL.withQueryResult;
     *
     * String str = withQueryResult(queryRes, con -> "str");
     * }
     * </pre>
     */
    public static <A> A withQueryResult(QueryResult res,
                                        Function<java.sql.Connection, A> f) {

        return f.apply(connection(handleQuery((x, y) -> res)));
    }

    /**
     * Returns an update result with row |count| and generated |keys|.
     * @param count the updated (row) count
     * @param keys the generated keys
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.AcolyteDSL.updateResult;
     * import acolyte.jdbc.RowLists;
     *
     * updateResult(2, RowLists.stringList("a", "b"));
     * }
     * </pre>
     */
    public static UpdateResult updateResult(int count, RowList<?> keys) {
        return new UpdateResult(count).withGeneratedKeys(keys);
    }
}
