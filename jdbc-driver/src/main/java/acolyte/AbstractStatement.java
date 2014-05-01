package acolyte;

import java.util.Collections;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Connection;
import java.sql.ResultSet;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import acolyte.StatementHandler.Parameter;

/**
 * Acolyte base statement.
 *
 * @author Cedric Chantepie
 */
abstract class AbstractStatement implements java.sql.Statement {
    // --- Shared ---

    /**
     * No-parameter list
     */
    protected static final List<Parameter> NO_PARAMS = 
        Collections.unmodifiableList(new ArrayList<Parameter>());

    /**
     * Empty generated keys
     */
    protected static final RowList<Row1<String>>.RowResultSet<Row1<String>> EMPTY_GENERATED_KEYS = RowLists.stringList().resultSet();

    // --- Properties ---

    /**
     * Closed?
     */
    protected boolean closed = false;

    /**
     * Poolable?
     */
    protected boolean poolable = false;

    /**
     * Close on completion?
     */
    protected boolean closeOnCompletion = false;

    /**
     * Escape processing?
     */
    protected boolean escapeProcessing = false;

    /**
     * Statement handler
     */
    protected final StatementHandler handler;

    /**
     * Warning
     */
    protected SQLWarning warning = null;

    /**
     * Cursor name
     */
    protected String cursorName = null;

    /**
     * Last resultset
     */
    protected ResultSet result = null;

    /**
     * Last generated keys
     * see #EMPTY_GENERATED_KEYS
     */
    protected ResultSet generatedKeys = 
        EMPTY_GENERATED_KEYS.withStatement(this);

    /**
     * Max rows
     */
    protected int maxRows = 0;

    /**
     * Last update count
     */
    protected int updateCount = -1;

    /**
     * Default fetch direction
     */
    protected int fetchDirection = ResultSet.FETCH_FORWARD;

    /**
     * Fetch size
     */
    protected int fetchSize = 0;

    /**
     * Batch elements
     */
    private final ArrayList<String> batch;

    /**
     * Owner connection
     */
    protected final acolyte.Connection connection;

    // --- Constructors ---

    /**
     * No-arg constructor.
     * @todo Remove when AbstractResultSet.empty is ok
     */
    protected AbstractStatement() {
        this.connection = null;
        this.handler = null;
        this.batch = new ArrayList<String>();
    } // end of <init>

    /**
     * Acolyte constructor.
     *
     * @param handler Statement handler (not null)
     */
    protected AbstractStatement(final acolyte.Connection connection,
                                final StatementHandler handler) {

        if (connection == null) {
            throw new IllegalArgumentException("Invalid connection");
        } // end of if

        if (handler == null) {
            throw new IllegalArgumentException("Invalid handler");
        } // end of if

        this.connection = connection;
        this.handler = handler;
        this.batch = new ArrayList<String>();
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public ResultSet executeQuery(final String sql) throws SQLException {
        checkClosed();

        this.updateCount = -1;
        this.generatedKeys = EMPTY_GENERATED_KEYS.withStatement(this);

        try {
            final QueryResult res = this.handler.whenSQLQuery(sql, NO_PARAMS);
            
            this.warning = res.getWarning();
            
            return (this.result = res.getRowList().
                    resultSet(this.maxRows).withStatement(this));

        } catch (SQLException se) {
            throw se;
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        } // end of catch
    } // end of executeQuery

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(final String sql) throws SQLException {
        return executeUpdate(sql, RETURN_GENERATED_KEYS);
    } // end of executeUpdate

    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        this.closed = true;
    } // end of close

    /**
     * {@inheritDoc}
     */
    public int getMaxFieldSize() throws SQLException {
        checkClosed();

        return 0;
    } // end of getMaxFieldSize

    /**
     * {@inheritDoc}
     */
    public void setMaxFieldSize(final int max) throws SQLException {
        checkClosed();

        throw new UnsupportedOperationException();
    } // end of setMaxFieldSize

    /**
     * {@inheritDoc}
     */
    public int getMaxRows() throws SQLException {
        checkClosed();

        return this.maxRows;
    } // end of getMaxRows

    /**
     * {@inheritDoc}
     */
    public void setMaxRows(final int max) throws SQLException {
        checkClosed();

        if (max < 0) {
            throw new SQLException("Negative max rows");
        } // end of if

        this.maxRows = max;
    } // end of setMaxRows

