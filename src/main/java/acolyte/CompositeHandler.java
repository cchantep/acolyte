package acolyte;

import java.util.List;

import java.util.regex.Pattern;

import java.sql.SQLException;
import java.sql.ResultSet;

import org.apache.commons.lang3.tuple.ImmutablePair;

import acolyte.ParameterMetaData.Parameter;

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
    public ResultSet whenSQLQuery(final String sql, final List<ImmutablePair<Parameter,Object>> parameters) throws SQLException {
        return null;
    } // end of whenSQLQuery

    /**
     * {@inheritDoc}
     */
    public int whenSQLUpdate(final String sql, final List<ImmutablePair<Parameter,Object>> parameters) throws SQLException {

        if (this.updateHandler == null) {
            throw new SQLException("No update handler");
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
     * @param queryDetection Query detection pattern
     * @throws java.util.regex.PatternSyntaxException If |pattern| is invalid
     * @see #withQueryDetection(java.util.regex.Pattern)
     */
    public CompositeHandler withQueryDetection(final String pattern) {
        return withQueryDetection(Pattern.compile(pattern));
    } // end of withQueryDetection

    /**
     * Returns an new handler based on this one, but including given
     * query detection |pattern|. If there is already existing pattern,
     * the new one will be used after.
     *
     * @param queryDetection Query detection pattern
     * @throws IllegalArgumentException if pattern is null
     */
    public CompositeHandler withQueryDetection(final Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        final Pattern[] patterns = 
            new Pattern[this.queryDetection.length+1];

        System.arraycopy(this.queryDetection, 0, 
                         patterns, 0, 
                         this.queryDetection.length);

        patterns[patterns.length-1] = pattern;

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
        public ResultSet apply(String sql, List<ImmutablePair<Parameter,Object>> parameters) throws SQLException;

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
        public int apply(String sql, List<ImmutablePair<Parameter,Object>> parameters) throws SQLException;

    } // end of interfaceQueryHandler
} // end of class CompositeHandler
