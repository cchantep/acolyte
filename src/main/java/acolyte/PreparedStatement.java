package acolyte;

import java.io.InputStream;
import java.io.Reader;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

import java.net.URL;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLXML;
import java.sql.Types;
import java.sql.Array;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Ref;

import org.apache.commons.lang3.tuple.ImmutablePair;

import acolyte.ParameterMetaData.Parameter;

import static acolyte.ParameterMetaData.Timestamp;
import static acolyte.ParameterMetaData.Numeric;
import static acolyte.ParameterMetaData.Decimal;
import static acolyte.ParameterMetaData.Default;
import static acolyte.ParameterMetaData.Double;
import static acolyte.ParameterMetaData.Float;
import static acolyte.ParameterMetaData.Short;
import static acolyte.ParameterMetaData.Date;
import static acolyte.ParameterMetaData.Time;
import static acolyte.ParameterMetaData.Bool;
import static acolyte.ParameterMetaData.Byte;
import static acolyte.ParameterMetaData.Null;
import static acolyte.ParameterMetaData.Long;
import static acolyte.ParameterMetaData.Real;
import static acolyte.ParameterMetaData.Int;
import static acolyte.ParameterMetaData.Str;

/**
 * Acolyte prepared statement.
 *
 * @author Cedric Chantepie
 */
