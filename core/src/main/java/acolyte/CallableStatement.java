package acolyte;

import java.io.InputStream;
import java.io.Reader;

import java.math.BigDecimal;

import java.net.URL;

import java.util.Calendar;
import java.util.TreeMap;
import java.util.Map;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLException;
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

import static acolyte.ParameterMetaData.ParameterDef;
import static acolyte.ParameterMetaData.Default;
import static acolyte.ParameterMetaData.Scaled;

/**
 * Acolyte callable statement.
 *
 * @author Cedric Chantepie
 */
public final class CallableStatement 
    extends PreparedStatement implements java.sql.CallableStatement {

    // --- Properties ---

    /**
     * Indexed parameters
     */
    private final TreeMap<Integer,ParameterDef> indexedOut = 
        new TreeMap<Integer,ParameterDef>();

    /**
     * Named parameters
     */
    private final TreeMap<String,ParameterDef> namedOut = 
        new TreeMap<String,ParameterDef>();

    // --- Constructors ---

    /**
     * Acolyte constructor.
     *
     * @param connection Owner connection
     * @param sql SQL statement
     * @param handler Statement handler (not null)
     */
    protected CallableStatement(final Connection connection,
                                final String sql,
                                final StatementHandler handler) {
        
        super(connection, sql, handler);
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public void registerOutParameter(final int parameterIndex, 
                                     final int sqlType) throws SQLException {

        checkClosed();

        if (parameterIndex < 1) {
            throw new SQLException("Invalid index: " + parameterIndex);
        } // end of if

        indexedOut.put(parameterIndex, Default(sqlType));
    } // end of registerOutParameter

    /**
     * {@inheritDoc}
     */
    public void registerOutParameter(final int parameterIndex, 
                                     final int sqlType, 
                                     final int scale) throws SQLException {

        checkClosed();

        if (parameterIndex < 1) {
            throw new SQLException("Invalid index: " + parameterIndex);
        } // end of if

        indexedOut.put(parameterIndex, Scaled(sqlType, scale));
    } // end of registerOutParameter

    /**
     * {@inheritDoc}
     */
    public void registerOutParameter(final int parameterIndex, 
                                     final int sqlType, 
                                     final String typeName) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of registerOutParameter

    /**
     * {@inheritDoc}
     */
    public void registerOutParameter(final String parameterName, 
                                     final int sqlType) throws SQLException {

        checkClosed();

        if (parameterName == null || parameterName.length() == 0) {
            throw new SQLException("Invalid name: " + parameterName);
        } // end of if

        this.namedOut.put(parameterName, Default(sqlType));
    } // end of registerOutParameter

    /**
     * {@inheritDoc}
     */
    public void registerOutParameter(final String parameterName, 
                                     final int sqlType, 
                                     final int scale) throws SQLException {

        checkClosed();

        if (parameterName == null || parameterName.length() == 0) {
            throw new SQLException("Invalid name: " +parameterName);
        } // end of if

        this.namedOut.put(parameterName, Scaled(sqlType, scale));
    } // end of registerOutParameter

    /**
     * {@inheritDoc}
     */
    public void registerOutParameter(final String parameterName, 
                                     final int sqlType, 
                                     final String typeName) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of registerOutParameter

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setObject(final String parameterName, 
                          final Object x) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setObject

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setObject(final String parameterName, 
                          final Object x,
                          final int targetSqlType) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setObject

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setObject(final String parameterName, 
                          final Object x,
                          final int targetSqlType,
                          final int scale) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setObject

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setURL(final String parameterName, final URL x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setURL

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setAsciiStream(final String parameterName, 
                               final InputStream x) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setAsciiStream

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setAsciiStream(final String parameterName, 
                               final InputStream x,
                               final long l) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setAsciiStream

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setAsciiStream(final String parameterName, 
                               final InputStream x,
                               final int l) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setAsciiStream

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setBinaryStream(final String parameterName, 
                                final InputStream x) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBinaryStream

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setBinaryStream(final String parameterName, 
                                final InputStream x,
                                final long l) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBinaryStream

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setBinaryStream(final String parameterName, 
                                final InputStream x,
                                final int l) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBinaryStream

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setBytes(final String parameterName, final byte[] x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBytes

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setBlob(final String parameterName, final Blob blob) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBlob

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException
     */
    public void setBlob(final String parameterName, final InputStream x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBlob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setBlob(final String parameterName, 
                        final InputStream inputStream, 
                        final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBlob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setClob(final String parameterName, final Clob clob) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setClob(final String parameterName, final Reader x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setClob(final String parameterName, 
                        final Reader reader, 
                        final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setCharacterStream(final String parameterName, final Reader x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setCharacterStream

    /**
     * {@inheritDoc}
     */
    public void setCharacterStream(final String parameterName, 
                                   final Reader reader, 
                                   final int length) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setCharacterStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setCharacterStream(final String parameterName, 
                                   final Reader x, 
                                   final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setCharacterStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNClob(final String parameterName, final Reader x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNClob(final String parameterName, final NClob value) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNClob(final String parameterName, 
                         final Reader reader, 
                         final long length) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNString(final String parameterName, final String value) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNString

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNull(final String parameterName,
                        final int sqlType) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNull

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNull(final String parameterName,
                        final int sqlType,
                        final String typeName) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNull

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
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setSQLXML(final String parameterName, final SQLXML xmlObject) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setSQLXML

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setDouble(final String parameterName, 
                          final double d) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setDouble

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setFloat(final String parameterName, 
                         final float f) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setFloat

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setLong(final String parameterName, 
                        final long l) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setLong

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setInt(final String parameterName, 
                       final int i) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setInt

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setShort(final String parameterName, 
                         final short s) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setShort

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setByte(final String parameterName, 
                        final byte b) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setByte

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setBoolean(final String parameterName, 
                           final boolean b) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBoolean

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setBigDecimal(final String parameterName, 
                              final BigDecimal x) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setBigDecimal

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setString(final String parameterName, 
                          final String x) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setString

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNCharacterStream(final String parameterName, final Reader x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNCharacterStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setNCharacterStream(final String parameterName,
                                    final Reader value, 
                                    final long length) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setNCharacterStream

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setRowId(final String parameterName, final RowId x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setRowId

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setTimestamp(final String parameterName, final Timestamp x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setTimestamp

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setTimestamp(final String parameterName, 
                             final Timestamp x, 
                             final Calendar cal) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setTimestamp

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setTime(final String parameterName, 
                        final Time x) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setTime

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setTime(final String parameterName, 
                        final Time x, 
                        final Calendar cal) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setTime

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setDate(final String parameterName, 
                        final Date x) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setDate

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public void setDate(final String parameterName, 
                        final Date x, 
                        final Calendar cal) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setDate

    /**
     * {@inheritDoc}
     */
    public boolean wasNull() throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.wasNull();
    } // end of wasNull

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public Array getArray(final int parameterIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getArray

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public Array getArray(final String parameterName) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getArray

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public Ref getRef(final int parameterIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getRef

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public Ref getRef(final String parameterName) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getRef

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public Blob getBlob(final int parameterIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getBlob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public Blob getBlob(final String parameterName) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getBlob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public Clob getClob(final int parameterIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public Clob getClob(final String parameterName) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getClob

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public byte[] getBytes(final int parameterIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getBytes

    /**
     * {@inheritDoc}
     * @see java.sql.SQLFeatureNotSupportedException
     */
    public byte[] getBytes(final String parameterName) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getBytes

    /**
     * {@inheritDoc}
     */
    public Object getObject(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if
            
        return this.result.getObject(parameterIndex);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Object getObject(final String parameterName) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getObject(parameterName);
    } // end of getObject            

    /**
     * {@inheritDoc}
     */
    public Object getObject(final int parameterIndex, 
                            final Map<String, Class<?>> map) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getObject(parameterIndex, map);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Object getObject(final String parameterName, 
                            final Map<String, Class<?>> map) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getObject(parameterName, map);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public <T extends Object> T getObject(final int parameterIndex, 
                                          final Class<T> type) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getObject(parameterIndex, type);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public <T extends Object> T getObject(final String parameterName,
                                          final Class<T> type) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getObject(parameterName, type);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public String getString(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getString(parameterIndex);
    } // end of getString

    /**
     * {@inheritDoc}
     */
    public String getString(final String parameterName) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getString(parameterName);
    } // end of getString

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getBoolean(parameterIndex);
    } // end of getBoolean

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getBoolean(parameterName);
    } // end of getBoolean

    /**
     * {@inheritDoc}
     */
    public byte getByte(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getByte(parameterIndex);
    } // end of getByte

    /**
     * {@inheritDoc}
     */
    public byte getByte(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getByte(parameterName);
    } // end of getByte

    /**
     * {@inheritDoc}
     */
    public short getShort(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getShort(parameterIndex);
    } // end of getShort

    /**
     * {@inheritDoc}
     */
    public short getShort(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getShort(parameterName);
    } // end of getShort

    /**
     * {@inheritDoc}
     */
    public int getInt(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getInt(parameterIndex);
    } // end of getInt

    /**
     * {@inheritDoc}
     */
    public int getInt(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getInt(parameterName);
    } // end of getInt

    /**
     * {@inheritDoc}
     */
    public long getLong(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getLong(parameterIndex);
    } // end of getLong

    /**
     * {@inheritDoc}
     */
    public long getLong(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getLong(parameterName);
    } // end of getLong

    /**
     * {@inheritDoc}
     */
    public float getFloat(final int parameterIndex) throws SQLException {
        final Object val = getObject(parameterIndex);

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getFloat(parameterIndex);
    } // end of getFloat

    /**
     * {@inheritDoc}
     */
    public float getFloat(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getFloat(parameterName);
    } // end of getFloat

    /**
     * {@inheritDoc}
     */
    public double getDouble(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getDouble(parameterIndex);
    } // end of getDouble

    /**
     * {@inheritDoc}
     */
    public double getDouble(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getDouble(parameterName);
    } // end of getDouble

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final int parameterIndex) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getBigDecimal(parameterIndex);
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getBigDecimal(parameterName);
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final int parameterIndex,
                                    final int scale) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getBigDecimal(parameterIndex, scale);
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final String parameterName, 
                                    final int scale) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getBigDecimal(parameterName, scale);
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public Date getDate(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getDate(parameterIndex);
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Date getDate(final String parameterName) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getDate(parameterName);
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Date getDate(final int parameterIndex, 
                        final Calendar cal) throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getDate(parameterIndex, cal);
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Date getDate(final String parameterName, 
                        final Calendar cal) throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getDate(parameterName, cal);
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Time getTime(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getTime(parameterIndex);
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Time getTime(final String parameterName) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getTime(parameterName);
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Time getTime(final int parameterIndex, 
                        final Calendar cal) throws SQLException {

        return getTime(parameterIndex);
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Time getTime(final String parameterName, 
                        final Calendar cal) throws SQLException {

        return getTime(parameterName);
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final int parameterIndex) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getTimestamp(parameterIndex);
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getTimestamp(parameterName);
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final int parameterIndex, 
                                  final Calendar cal) throws SQLException {
            
        return getTimestamp(parameterIndex);
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final String parameterName, 
                                  final Calendar cal) throws SQLException {
            
        return getTimestamp(parameterName);
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public Reader getCharacterStream(final int parameterIndex) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getCharacterStream(parameterIndex);
    } // end of getCharacterStream

    /**
     * {@inheritDoc}
     */
    public Reader getCharacterStream(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getCharacterStream(parameterName);
    } // end of getCharacterStream

    /**
     * {@inheritDoc}
     */
    public Reader getNCharacterStream(final int parameterIndex) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getNCharacterStream(parameterIndex);
    } // end of getNCharacterStream

    /**
     * {@inheritDoc}
     */
    public Reader getNCharacterStream(final String parameterName) 
        throws SQLException {

        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getNCharacterStream(parameterName);
    } // end of getNCharacterStream

    /**
     * {@inheritDoc}
     */
    public RowId getRowId(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getRowId(parameterIndex);
    } // end of getRowId

    /**
     * {@inheritDoc}
     */
    public RowId getRowId(final String parameterName) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getRowId(parameterName);
    } // end of getRowId

    /**
     * {@inheritDoc}
     */
    public URL getURL(final int parameterIndex) throws SQLException {
        checkClosed();
        
        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if
        
        return this.result.getURL(parameterIndex);
    } // end of getURL

    /**
     * {@inheritDoc}
     */
    public URL getURL(final String parameterName) throws SQLException {
        checkClosed();
        
        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if
        
        return this.result.getURL(parameterName);
    } // end of getURL

    /**
     * {@inheritDoc}
     */
    public NClob getNClob(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if
        
        return this.result.getNClob(parameterIndex);
    } // end of getNClob

    /**
     * {@inheritDoc}
     */
    public NClob getNClob(final String parameterName) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if
        
        return this.result.getNClob(parameterName);
    } // end of getNClob

    /**
     * {@inheritDoc}
     */
    public String getNString(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getNString(parameterIndex);
    } // end of getNString

    /**
     * {@inheritDoc}
     */
    public String getNString(final String parameterName) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getNString(parameterName);
    } // end of getNString

    /**
     * {@inheritDoc}
     */
    public SQLXML getSQLXML(final int parameterIndex) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getSQLXML(parameterIndex);
    } // end of getSQLXML

    /**
     * {@inheritDoc}
     */
    public SQLXML getSQLXML(final String parameterName) throws SQLException {
        checkClosed();

        if (this.result == null) {
            throw new SQLException("No result");
        } // end of if

        return this.result.getSQLXML(parameterName);
    } // end of getSQLXML
} // end of class CallableStatement
