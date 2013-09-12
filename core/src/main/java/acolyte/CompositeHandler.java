package acolyte;

import java.util.List;

import java.util.regex.Pattern;

import java.sql.SQLException;
import java.sql.ResultSet;

import acolyte.StatementHandler.Parameter;

/**
 * Rule-based (immutable/thread-safe) statement handler.
 *
 * @author Cedric Chantepie
 */
public class CompositeHandler implements StatementHandler {
    // --- Properties ---

    /**
     * Query (ordered) detection patterns
     */
    private final Pattern[] queryDetection;

    /**
     * Query handler
     */
    private final QueryHandler queryHandler;

    /**
     * Update handler
     */
    private final UpdateHandler updateHandler;

    // --- Constructors ---

    /**
     * Constructor
     */
    public CompositeHandler() {
        this.queryDetection = new Pattern[0];
        this.queryHandler = null;
        this.updateHandler = null;
    } // end of <init>

    /**
     * Copy constructor.
     */
    public CompositeHandler(final Pattern[] queryDetection,
                            final QueryHandler queryHandler,
                            final UpdateHandler updateHandler) {

        this.queryDetection = (queryDetection == null) ? null : queryDetection;
        this.queryHandler = queryHandler;
        this.updateHandler = updateHandler;
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public QueryResult whenSQLQuery(final String sql, 
                                    final List<Parameter> parameters) 
        throws SQLException {

        if (this.queryHandler == null) {
            throw new SQLException("No query handler");
        } // end of if

        return this.queryHandler.apply(sql, parameters);
    } // end of whenSQLQuery

    /**
     * {@inheritDoc}
     */
    public UpdateResult whenSQLUpdate(final String sql, 
                                      final List<Parameter> parameters) 
        throws SQLException {

        if (this.updateHandler == null) {
            throw new SQLException("No update handler: " + sql);
        } // end of if

        return this.updateHandler.apply(sql, parameters);
    } // end of whenSQLUpdate

    /**
     * {@inheritDoc}
     */
    public boolean isQuery(final String sql) {
        for (final Pattern p : queryDetection) {
            if (p.matcher(sql).lookingAt()) {
                return true;
            } // end of if
        } // end of for

        return false;
    } // end of isQuery

    /**
     * {@inheritDoc}
     * @throws RuntimeException Not supported
     */
    public ResultSet getGeneratedKeys() {
        throw new RuntimeException("Not supported");
    } // end of getGeneratedKeys

    // ---

    /**
     * Returns an new handler based on this one, but including given
     * query detection |pattern|. If there is already existing pattern,
     * the new one will be used after.
     *
     * @param pattern Query detection pattern list
     * @throws java.util.regex.PatternSyntaxException If |pattern| is invalid
     * @see #withQueryDetection(java.util.regex.Pattern)
     */
    public CompositeHandler withQueryDetection(final String... pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        final Pattern[] ps = new Pattern[pattern.length];

        int i = 0;
        for (final String p : pattern) {
            ps[i++] = Pattern.compile(p);
        } // end of for

        return withQueryDetection(ps);
    } // end of withQueryDetection

    /**
     * Returns an new handler based on this one, but including given
     * query detection |pattern|. If there is already existing pattern,
     * the new one will be used after.
     *
     * @param pattern Query detection pattern
     * @throws IllegalArgumentException if pattern is null
     */
    public CompositeHandler withQueryDetection(final Pattern... pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        final Pattern[] patterns = 
            new Pattern[this.queryDetection.length + pattern.length];

        System.arraycopy(this.queryDetection, 0, 
                         patterns, 0, 
                         this.queryDetection.length);

        int i = this.queryDetection.length;

        for (final Pattern p : pattern) {
            patterns[i++] = p;
        } // end of for

        return new CompositeHandler(patterns, 
                                    this.queryHandler, 
                                    this.updateHandler);

    } // end of withQueryDetection

    /**
     * Returns a new handler based on this one, 
     * but with given query |handler| appended.
     *
     * @param handler Query handler
     * @throws IllegalArgumentException if handler is null
     */
    public CompositeHandler withQueryHandler(final QueryHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        return new CompositeHandler(this.queryDetection,
                                    handler,
                                    this.updateHandler);
        
    } // end of withQueryHandler

    /**
     * Returns a new handler based on this one, 
     * but with given update |handler| appended.
     *
     * @param handler Update handler
     * @throws IllegalArgumentException if handler is null
     */
    public CompositeHandler withUpdateHandler(final UpdateHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        return new CompositeHandler(this.queryDetection,
                                    this.queryHandler,
                                    handler);
        
    } // end of withUpdateHandler

    // --- Inner classes ---

    /**
     * Query handler.
     */
    public static interface QueryHandler {
        public QueryResult apply(String sql, List<Parameter> parameters) throws SQLException;

    } // end of interfaceQueryHandler

    /**
     * Update handler.
     */
    public static interface UpdateHandler {
        /**
         * Handles update.
         *
         * @return Update count
         */
        public UpdateResult apply(String sql, List<Parameter> parameters) 
            throws SQLException;

    } // end of interfaceQueryHandler
} // end of class CompositeHandler
