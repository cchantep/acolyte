package acolyte.jdbc;

import java.util.List;
import java.util.regex.Pattern;

import java.util.function.Function;

import java.sql.SQLException;

import acolyte.jdbc.StatementHandler.Parameter;

public final class Java8CompositeHandler extends AbstractCompositeHandler {
    Java8CompositeHandler(Pattern[] qd, QueryHandler qh, UpdateHandler uh) {
        super(qd, qh, uh);
    }

    /**
     * Returns handler that detects statement matching given pattern(s)
     * as query.
     *
     * @param pattern the new pattern for query detection
     *
     * <pre>
     * {@code
     * import static acolyte.jdbc.AcolyteDSL.handleStatement;
     *
     * // Created handle will detect as query statements
     * // either starting with 'SELECT ' or containing 'EXEC proc'.
     * handleStatement.withQueryDetection("^SELECT ", "EXEC proc");
     * }
     * </pre>
     */
    public Java8CompositeHandler withQueryDetection(String... pattern) {
        final Pattern[] ps = new Pattern[pattern.length];

        for (int i = 0; i < ps.length; i++) {
            ps[i] = Pattern.compile(pattern[i]);
        }
        
        return withQueryDetection(ps);
    }

    /**
     * {@inheritDoc}
     */
    public Java8CompositeHandler withQueryDetection(Pattern... pattern) {
        return new Java8CompositeHandler(pattern, this.queryHandler, this.updateHandler);
    }

    /**
     * Returns handler that delegates query execution to |h| function.
     * Given function will be used only if executed statement is detected
     * as a query by withQueryDetection.
     *
     * @param h the new query handler
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.StatementHandler.Parameter;
     * import static acolyte.jdbc.AcolyteDSL.handleStatement;
     *
     * handleStatement.withQueryHandler((String sql, List<Parameter> ps) -> {
     *   if (sql == "SELECT * FROM Test WHERE id = ?") return aQueryResult;
     *   else return otherResult;
     * });
     * }
     * </pre>
     */
    public Java8CompositeHandler withQueryHandler(QueryHandler h) {
        return new Java8CompositeHandler(this.queryDetection, h, this.updateHandler);
    }

    /** A query handler whose result is a list of row. */
    public static interface RowListQueryHandler {
        public RowList apply(String sql, List<Parameter> parameters)
            throws SQLException;
        
    }

    /**
     * Returns handler that delegates query execution to |h| function.
     * Given function will be used only if executed statement is detected
     * as a query by withQueryDetection.
     *
     * @param h the new query handler
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.StatementHandler.Parameter;
     * import static acolyte.jdbc.AcolyteDSL.handleStatement;
     *
     * handleStatement.withQueryHandler1((String sql, List<Parameter> ps) -> {
     *   if (sql == "SELECT * FROM Test WHERE id = ?") return aRowList;
     *   else return anotherRowList;
     * });
     * }
     * </pre>
     */
    public Java8CompositeHandler withQueryHandler1(RowListQueryHandler h) {
        final QueryHandler qh = new QueryHandler() {
                public QueryResult apply(String sql, List<Parameter> ps)
                    throws SQLException {
                    return h.apply(sql, ps).asResult();
                }
            };

        return withQueryHandler(qh);
    }

    /** A query handler whose result is a single row with a single value. */
    public static interface ScalarQueryHandler<T> {
        public <T> T apply(String sql, List<Parameter> parameters)
            throws SQLException;
    }

    /**
     * Returns handler that delegates query execution to |h| function.
     * Given function will be used only if executed statement is detected
     * as a query by withQueryDetection.
     *
     * @param h the new query handler
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.StatementHandler.Parameter;
     * import static acolyte.jdbc.AcolyteDSL.handleStatement;
     *
     * handleStatement.withQueryHandler2((String sql, List<Parameter> ps) -> {
     *   if (sql == "SELECT * FROM Test WHERE id = ?") return "Foo";
     *   else return "Bar";
     * });
     * }
     * </pre>
     */
    public <T> Java8CompositeHandler withQueryHandler2(ScalarQueryHandler<T> h) {
        final QueryHandler qh = new QueryHandler() {
                public QueryResult apply(String sql, List<Parameter> ps)
                    throws SQLException {
                    return RowLists.scalar(h.apply(sql, ps)).asResult();
                }
            };

        return withQueryHandler(qh);
    }    

    /**
     * Returns handler that delegates update execution to |h| function.
     * Given function will be used only if executed statement is not detected
     * as a query by withQueryDetection.
     *
     * @param h the new update handler
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.UpdateResult;
     * import acolyte.jdbc.StatementHandler.Parameter;
     * import static acolyte.jdbc.AcolyteDSL.handleStatement;
     *
     * handleStatement.withUpdateHandler((String sql, List<Parameter> ps) -> {
     *   if (sql == "INSERT INTO Country (code, name) VALUES (?, ?)") {
     *     return new UpdateResult(1); // update count
     *   } else return otherResult;
     * });
     * }
     * </pre>
     */
    public Java8CompositeHandler withUpdateHandler(UpdateHandler h) {
        return new Java8CompositeHandler(this.queryDetection, this.queryHandler, h);
    }

    /** 
     * An update handler which return the count of successfully updated rows. 
     */
    static interface CountUpdateHandler {
        public int apply(String sql, List<Parameter> parameters);
    }

    /**
     * Returns handler that delegates update execution to |h| function.
     * Given function will be used only if executed statement is not detected
     * as a query by withQueryDetection.
     *
     * @param h the new update handler
     *
     * <pre>
     * {@code
     * import acolyte.jdbc.UpdateResult;
     * import acolyte.jdbc.StatementHandler.Parameter;
     * import static acolyte.jdbc.AcolyteDSL.handleStatement;
     *
     * handleStatement.withUpdateHandler1((String sql, List<Parameter> ps) -> {
     *   if (sql == "INSERT INTO Country (code, name) VALUES (?, ?)") {
     *     return 1; // update count
     *   } else return 0;
     * });
     * }
     * </pre>
     */
    public Java8CompositeHandler withUpdateHandler1(CountUpdateHandler h) {
        final UpdateHandler uh = new UpdateHandler() {
                public UpdateResult apply(String sql, List<Parameter> ps) {
                    return new UpdateResult(h.apply(sql, ps));
                }
            };
        
        return withUpdateHandler(uh);
    }    
}