    /**
     * {@inheritDoc}
     */
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        checkClosed();

        this.escapeProcessing = enable;
    } // end of setEscapeProcessing

    /**
     * {@inheritDoc}
     */
    public int getQueryTimeout() throws SQLException {
        checkClosed();

        return 0;
    } // end of getQueryTimeout

    /**
     * {@inheritDoc}
     */
    public void setQueryTimeout(final int seconds) throws SQLException {
        checkClosed();

        throw new UnsupportedOperationException();
    } // end of setQueryTimeout

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void cancel() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of cancel

    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarnings() throws SQLException {
        checkClosed();

        return this.warning;
    } // end of getWarnings

    /**
     * {@inheritDoc}
     */
    public void clearWarnings() throws SQLException {
        checkClosed();

        this.warning = null;
    } // end of clearWarnings

    /**
     * {@inheritDoc}
     */
    public void setCursorName(final String name) throws SQLException {
        checkClosed();

        this.cursorName = name;
    } // end of setCursorName

    /**
     * {@inheritDoc}
     */
    public boolean execute(final String sql) throws SQLException {
        checkClosed();

        if (this.handler.isQuery(sql)) {
            executeQuery(sql);

            return true;
        } else {
            executeUpdate(sql);

            return false;
        } // end of else
    } // end of execute

    /**
     * {@inheritDoc}
     */
    public ResultSet getResultSet() throws SQLException {
        checkClosed();

        return this.result;
    } // end of getResultSet

    /**
     * {@inheritDoc}
     */
    public int getUpdateCount() throws SQLException {
        checkClosed();

        return this.updateCount;
    } // end of getUpdateCount

    /**
     * {@inheritDoc}
     */
    public boolean getMoreResults() throws SQLException {
        checkClosed();

        return false;
    } // end of getMoreResults

    /**
     * {@inheritDoc}
     */
    public void setFetchDirection(final int direction) throws SQLException {
        checkClosed();

        this.fetchDirection = direction;
    } // end of setFetchDirection

    /**
     * {@inheritDoc}
     */
    public int getFetchDirection() throws SQLException {
        return this.fetchDirection;
    } // end of getFetchDirection

    /**
     * {@inheritDoc}
     */
    public void setFetchSize(final int rows) throws SQLException {
        checkClosed();

        if (rows < 0) {
            throw new SQLException("Negative fetch size");
        } // end of if

        this.fetchSize = rows;
    } // end of setFetchSize

    /**
     * {@inheritDoc}
     */
    public int getFetchSize() throws SQLException {
        checkClosed();

        return this.fetchSize;
    } // end of getFetchSize

    /**
     * {@inheritDoc}
     */
    public int getResultSetConcurrency() throws SQLException {
        checkClosed();

        return ResultSet.CONCUR_READ_ONLY;
    } // end of getResultSetConcurrency

    /**
     * {@inheritDoc}
     */
    public int getResultSetType() throws SQLException {
        checkClosed();

        return ResultSet.TYPE_FORWARD_ONLY;
    } // end of getResultSetType

    /**
     * {@inheritDoc}
     */
    public void addBatch(final String sql) throws SQLException {
        checkClosed();

        this.batch.add(sql);
    } // end of addBatch

    /**
     * {@inheritDoc}
     */
    public void clearBatch() throws SQLException {
        this.batch.clear();
    } // end of clearBatch

    /**
     * {@inheritDoc}
     */
    public int[] executeBatch() throws SQLException {
        final int[] cs = new int[batch.size()];
        java.util.Arrays.fill(cs, EXECUTE_FAILED);

        final boolean cont = 
            "true".equals(connection.getProperties().
                          get("acolyte.batch.continueOnError"));

        SQLException firstEx = null;  // if |cont| is true
        SQLException lastEx = null;

        int i = 0;
        for (final String sql : batch) {
            try {
                cs[i++] = update(sql, NO_GENERATED_KEYS).left;
            } catch (SQLException se) {
                if (!cont) throw new BatchUpdateException(se.getMessage(), se.getSQLState(), se.getErrorCode(), cs, se.getCause());
                else {
                    if (firstEx == null) firstEx = se;
                    if (lastEx != null) lastEx.setNextException(se);
                    lastEx = se;
                } // end of else
            } // end of catch
        } // end of for

        if (firstEx != null) throw new BatchUpdateException(firstEx.getMessage(), firstEx.getSQLState(), firstEx.getErrorCode(), cs, firstEx.getCause());

        return cs;
    } // end of executeBatch

    /**
     * {@inheritDoc}
     */
    public Connection getConnection() throws SQLException {
        checkClosed();

        return this.connection;
    } // end of getConnection

    /**
     * {@inheritDoc}
     */
    public boolean getMoreResults(final int current) throws SQLException {
        checkClosed();

        return false;
    } // end of getMoreResults

    /**
     * {@inheritDoc}
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        checkClosed();

        return this.generatedKeys;
    } // end of getGeneratedKeys

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(final String sql, final int autoGeneratedKeys) 
        throws SQLException {

        this.result = null;

        final ImmutableTriple<Integer,ResultSet,SQLWarning> res = 
            update(sql, autoGeneratedKeys);

        this.warning = res.right;
        this.generatedKeys = res.middle;

        return (this.updateCount = res.left);
    } // end of executeUpdate

    /**
     * Executes update.
     * @return Triple of update count, generated keys (or null) 
     * and optional warning
     */
    private ImmutableTriple<Integer,ResultSet,SQLWarning> update(final String sql, final int autoGeneratedKeys) throws SQLException {

        checkClosed();

        try {
            final UpdateResult res = this.handler.whenSQLUpdate(sql, NO_PARAMS);
            final SQLWarning w = res.getWarning();
            final ResultSet k = (res.generatedKeys == null 
                                 || autoGeneratedKeys == NO_GENERATED_KEYS) 
                ? EMPTY_GENERATED_KEYS.withStatement(this) 
                : res.generatedKeys.resultSet().withStatement(this);
            
            return ImmutableTriple.of(res.getUpdateCount(), k, w);
        } catch (SQLException se) {
            throw se;
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        } // end of catch
    } // end of update

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(final String sql, final int[] columnIndexes) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of executeUpdate

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(final String sql, final String[] columnNames) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of executeUpdate

    /**
     * {@inheritDoc}
     */
    public boolean execute(final String sql, final int autoGeneratedKeys) 
        throws SQLException {

        checkClosed();

        if (!this.handler.isQuery(sql)) {
            executeUpdate(sql, autoGeneratedKeys);

            return false;
        } // end of if

        // ---

        executeQuery(sql);

        return true;
    } // end of execute

    /**
     * {@inheritDoc}
     */
    public boolean execute(final String sql, final int[] columnIndexes) 
        throws SQLException {

        checkClosed();

        if (!this.handler.isQuery(sql)) {
            throw new SQLFeatureNotSupportedException();
        } // end of if

        executeQuery(sql);

        return true;
    } // end of execute

    /**
     * {@inheritDoc}
     */
    public boolean execute(final String sql, final String[] columnNames) 
        throws SQLException {

        checkClosed();

        if (!this.handler.isQuery(sql)) {
            throw new SQLFeatureNotSupportedException();
        } // end of if

        executeQuery(sql);

        return true;
    } // end of execute

    /**
     * {@inheritDoc}
     */
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    } // end of getResultSetHoldability

    /**
     * {@inheritDoc}
     */
    public boolean isClosed() throws SQLException {
        return this.closed;
    } // end of isClosed

    /**
     * {@inheritDoc}
     */
    public void setPoolable(final boolean poolable) throws SQLException {
        this.poolable = poolable;
    } // end of setPoolable

    /**
     * {@inheritDoc}
     */
    public boolean isPoolable() throws SQLException {
        return this.poolable;
    } // end of isPoolable

    /**
     * {@inheritDoc}
     */
    public void closeOnCompletion() throws SQLException {
        checkClosed();

        this.closeOnCompletion = true;
    } // end of closeOnCompletion

    /**
     * {@inheritDoc}
     */
    public boolean isCloseOnCompletion() throws SQLException {
        checkClosed();
        
        return false;
    } // end of isCloseOnCompletion

    /**
     * {@inheritDoc}
     */
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    } // end of isWrapperFor

    /**
     * {@inheritDoc}
     */
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw new SQLException();
        } // end of if

        @SuppressWarnings("unchecked")
        final T proxy = (T) this;

        return proxy;
    } // end of unwrap

    // ---

    /**
     * Throws a SQLException("Statement is closed") if connection is closed.
     */
    protected void checkClosed() throws SQLException {
        if (this.closed) {
            throw new SQLException("Statement is closed");
        } // end of if
    } // end of checkClosed
} // end of class AbstractStatement
