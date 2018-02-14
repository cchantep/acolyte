package acolyte.jdbc;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Executor;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLClientInfoException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.NClob;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;

/**
 * Acolyte connection.
 *
 * @author Cedric Chantepie
 */
public class Connection implements java.sql.Connection {
    // --- Properties ---

    /**
     * JDBC URL
     */
    final String url;

    /**
     * JDBC meta-properties
     */
    private final Properties props;

    /**
     * Acolyte handler
     */
    private final ConnectionHandler handler;

    /**
     * Auto-commit flag
     */
    private boolean autoCommit = false;

    /**
     * Read-only flag
     */
    private boolean readonly = false;

    /**
     * Closed flag
     */
    private boolean closed = false;

    /**
     * Validity
     */
    private boolean validity = true;

    /**
     * Current warnings
     */
    private SQLWarning warning = null;

    /**
     * Transaction isolation
     */
    private int transactionIsolation = Connection.TRANSACTION_NONE;

    /**
     * Type map
     */
    private Map<String,Class<?>> typemap = new HashMap<String,Class<?>>();

    /**
     * Current savepoint
     */
    private Savepoint savepoint = null;

    /**
     * Client info properties
     */
    private Properties clientInfo = new Properties();

    /**
     * Catalog name for next statements
     */
    private String catalog = null;

    /**
     * Schema name for next statements
     */
    private String schema = null;

    // --- Constructors ---

    /**
     * Bulk constructor.
     *
     * @param url JDBC URL
     * @param props JDBC properties (immutable)
     * @param handler Acolyte handler
     * @see Driver#connect
     */
    public Connection(final String url, 
                      final Properties props, 
                      final ConnectionHandler handler) {

        if (url == null) {
            throw new IllegalArgumentException("Invalid JDBC URL");
        } // end of if

        if (handler == null) {
            throw new IllegalArgumentException("Invalid Acolyte handler");
        } // end of if

        // ---

        this.url = url;
        this.props = new Properties();
        this.handler = handler;

        if (props != null) {
            this.props.putAll(props);
        } // end of if
    } // end of <init>

    // --- Connection impl ---

    /**
     * {@inheritDoc}
     */
    public Statement createStatement() throws SQLException {
        checkClosed();

        return new PlainStatement(this, this.handler.getStatementHandler());
    } // end of createStatement        

    /**
     * {@inheritDoc}
     * @see #prepareStatement(String, int)
     * @see java.sql.Statement#RETURN_GENERATED_KEYS
     */
    public PreparedStatement prepareStatement(final String sql) 
        throws SQLException {

        return this.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    } // end of prepareStatement

    /**
     * {@inheritDoc}
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        checkClosed();

        return new acolyte.jdbc.
            CallableStatement(this, sql, Statement.RETURN_GENERATED_KEYS,
                              this.handler.getStatementHandler());

    } // end of prepareCall

    /**
     * {@inheritDoc}
     */
    public String nativeSQL(String sql) throws SQLException {
        checkClosed();

        return sql;
    } // end of nativeSQL

    /**
     * {@inheritDoc}
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkClosed();

        this.autoCommit = autoCommit;
    } // end of setAutoCommit

    /**
     * {@inheritDoc}
     */
    public boolean getAutoCommit() throws SQLException {
        checkClosed();

        return this.autoCommit;
    } // end of getAutoCommit

    /**
     * {@inheritDoc}
     */
    public void commit() throws SQLException {
        checkClosed();
            
        if (this.autoCommit) {
            throw new SQLException("Auto-commit is enabled");
        } // end of if
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void rollback() throws SQLException {
        checkClosed();
            
        if (this.autoCommit) {
            throw new SQLException("Auto-commit is enabled");
        } // end of if
    } // end of rollback

    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        if (this.closed) {
            throw new SQLException("Connection is already closed");
        } // end of if

        this.closed = true;
        this.validity = false;
    } // end of close

