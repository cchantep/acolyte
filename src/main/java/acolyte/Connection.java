package acolyte;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Executor;

import java.sql.SQLClientInfoException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.NClob;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;

public final class Connection implements java.sql.Connection {
    // --- Properties ---

    /**
     * JDBC URL
     */
    private final String url;

    /**
     * JDBC meta-properties
     */
    private final Properties props;

    /**
     * Acolyte handler
     */
    private final Object handler;

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

    // --- Constructors ---

    /**
     * Bulk constructor.
     *
     * @param url JDBC URL
     * @param props JDBC properties
     * @param handler Acolyte handler
     * @see Driver#connect
     */
    public Connection(final String url, 
                      final Properties props, 
                      final Object handler) {

        if (url == null) {
            throw new IllegalArgumentException("Invalid JDBC URL");
        } // end of if

        if (handler == null) {
            throw new IllegalArgumentException("Invalid Acolyte handler");
        } // end of if

        // ---

        this.url = url;
        this.props = props;
        this.handler = handler;
    } // end of <init>

    // --- Connection impl ---

    /**
     * {@inheritDoc}
     */
    public Statement createStatement() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of createStatement        

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String str) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public CallableStatement prepareCall(String str) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public String nativeSQL(String str) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
    } // end of setAutoCommit

    /**
     * {@inheritDoc}
     */
    public boolean getAutoCommit() throws SQLException {
        return this.autoCommit;
    } // end of getAutoCommit

    /**
     * {@inheritDoc}
     */
    public void commit() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void rollback() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

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
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setReadOnly(boolean readonly) throws SQLException {
        this.readonly = readonly;
    } // end of setReadOnly

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly() throws SQLException {
        return this.readonly;
    } // end of isReadOnly

    /**
     * {@inheritDoc}
     */
    public void setCatalog(String str) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public String getCatalog() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setTransactionIsolation(int level) throws SQLException {
        this.transactionIsolation = level;
    } // end of setTransactionIsolation

    /**
     * {@inheritDoc}
     */
    public int getTransactionIsolation() throws SQLException {
        return this.transactionIsolation;
    } // end of getTransactionIsolation

    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarnings() throws SQLException {
        return this.warning;
    } // end of getWarnings

    /**
     * {@inheritDoc}
     */
    public void clearWarnings() throws SQLException {
        this.warning = null;
    } // end of clearWarnings

    /**
     * {@inheritDoc}
     */
    public Statement createStatement(int a, int b) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String str, int a, int b) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public CallableStatement prepareCall(String str, int a, int b) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return this.typemap;
    } // end of getTypeMap

    /**
     * {@inheritDoc}
     */
    public void setTypeMap(final Map<String, Class<?>> typemap) 
        throws SQLException {

        if (typemap == null) {
            throw new SQLException("Invalid type-map");
        } // end of if

        this.typemap = typemap;
    } // end of setTypeMap

    /**
     * {@inheritDoc}
     */
    public void setHoldability(int i) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public int getHoldability() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public Savepoint setSavepoint() throws SQLException {
        return (this.savepoint = new acolyte.Savepoint());
    } // end of setSavepoint

    /**
     * {@inheritDoc}
     */
    public Savepoint setSavepoint(String name) throws SQLException {
        return (this.savepoint = new acolyte.Savepoint(name));
    } // end of setSavepoint

    /**
     * {@inheritDoc}
     */
    public void rollback(Savepoint i) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void releaseSavepoint(Savepoint i) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public Statement createStatement(int a, int b, int c) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String str, int a, int b, int c) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public CallableStatement prepareCall(String str, int a, int b, int c) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String str, int i) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String str, int[] args) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String str, String[] args) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public Clob createClob() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public Blob createBlob() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public NClob createNClob() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public SQLXML createSQLXML() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public boolean isValid(int i) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setClientInfo(final String name, final String value) 
        throws SQLClientInfoException {

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

        this.clientInfo = properties;
    } // end of setClientInfo

    /**
     * {@inheritDoc}
     */
    public String getClientInfo(String str) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public Properties getClientInfo() throws SQLException {
        return this.clientInfo;
    } // end of getClientInfo

    /**
     * {@inheritDoc}
     */
    public Array createArrayOf(String str, Object[] data) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public Struct createStruct(String str, Object[] data) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setSchema(String str) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public String getSchema() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void abort(Executor exec) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setNetworkTimeout(Executor exec, int i) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public int getNetworkTimeout() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of isWrapperFor

    /**
     * {@inheritDoc}
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of unwrap
} // end of class Connection
