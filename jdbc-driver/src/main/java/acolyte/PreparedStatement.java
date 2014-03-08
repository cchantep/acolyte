package acolyte;

import java.io.InputStream;
import java.io.Reader;

import java.util.ArrayList;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Locale;

import java.text.SimpleDateFormat;

import java.net.URL;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.BatchUpdateException;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
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

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.ImmutablePair;

import acolyte.ParameterMetaData.ParameterDef;
import acolyte.StatementHandler.Parameter;

import static acolyte.ParameterMetaData.Timestamp;
import static acolyte.ParameterMetaData.Decimal;
import static acolyte.ParameterMetaData.Numeric;
import static acolyte.ParameterMetaData.Default;
import static acolyte.ParameterMetaData.Scaled;
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
public class PreparedStatement 
    extends AbstractStatement implements java.sql.PreparedStatement {

    // --- Shared ---

    /**
     * Date format
     */
    public static final SimpleDateFormat DATE;

    /**
     * Time format
     */
    public static final SimpleDateFormat TIME;

    /**
     * Date/Time format
     */
    public static final SimpleDateFormat DATE_TIME;

    static {
        DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        TIME = new SimpleDateFormat("HH:mm:ss.S", Locale.US);
        DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S", Locale.US);
    } // end of static

    // --- Properties ---

    /**
     * SQL statement
     */
    private final String sql;

    /**
     * Is query?
     */
    private final boolean query;

    /**
     * Parameters
     */
    private final TreeMap<Integer,Parameter> parameters = 
        new TreeMap<Integer,Parameter>();

    /**
     * Batch elements
     */
    private final ArrayList<ImmutablePair<String,TreeMap<Integer,Parameter>>> batch;

    // --- Constructors ---

    /**
     * Acolyte constructor.
     *
     * @param connection Owner connection
     * @param sql SQL statement
     * @param handler Statement handler (not null)
     */
    protected PreparedStatement(final acolyte.Connection connection,
                                final String sql,
                                final StatementHandler handler) {

        super(connection, handler);

        if (sql == null) {
            throw new IllegalArgumentException("Missing SQL");
        } // end of if

        this.sql = sql;
        this.query = handler.isQuery(sql);
        this.batch = new ArrayList<ImmutablePair<String,TreeMap<Integer,Parameter>>>();
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public ResultSet executeQuery() throws SQLException {
        checkClosed();

        if (!this.query) {
            throw new SQLException("Not a query");
        } // end of if

        // ---

        final ArrayList<Parameter> params = 
            new ArrayList<Parameter>(this.parameters.values());

        final int idx = params.indexOf(null);

        if (idx != -1) {
            throw new SQLException("Missing parameter value: " + (idx+1));
        } // end of if
        
        // ---

        try {
            final QueryResult res = this.handler.whenSQLQuery(sql, params);
            
            this.updateCount = -1;
            this.warning = res.getWarning();
            this.generatedKeys = NO_GENERATED_KEY;
            
            return (this.result = 
                    res.getRowList().resultSet().withStatement(this));

        } catch (SQLException se) {
            throw se;
        } catch (Exception e) {
            throw new SQLException(e);
        } // end of catch
    } // end of executeQuery

    /**
     * {@inheritDoc}
     */
    public int executeUpdate() throws SQLException {
        this.result = null;

        final ImmutableTriple<Integer,ResultSet,SQLWarning> res = 
            update(parameters);

        this.warning = res.right;
        this.generatedKeys = res.middle;

        return (this.updateCount = res.left);
    } // end of executeUpdate

    /**
     * Executes update.
     * @return Triple of update count, generated keys (or null) 
     * and optional warning
     */
    private ImmutableTriple<Integer,ResultSet,SQLWarning> update(final TreeMap<Integer,Parameter> parameters) throws SQLException {

        checkClosed();

        if (this.query) {
            throw new SQLException("Cannot update with query");
        } // end of if

        // ---

        final ArrayList<Parameter> params = 
            new ArrayList<Parameter>(parameters.values());

        final int idx = params.indexOf(null);

        if (idx != -1) {
            throw new SQLException("Missing parameter value: " + (idx+1));
        } // end of if

        // ---

        try {
            final UpdateResult res = this.handler.whenSQLUpdate(sql, params);
            final SQLWarning w = res.getWarning();
            final ResultSet k = (res.generatedKeys == null) 
                ? RowLists.stringList().resultSet()/* empty ResultSet */
                : res.generatedKeys.resultSet();
            
            return ImmutableTriple.of(res.getUpdateCount(), k, w);
        } catch (SQLException se) {
            throw se;
        } catch (Exception e) {
            throw new SQLException(e);
        } // end of catch
    } // end of update

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
            if ("true".equals(connection.getProperties().
                              get("acolyte.parameter.untypedNull"))) {

                // Fallback to String-VARCHAR
                setObject(parameterIndex, null, Types.VARCHAR);

                return;
            } // end of if

            throw new SQLException("Cannot set parameter from null object");
        } // end of if

        // ---

        final String className = x.getClass().getName();

        if (!Defaults.jdbcTypeClasses.containsKey(className)) {
            throw new SQLFeatureNotSupportedException("Unsupported parameter type: " + className);
        } // end of if
        
        // ---

        final int sqlType = Defaults.jdbcTypeClasses.get(className);

        setObject(parameterIndex, x, sqlType);
    } // end of setObject

    /**
     * {@inheritDoc}
     */
    public boolean execute() throws SQLException {
        if (this.query) {
            executeQuery();

            return true;
        } else {
            executeUpdate();

            return false;
        } // end of else
    } // end of execute

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLException As cannot used SQL on already prepared statement.
     */
    public void addBatch(final String sql) throws SQLException {
        throw new SQLException("Cannot add distinct SQL to prepared statement");
    } // end of addBatch

    /**
     * {@inheritDoc}
     */
    public void addBatch() throws SQLException {
        checkClosed();

        batch.add(ImmutablePair.
                  of(sql, new TreeMap<Integer,Parameter>(parameters)));

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
        for (final ImmutablePair<String,TreeMap<Integer,Parameter>> b : batch) {
            try {
                cs[i++] = update(b.right).left;
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
    public void setDate(final int parameterIndex, 
                        final Date x) throws SQLException {

        setParam(parameterIndex, Date(), (Object)x);
    } // end of setDate

    /**
     * {@inheritDoc}
     */
    public void setDate(final int parameterIndex, 
                        final Date x, 
                        final Calendar cal) throws SQLException {

        setParam(parameterIndex, Date(), 
                 (Object)ImmutablePair.of(x, cal.getTimeZone()));

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
    public void setTime(final int parameterIndex, 
                        final Time x, 
                        final Calendar cal) throws SQLException {

        setParam(parameterIndex, Time(), 
                 (Object)ImmutablePair.of(x, cal.getTimeZone()));

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
     */
    public void setTimestamp(final int parameterIndex, 
                             final Timestamp x, 
                             final Calendar cal) 
        throws SQLException {

        setParam(parameterIndex, Timestamp(), 
                 (Object)ImmutablePair.of(x, cal.getTimeZone()));

    } // end of setTimestamp

    /**
     * {@inheritDoc}
     */
    public void setNull(final int parameterIndex, 
                        final int sqlType, 
                        final String typeName) throws SQLException {

        setNull(parameterIndex, sqlType);
    } // end of setNull

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
        final ArrayList<ParameterDef> params = new ArrayList<ParameterDef>();
        
        for (final Parameter p : parameters.values()) {
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
    public void setObject(final int parameterIndex, 
                          final Object x, 
                          final int targetSqlType, 
                          final int scaleOrLength) throws SQLException {

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
        case Types.REAL:
        case Types.FLOAT:
        case Types.NUMERIC: 
        case Types.DECIMAL: 
            setParam(parameterIndex, Scaled(targetSqlType, scaleOrLength), x);
            break;

        default:
            setParam(parameterIndex, Default(targetSqlType), x);
            break;
        }
    } // end of setObject

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
     */
    public void setCharacterStream(final int parameterIndex, 
                                   final Reader reader, 
                                   final int length) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setCharacterStream

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

    // ---

    /**
     * Set parameter
     */
    private void setParam(final int index, 
                          final ParameterDef meta, 
                          final Object val) {

        // Fill gap
        for (int i = index-1; 
             i > 0 && !this.parameters.containsKey(i);
             i--) { 

            this.parameters.put(i, null);
        } // end of for

        this.parameters.put(index, Parameter.of(meta, val));
    } // end of setParam
} // end of PreparedStatement
