package acolyte;

import java.math.BigDecimal;

import java.io.InputStream;
import java.io.Reader;

import java.util.Calendar;

import java.net.URL;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLXML;
import java.sql.Array;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Ref;

/**
 * Acolyte prepared statement.
 *
 * @author Cedric Chantepie
 */
public final class PreparedStatement 
    extends AbstractStatement implements java.sql.PreparedStatement {

    // --- Constructors ---

    /**
     * Acolyte constructor.
     *
     * @param handler Statement handler (not null)
     */
    protected PreparedStatement(final Connection connection,
                                final StatementHandler handler) {

        super(connection, handler);
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public ResultSet executeQuery() throws SQLException {
        checkClosed();

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public int executeUpdate() throws SQLException {
        checkClosed();

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setNull(final int i, final int j) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setBoolean(final int i, final boolean b) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setByte(final int i, final byte b) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setShort(final int i, final short s) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setInt(final int i, final int j) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setLong(final int i, final long j) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setFloat(final int i, final float f) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setDouble(final int i, final double d) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setBigDecimal(final int i, final BigDecimal bd) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setString(final int i, final String str) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setBytes(final int i, final byte[] a) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setDate(final int i, final Date d) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setTime(final int i, final Time t) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setTimestamp(final int i, final Timestamp ts) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setAsciiStream(final int parameterIndex, 
                               final InputStream x, 
                               final int length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setAsciiStream

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setUnicodeStream(final int parameterIndex, 
                                 final InputStream x, 
                                 final int length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setUnicodeStream

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setBinaryStream(final int parameterIndex, 
                                final InputStream x, 
                                final int length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBinaryStream

    /**
     * {@inheritDoc}
     */
    public void clearParameters() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setObject(final int i, final Object o, final int j) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setObject(final int i, final Object o) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public boolean execute() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLException Batch is not supported
     * @see AbstractStatement#addBatch
     */
    public void addBatch() throws SQLException {
        throw new SQLException("Batch is not supported");
    } // end of addBatch

    /**
     * {@inheritDoc}
     */
    public void setCharacterStream(final int i, final Reader r, final int j) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setRef(final int parameterIndex, final Ref x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setRef

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setBlob(final int parameterIndex, final Blob x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBlob

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setClob(final int parameterIndex, final Clob clob) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setClob

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setArray(final int parameterIndex, final Array x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setArray

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getMetaData

    /**
     * {@inheritDoc}
     */
    public void setDate(final int i, 
                        final Date d, 
                        final Calendar c) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setTime(final int i, 
                        final Time t, 
                        final Calendar c) throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setTimestamp(final int i, 
                             final Timestamp ts, 
                             final Calendar c) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public void setNull(final int i, final int j, final String str) 
        throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setURL(final int parameterIndex, final URL x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setURL

    /**
     * {@inheritDoc}
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of getParameterMetaData

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setRowId(final int parameterIndex, final RowId x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setRowId

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNString(final int parameterIndex, final String value) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNString

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNCharacterStream(final int parameterIndex,
                                    final Reader value, 
                                    final long length) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNCharacterStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNClob(final int parameterIndex, final NClob value) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setClob(final int parameterIndex, 
                        final Reader reader, 
                        final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setBlob(final int parameterIndex, 
                        final InputStream inputStream, 
                        final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBlob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNClob(final int parameterIndex, 
                         final Reader reader, 
                         final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setSQLXML

    /**
     * {@inheritDoc}
     */
    public void setObject(final int i, 
                          final Object o, 
                          final int k, 
                          final int j) throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of 

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setAsciiStream(final int parameterIndex, 
                               final InputStream x, 
                               final long length)
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setAsciiStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setBinaryStream(final int parameterIndex, 
                                final InputStream x, 
                                final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBinaryStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setCharacterStream(final int parameterIndex, 
                                   final Reader x, 
                                   final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setCharacterStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setAsciiStream(final int parameterIndex, final InputStream x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setAsciiStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setBinaryStream(final int parameterStream, 
                                final InputStream x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBinaryStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setCharacterStream(final int parameterIndex, final Reader x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setCharacterStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNCharacterStream(final int parameterIndex, final Reader x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNCharacterStream

    /**
     * {@inheritDoc}
     */
    public void setClob(final int parameterIndex, final Reader x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setBlob(final int parameterIndex, final InputStream x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBlob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNClob(final int parameterIndex, final Reader x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNClob
} // end of PreparedStatement
