package acolyte;

import java.util.Properties;
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
    public void setAutoCommit(boolean b) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public boolean getAutoCommit() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

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
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setReadOnly(boolean b) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

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
    public void setTransactionIsolation(int i) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public int getTransactionIsolation() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarnings() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void clearWarnings() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

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
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setTypeMap(Map<String, Class<?>> typemap) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

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
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public Savepoint setSavepoint(String str) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

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
    public void setClientInfo(String a, String b) 
        throws SQLClientInfoException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setClientInfo(Properties props) throws SQLClientInfoException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

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
        throw new RuntimeException("Not yet implemented");
    } // end of 

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
