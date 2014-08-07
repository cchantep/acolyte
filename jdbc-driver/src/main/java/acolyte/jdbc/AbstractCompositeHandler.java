package acolyte.jdbc;

import java.util.List;

import java.util.regex.Pattern;

import java.sql.SQLException;
import java.sql.ResultSet;

import acolyte.jdbc.StatementHandler.Parameter;

/**
 * Base for rule-based (immutable/thread-safe) statement handler.
 *
 * @author Cedric Chantepie
 */
public abstract class AbstractCompositeHandler<T extends AbstractCompositeHandler> implements StatementHandler {

    // --- Properties ---

    /**
     * Query (ordered) detection patterns
     */
    protected final Pattern[] queryDetection;

    /**
     * Query handler
     */
    protected final QueryHandler queryHandler;

    /**
     * Update handler
     */
    protected final UpdateHandler updateHandler;

    // --- Constructors ---

    /**
     * Constructor
     */
    protected AbstractCompositeHandler() {
        this.queryDetection = new Pattern[0];
        this.queryHandler = null;
        this.updateHandler = null;
    } // end of <init>

    /**
     * Copy constructor.
     */
    protected AbstractCompositeHandler(final Pattern[] queryDetection,
                                       final QueryHandler queryHandler,
                                       final UpdateHandler updateHandler) {
        
        this.queryDetection = (queryDetection == null) ? 
            new Pattern[0] : queryDetection;
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
    public T withQueryDetection(final String... pattern) {
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
    public abstract T withQueryDetection(final Pattern[] pattern);

    /**
     * Appends given |pattern| to current query detection.
     *
     * @throws IllegalArgumentException if pattern is null
     */
    protected Pattern[] queryDetectionPattern(final Pattern... pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        if (this.queryDetection == null) {
            return pattern;
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

        return patterns;
    } // end of queryDetectionPattern

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
} // end of class AbstractCompositeHandler