public final class PreparedStatement 
    extends AbstractStatement implements java.sql.PreparedStatement {

    // --- Properties ---

    /**
     * Parameters
     */
    private final TreeMap<Integer,ImmutablePair<Parameter,Object>> parameters = new TreeMap<Integer,ImmutablePair<Parameter,Object>>();

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
    public void setNull(final int parameterIndex, 
                        final int sqlType) throws SQLException {

        setParam(parameterIndex, Null(sqlType), null);
    } // end of setNull

    /**
     * {@inheritDoc}
     */
    public void setBoolean(final int parameterIndex, 
                           final boolean x) throws SQLException {

        setParam(parameterIndex, Bool(), (Object)x);
    } // end of setBoolean

    /**
     * {@inheritDoc}
     */
    public void setByte(final int parameterIndex, 
                        final byte x) throws SQLException {

        setParam(parameterIndex, Byte(), (Object)x);
    } // end of setByte

    /**
     * {@inheritDoc}
     */
    public void setShort(final int parameterIndex, 
                         final short x) throws SQLException {

        setParam(parameterIndex, Short(), (Object)x);
    } // end of setShort

    /**
     * {@inheritDoc}
     */
    public void setInt(final int parameterIndex, 
                       final int x) throws SQLException {

        setParam(parameterIndex, Int(), (Object)x);
    } // end of setInt

    /**
     * {@inheritDoc}
     */
    public void setLong(final int parameterIndex, 
                        final long x) throws SQLException {

        setParam(parameterIndex, Long(), (Object)x);
    } // end of setLong

    /**
     * {@inheritDoc}
     */
    public void setFloat(final int parameterIndex, 
                         final float x) throws SQLException {

        setParam(parameterIndex, Float(x), (Object)x);
    } // end of setFloat

    /**
     * {@inheritDoc}
     */
    public void setDouble(final int parameterIndex, 
                          final double x) throws SQLException {

        setParam(parameterIndex, Double(x), (Object)x);
    } // end of setDouble

    /**
     * {@inheritDoc}
     */
    public void setBigDecimal(final int parameterIndex, 
                              final BigDecimal x) throws SQLException {

        setParam(parameterIndex, Numeric(x), (Object)x);
    } // end of setBigDecimal

    /**
     * {@inheritDoc}
     */
    public void setString(final int parameterIndex, 
                          final String x) throws SQLException {

        setParam(parameterIndex, Str(), (Object)x);
    } // end of setString

    /**
     * {@inheritDoc}
     */
    public void setBytes(final int parameterIndex, final byte[] x) 
        throws SQLException {

        throw new SQLException("Not supported");
    } // end of setBytes

    /**
     * {@inheritDoc}
     */
    public void setDate(final int parameterIndex, 
                        final Date x) throws SQLException {

        setParam(parameterIndex, Date(), (Object)x);
    } // end of setDate

    /**
     * {@inheritDoc}
     */
    public void setTime(final int parameterIndex, 
                        final Time x) throws SQLException {

        setParam(parameterIndex, Time(), (Object)x);
    } // end of setTime

    /**
     * {@inheritDoc}
     */
    public void setTimestamp(final int parameterIndex, final Timestamp x) 
        throws SQLException {

        setParam(parameterIndex, Timestamp(), (Object)x);
    } // end of setTimestamp

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
        this.parameters.clear();
    } // end of clearParameters()

    /**
     * {@inheritDoc}
     */
    public void setObject(final int parameterIndex, 
                          final Object x, 
                          final int targetSqlType) 
        throws SQLException {

        if (!Defaults.jdbcTypeMappings.containsKey(targetSqlType)) {
            throw new SQLFeatureNotSupportedException();
        } // end of if

        // ---

        if (x == null) {
            setNull(parameterIndex, targetSqlType);
            return;
        } // end of if

        // ---
        
        switch (targetSqlType) {
        case Types.DOUBLE:
            setDouble(parameterIndex, (Double)x);
            break;

        case Types.REAL:
            setReal(parameterIndex, (Float)x);
            break;

        case Types.FLOAT:
            setFloat(parameterIndex, (Float)x);
            break;

        case Types.NUMERIC: 
            setBigDecimal(parameterIndex, (BigDecimal)x); 
            break;

        case Types.DECIMAL: 
            setDecimal(parameterIndex, (BigDecimal)x); 
            break;

        default:
            setParam(parameterIndex, Default(targetSqlType), x);
            break;
        }
    } // end of setObject

    /**
     * {@inheritDoc}
     * Cannot be used with null parameter |x|.
     * @see #setObject(int,Object,int)
     */
    public void setObject(final int parameterIndex, 
                          final Object x) throws SQLException {

        if (x == null) {
            throw new SQLException("Cannot set parameter from null object");
        } // end of if

        // ---

        final String className = x.getClass().getName();

        if (!Defaults.jdbcTypeClasses.containsKey(className)) {
            System.err.println("Parameter class: " + x.getClass());

            throw new SQLFeatureNotSupportedException();
        } // end of if
        
        // ---

        final int sqlType = Defaults.jdbcTypeClasses.get(className);

        setObject(parameterIndex, x, sqlType);
    } // end of setObject

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
    public void setCharacterStream(final int parameterIndex, 
                                   final Reader reader, 
                                   final int length) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setCharacterStream

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
        final ArrayList<Parameter> params = new ArrayList<Parameter>();

        
        for (final ImmutablePair<Parameter,Object> p : parameters.values()) {
            if (p == null) {
                params.add(null);
            } else {
                params.add(p.left);
            } // end of else
        } // end of for

        return new acolyte.ParameterMetaData(params);
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

    // ---

    /**
     * Sets parameter as DECIMAL.
     */
    void setDecimal(final int parameterIndex, 
                    final BigDecimal x) throws SQLException {

        setParam(parameterIndex, Decimal(x), (Object)x);
    } // end of setBigDecimal

    /**
     * Sets parameter as REAL.
     */
    public void setReal(final int parameterIndex, 
                        final float x) throws SQLException {

        setParam(parameterIndex, Real(x), (Object)x);
    } // end of setReal

    /**
     * Set parameter
     */
    private void setParam(final int index, 
                          final Parameter meta, 
                          final Object val) {

        // Fill gap
        for (int i = index-1; 
             i > 0 && !this.parameters.containsKey(i);
             i--) { 

            this.parameters.put(i, null);
        } // end of for

        this.parameters.put(index, ImmutablePair.of(meta, val));
    } // end of setParam
} // end of PreparedStatement