    /**
     * {@inheritDoc}
     */
    public boolean isClosed() throws SQLException {
        return this.closed;
    } // end of isClosed

    /**
     * {@inheritDoc}
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        checkClosed();

        return new acolyte.jdbc.DatabaseMetaData(this);
    } // end of getMetaData

    /**
     * {@inheritDoc}
     */
    public void setReadOnly(boolean readonly) throws SQLException {
        checkClosed();

        this.readonly = readonly;
    } // end of setReadOnly

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly() throws SQLException {
        checkClosed();

        return this.readonly;
    } // end of isReadOnly

    /**
     * {@inheritDoc}
     */
    public void setCatalog(final String catalog) throws SQLException {
        checkClosed();

        this.catalog = catalog;
    } // end of setCatalog

    /**
     * {@inheritDoc}
     */
    public String getCatalog() throws SQLException {
        checkClosed();

        return this.catalog;
    } // end of getCatalog

    /**
     * {@inheritDoc}
     */
    public void setTransactionIsolation(int level) throws SQLException {
        checkClosed();

        this.transactionIsolation = level;
    } // end of setTransactionIsolation

    /**
     * {@inheritDoc}
     */
    public int getTransactionIsolation() throws SQLException {
        checkClosed();

        return this.transactionIsolation;
    } // end of getTransactionIsolation

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
    public Statement createStatement(final int resultSetType, 
                                     final int resultSetConcurrency) 
        throws SQLException {

        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLFeatureNotSupportedException("Unsupported result set type");
        } // end of if

        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            throw new SQLFeatureNotSupportedException("Unsupported result set concurrency");

        } // end of if

        return createStatement();
    } // end of createStatement

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(final String sql, 
                                              final int resultSetType, 
                                              final int resultSetConcurrency)
        throws SQLException {

        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLFeatureNotSupportedException("Unsupported result set type");
        } // end of if

        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            throw new SQLFeatureNotSupportedException("Unsupported result set concurrency");

        } // end of if

        return prepareStatement(sql);
    } // end of prepareStatement

    /**
     * {@inheritDoc}
     */
    public CallableStatement prepareCall(final String sql, 
                                         final int resultSetType, 
                                         final int resultSetConcurrency)
        throws SQLException {

        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLFeatureNotSupportedException("Unsupported result set type");
        } // end of if

        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            throw new SQLFeatureNotSupportedException("Unsupported result set concurrency");

        } // end of if

        return prepareCall(sql);
    } // end of prepareCall

    /**
     * {@inheritDoc}
     */
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        checkClosed();

        return this.typemap;
    } // end of getTypeMap

    /**
     * {@inheritDoc}
     */
    public void setTypeMap(final Map<String, Class<?>> typemap) 
        throws SQLException {

        checkClosed();

        if (typemap == null) {
            throw new SQLException("Invalid type-map");
        } // end of if

        this.typemap = typemap;
    } // end of setTypeMap

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public void setHoldability(final int holdability) throws SQLException {
        checkClosed();

        throw new SQLFeatureNotSupportedException();
    } // end of setHoldability

    /**
     * {@inheritDoc}
     */
    public int getHoldability() throws SQLException {
        checkClosed();

        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    } // end of getHoldability

    /**
     * {@inheritDoc}
     */
    public Savepoint setSavepoint() throws SQLException {
        checkClosed();

        if (this.autoCommit) {
            throw new SQLException("Auto-commit is enabled");
        } // end of if
                
        return (this.savepoint = new acolyte.jdbc.Savepoint());
    } // end of setSavepoint

    /**
     * {@inheritDoc}
     */
    public Savepoint setSavepoint(final String name) throws SQLException {
        checkClosed();

        if (this.autoCommit) {
            throw new SQLException("Auto-commit is enabled");
        } // end of if

        return (this.savepoint = new acolyte.jdbc.Savepoint(name));
    } // end of setSavepoint

    /**
     * {@inheritDoc}
     */
    public void rollback(final Savepoint savepoint) throws SQLException {
        checkClosed();

        if (this.autoCommit) {
            throw new SQLException("Auto-commit is enabled");
        } // end of if        

        throw new SQLFeatureNotSupportedException();
    } // end of rollback

    /**
     * {@inheritDoc}
     */
    public void releaseSavepoint(final Savepoint savepoint) 
        throws SQLException {

        checkClosed();

        if (this.autoCommit) {
            throw new SQLException("Auto-commit is enabled");
        } // end of if        

        throw new SQLFeatureNotSupportedException();
    } // end of releaseSavepoint

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException if |resultSetHoldability| is not ResultSet.CLOSE_CURSORS_AT_COMMIT
     */
    public Statement createStatement(final int resultSetType, 
                                     final int resultSetConcurrency, 
                                     final int resultSetHoldability) 
        throws SQLException {

        if (resultSetHoldability != ResultSet.CLOSE_CURSORS_AT_COMMIT) {
            throw new SQLFeatureNotSupportedException("Unsupported result set holdability");
        } // end of if

        return createStatement(resultSetType, resultSetConcurrency);
    } // end of createStatement

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException if |resultSetHoldability| is not ResultSet.CLOSE_CURSORS_AT_COMMIT
     */
    public PreparedStatement prepareStatement(final String sql, 
                                              final int resultSetType, 
                                              final int resultSetConcurrency, 
                                              final int resultSetHoldability)
        throws SQLException {

        if (resultSetHoldability != ResultSet.CLOSE_CURSORS_AT_COMMIT) {
            throw new SQLFeatureNotSupportedException("Unsupported result set holdability");
        } // end of if

        return prepareStatement(sql, resultSetType, resultSetConcurrency);
    } // end of prepareStatement

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException if |resultSetHoldability| is not ResultSet.CLOSE_CURSORS_AT_COMMIT
     */
    public CallableStatement prepareCall(final String sql, 
                                         final int resultSetType, 
                                         final int resultSetConcurrency, 
                                         final int resultSetHoldability)
        throws SQLException {

        if (resultSetHoldability != ResultSet.CLOSE_CURSORS_AT_COMMIT) {
            throw new SQLFeatureNotSupportedException("Unsupported result set holdability");
        } // end of if

        return prepareCall(sql, resultSetType, resultSetConcurrency);
    } // end of prepareCall

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException if |autoGeneratedKeys| is Statement.RETURN_GENERATED_KEYS
     * @see #prepareStatement(java.lang.String)
     */
    public PreparedStatement prepareStatement(final String sql, 
                                              final int autoGeneratedKeys) 
        throws SQLException {

        checkClosed();

        return new acolyte.jdbc.
            PreparedStatement(this, sql, autoGeneratedKeys, null, null,
                              this.handler.getStatementHandler());

    } // end of prepareStatement

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(final String sql, 
                                              final int[] columnIndexes) 
        throws SQLException {

        checkClosed();

        return new acolyte.jdbc.
            PreparedStatement(this, sql, Statement.RETURN_GENERATED_KEYS,
                              null, columnIndexes,
                              this.handler.getStatementHandler());

    } // end of prepareStatement

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(final String sql, 
                                              final String[] columnNames) 
        throws SQLException {

        checkClosed();

        return new acolyte.jdbc.
            PreparedStatement(this, sql, Statement.RETURN_GENERATED_KEYS,
                              columnNames, null,
                              this.handler.getStatementHandler());

    } // end of prepareStatement

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of createClob

    /**
     * {@inheritDoc}
     * @see #createBlob(byte[])
     */
    public Blob createBlob() throws SQLException {
        return acolyte.jdbc.Blob.Nil();
    } // end of createBlob

    /**
     * Returns a BLOB with given |data|.
     *
     * @param data the binary data
     * @return the created BLOB
     * @throws SQLException if fails to create a BLOB
     */
    public Blob createBlob(final byte[] data) throws SQLException {
        return new javax.sql.rowset.serial.SerialBlob(data);
    } // end of createBlob

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of createNClob

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of createSQLXML

    /**
     * {@inheritDoc}
     * @see #isClosed
     */
    public boolean isValid(final int timeout) throws SQLException {
        return this.validity;
    } // end of isValid

    /**
     * {@inheritDoc}
     */
    public void setClientInfo(final String name, final String value) 
        throws SQLClientInfoException {

        if (this.closed) {
            throw new SQLClientInfoException();
        } // end of if

        this.clientInfo.put(name, value);
    } // end of setClientInfo

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if |properties| is null
     */
    public void setClientInfo(final Properties properties) 
        throws SQLClientInfoException {

        if (properties == null) {
            throw new IllegalArgumentException();
        } // end of if

        if (this.closed) {
            throw new SQLClientInfoException();
        } // end of if

        this.clientInfo = properties;
    } // end of setClientInfo

    /**
     * {@inheritDoc}
     */
    public String getClientInfo(String name) throws SQLException {
        if (this.closed) {
            throw new SQLClientInfoException();
        } // end of if

        return this.clientInfo.getProperty(name);
    } // end of getClientInfo

    /**
     * {@inheritDoc}
     */
    public Properties getClientInfo() throws SQLException {
        if (this.closed) {
            throw new SQLClientInfoException();
        } // end of if

        return this.clientInfo;
    } // end of getClientInfo

    /**
     * {@inheritDoc}
     */
    public Array createArrayOf(final String typeName, 
                               final Object[] elements) throws SQLException {

        final String jdbcClassName = Defaults.jdbcTypeNameClasses.get(typeName);

        if (jdbcClassName == null) {
            throw new SQLException("Unsupported type: " + typeName);
        } // end of if

        // ---

        try {
            final Class jdbcClass = Class.forName(jdbcClassName);

            return ImmutableArray.getInstance(jdbcClass, elements);
        } catch (ClassNotFoundException ce) {
            throw new SQLException("Element type not found: " + jdbcClassName);
        } // end of catch
    } // end of createArrayOf

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public Struct createStruct(final String typeName, 
                               final Object[] attributes) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of createStruct

    /**
     * {@inheritDoc}
     */
    public void setSchema(final String schema) throws SQLException {
        checkClosed();

        this.schema = schema;
    } // end of setSchema

    /**
     * {@inheritDoc}
     */
    public String getSchema() throws SQLException {
        checkClosed();

        return this.schema;
    } // end of getSchema

    /**
     * {@inheritDoc} (Java 1.7)
     */
    public void abort(final Executor exec) throws SQLException {
        if (exec == null) {
            throw new SQLException("Missing executor");
        } // end of if

        if (this.closed) {
            return;
        } // end of if

        this.closed = true;
    } // end of abort

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public void setNetworkTimeout(final Executor executor, 
                                  final int milliseconds) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNetworkTimeout

    /**
     * {@inheritDoc} (Java 1.7)
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public int getNetworkTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getNetworkTimeout

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
     * Returns connection properties.
     * @return the current properties
     */
    public Properties getProperties() {
        return this.props;
    } // end of getProperties

    /**
     * Throws a SQLException("Connection is closed") if connection is closed.
     */
    private void checkClosed() throws SQLException {
        if (this.closed) {
            throw new SQLException("Connection is closed");
        } // end of if
    } // end of checkClosed

    // --- Inner classes ---

    /**
     * Plain statement (not prepared or callable).
     */
    private static final class PlainStatement extends AbstractStatement {
        PlainStatement(final Connection connection,
                       final StatementHandler handler) {

            super(connection, handler);
        }
    } // end of class PlainStatement
} // end of class Connection
