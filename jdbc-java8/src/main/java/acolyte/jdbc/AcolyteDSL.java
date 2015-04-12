package acolyte.jdbc;

import java.util.Properties;
import java.util.function.Function;

import static acolyte.jdbc.CompositeHandler.QueryHandler;

/**
 * Acolyte DSL for JDBC.
 */
public final class AcolyteDSL {
    /**
     * Creates a connection, whose statement will be passed to given handler.
     *
     * @param h statement handler
     * @return a new Acolyte connection
     *
     * <pre>
     * {@code
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
     * @param h statement handler
     * @param p connection properties
     * @return a new Acolyte connection
     *
     * <pre>
     * {@code
     * // With connection property to fallback untyped null
     * connection(handler, properties); //e.g. "acolyte.parameter.untypedNull"
     * }
     * </pre>
     */
    public static Connection connection(AbstractCompositeHandler<?> h, Properties p) { return Driver.connection(h, p); }

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
     * @param h connection handler
     * @param p connection properties
     * @return a new Acolyte connection
     *
     * <pre>
     * {@code
     * // With connection property to fallback untyped null
     * connection(handler, properties); //e.g. "acolyte.parameter.untypedNull
     * }
     * </pre>
     */
    public static Connection connection(ConnectionHandler h, Properties p) {
        return Driver.connection(h, p);
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
     *   else otherResult
     * }));
     * }
     * </pre>
     */
    public static Java8CompositeHandler handleQuery(QueryHandler h) {
        return AcolyteDSL.handleStatement.
            withQueryDetection(".*").withQueryHandler(h);
    }

    /**
     * Executes |f| using a connection accepting only queries,
     * and answering with |result| to any query.
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.AcolyteDSL.withQueryResult
     *
     * val str: String = withQueryResult(queryRes) { con => "str" }
     * }
     * </pre>
     */
    public static <A> A withQueryResult(QueryResult res,
                                        Function<java.sql.Connection, A> f) {

        return f.apply(connection(handleQuery((x, y) -> res)));
    }

    /**
     * Returns an update result with row |count| and generated |keys|.
     * @param count Updated (row) count
     * @param keys Generated keys
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.AcolyteDSL.updateResult
     * import acolyte.jdbc.RowLists
     *
     * updateResult(2, RowLists.stringList("a", "b"))
     * }
     * </pre>
     */
    public static UpdateResult updateResult(int count, RowList<?> keys) {
        return new UpdateResult(count).withGeneratedKeys(keys);
    }
}
