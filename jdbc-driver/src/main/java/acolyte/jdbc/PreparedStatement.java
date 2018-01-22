package acolyte.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.ImmutablePair;

import acolyte.jdbc.ParameterMetaData.ParameterDef;
import acolyte.jdbc.StatementHandler.Parameter;

import static acolyte.jdbc.ParameterMetaData.Timestamp;
import static acolyte.jdbc.ParameterMetaData.Decimal;
import static acolyte.jdbc.ParameterMetaData.Numeric;
import static acolyte.jdbc.ParameterMetaData.Default;
import static acolyte.jdbc.ParameterMetaData.Scaled;
import static acolyte.jdbc.ParameterMetaData.Binary;
import static acolyte.jdbc.ParameterMetaData.Double;
import static acolyte.jdbc.ParameterMetaData.Float;
import static acolyte.jdbc.ParameterMetaData.Short;
import static acolyte.jdbc.ParameterMetaData.Date;
import static acolyte.jdbc.ParameterMetaData.Time;
import static acolyte.jdbc.ParameterMetaData.Bool;
import static acolyte.jdbc.ParameterMetaData.Byte;
import static acolyte.jdbc.ParameterMetaData.Null;
import static acolyte.jdbc.ParameterMetaData.Long;
import static acolyte.jdbc.ParameterMetaData.Real;
import static acolyte.jdbc.ParameterMetaData.Int;
import static acolyte.jdbc.ParameterMetaData.Str;

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
     * Generated key flag
     */
    private final int generatedKeysFlag;

    /**
     * Parameters
     */
    private final TreeMap<Integer,Parameter> parameters = 
        new TreeMap<Integer,Parameter>();

    /**
     * Batch elements
     */
    private final ArrayList<ImmutablePair<String,TreeMap<Integer,Parameter>>> batch;

    /**
     * Names of the column to be considered within the generated keys
     * (or null)
     */
    private final String[] generatedKeysColumnNames;

    /**
     * Indexes of the column to be considered within the generated keys
     */
    private final int[] generatedKeysColumnIndexes;

    // --- Constructors ---

    /**
     * Acolyte constructor.
     *
     * @param connection Owner connection
     * @param sql SQL statement
     * @param generatedKeys Generated keys flag
     * @param handler Statement handler (not null)
     * @deprecated Use the constructor with generatedKeysColumn{Names,Indexes}
     */
    @Deprecated
    protected PreparedStatement(final acolyte.jdbc.Connection connection,
                                final String sql,
                                final int generatedKeys,
                                final StatementHandler handler) {

        this(connection, sql, generatedKeys, null, null, handler);
    } // end of <init>

    /**
     * Acolyte constructor.
     *
     * @param connection Owner connection
     * @param sql SQL statement
     * @param generatedKeys Generated keys flag
     * @param generatedKeysColumnNames Column names for the generated keys
     * @param generatedKeysColumnIndexes Column indexes for the generated keys
     * @param handler Statement handler (not null)
     */
    protected PreparedStatement(final acolyte.jdbc.Connection connection,
                                final String sql,
                                final int generatedKeys,
                                final String[] generatedKeysColumnNames,
                                final int[] generatedKeysColumnIndexes,
                                final StatementHandler handler) {

        super(connection, handler);

        if (sql == null) {
            throw new IllegalArgumentException("Missing SQL");
        } // end of if

        this.sql = sql;
        this.query = handler.isQuery(sql);
        this.generatedKeysFlag = generatedKeys;
        this.generatedKeysColumnNames = generatedKeysColumnNames;
        this.generatedKeysColumnIndexes = generatedKeysColumnIndexes;

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

            // Not an update, so no update count or generated keys
            this.updateCount = -1;
            this.generatedKeys = EMPTY_GENERATED_KEYS.withStatement(this);

            this.warning = res.getWarning();
            
            return (this.result = res.getRowList().resultSet().
                withStatement(this).withWarning(this.warning));

        } catch (SQLException se) {
            throw se;
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
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
     * Returns the resultset corresponding to the keys generated on update.
     */
    private ResultSet generateKeysResultSet(final UpdateResult res) {
        if (this.generatedKeysColumnIndexes == null &&
            this.generatedKeysColumnNames == null) {

            return res.generatedKeys.resultSet().withStatement(this);
        } else if (this.generatedKeysColumnIndexes != null) {
            return res.generatedKeys.resultSet().withStatement(this).
                withProjection(this.generatedKeysColumnIndexes);

        } else {
            return res.generatedKeys.resultSet().withStatement(this).
                withProjection(this.generatedKeysColumnNames);
        }
    } // end of generateKeysResultSet

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
                ? EMPTY_GENERATED_KEYS.withStatement(this)
                : generateKeysResultSet(res);
            
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
    public void setNull(final int parameterIndex, 
                        final int sqlType) throws SQLException {

        setParam(parameterIndex, Null(sqlType), null);
    } // end of setNull

    /**
     * {@inheritDoc}
     */
    public void setBoolean(final int parameterIndex, 
                           final boolean x) throws SQLException {

        setParam(parameterIndex, Bool, x);
    } // end of setBoolean

    /**
     * {@inheritDoc}
     */
    public void setByte(final int parameterIndex, 
                        final byte x) throws SQLException {

        setParam(parameterIndex, Byte, x);
    } // end of setByte

    /**
     * {@inheritDoc}
     */
    public void setShort(final int parameterIndex, 
                         final short x) throws SQLException {

        setParam(parameterIndex, Short, x);
    } // end of setShort

    /**
     * {@inheritDoc}
     */
    public void setInt(final int parameterIndex, 
                       final int x) throws SQLException {

        setParam(parameterIndex, Int, x);
    } // end of setInt

    /**
     * {@inheritDoc}
     */
    public void setLong(final int parameterIndex, 
                        final long x) throws SQLException {

        setParam(parameterIndex, Long, x);
    } // end of setLong

    /**
     * {@inheritDoc}
     */
    public void setFloat(final int parameterIndex, 
                         final float x) throws SQLException {

        setParam(parameterIndex, Float(x), x);
    } // end of setFloat

    /**
     * {@inheritDoc}
     */
    public void setDouble(final int parameterIndex, 
                          final double x) throws SQLException {

        setParam(parameterIndex, Double(x), x);
    } // end of setDouble

    /**
     * {@inheritDoc}
     */
    public void setBigDecimal(final int parameterIndex, 
                              final BigDecimal x) throws SQLException {

        final ParameterDef def = (x == null) ? Numeric : Numeric(x);
            
        setParam(parameterIndex, def, x);
    } // end of setBigDecimal

    /**
     * {@inheritDoc}
     */
    public void setString(final int parameterIndex, 
                          final String x) throws SQLException {

        setParam(parameterIndex, Str, x);
    } // end of setString

    /**
     * {@inheritDoc}
     */
    public void setBytes(final int parameterIndex, final byte[] x) 
        throws SQLException {

        setParam(parameterIndex, Binary, x);
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
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public void setUnicodeStream(final int parameterIndex, 
                                 final InputStream x, 
                                 final int length) throws SQLException {
        
        throw new SQLFeatureNotSupportedException();
    } // end of setUnicodeStream
    
    /**
     * {@inheritDoc}
     */
    public void setBinaryStream(final int parameterIndex, 
                                final InputStream x, 
                                final int length) throws SQLException {

        setBinaryStream(parameterIndex, x, (long) length);
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

        final String className = normalizeClassName(x.getClass());

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
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public void setRef(final int parameterIndex, final Ref x) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setRef

    /**
     * {@inheritDoc}
     */
    public void setBlob(final int parameterIndex, final Blob x) 
        throws SQLException {

        setParam(parameterIndex, acolyte.jdbc.ParameterMetaData.Blob, x);
    } // end of setBlob

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public void setClob(final int parameterIndex, final Clob clob) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of setClob

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public void setArray(final int parameterIndex, final Array x) 
        throws SQLException {

        setParam(parameterIndex, acolyte.jdbc.ParameterMetaData.Array, x);
    } // end of setArray

    /**
     * {@inheritDoc}
     * @throws java.sql.SQLFeatureNotSupportedException Not currently supported
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getMetaData

    /**
     * {@inheritDoc}
     */
    public void setDate(final int parameterIndex, 
                        final Date x) throws SQLException {

        setParam(parameterIndex, Date, x);
    } // end of setDate

    /**
     * {@inheritDoc}
     */
    public void setDate(final int parameterIndex, 
                        final Date x, 
                        final Calendar cal) throws SQLException {

        setParam(parameterIndex, Date, 
                 (Object)ImmutablePair.of(x, cal.getTimeZone()));

    } // end of setDate

    /**
     * {@inheritDoc}
     */
    public void setTime(final int parameterIndex, 
                        final Time x) throws SQLException {

        setParam(parameterIndex, Time, x);
    } // end of setTime

    /**
     * {@inheritDoc}
     */
    public void setTime(final int parameterIndex, 
                        final Time x, 
                        final Calendar cal) throws SQLException {

        setParam(parameterIndex, Time, 
                 (Object)ImmutablePair.of(x, cal.getTimeZone()));

    } // end of setTime

    /**
     * {@inheritDoc}
     */
    public void setTimestamp(final int parameterIndex, final Timestamp x) 
        throws SQLException {

        setParam(parameterIndex, Timestamp, x);
    } // end of setTimestamp

    /**
     * {@inheritDoc}
     */
    public void setTimestamp(final int parameterIndex, 
                             final Timestamp x, 
                             final Calendar cal) 
        throws SQLException {

        setParam(parameterIndex, Timestamp, 
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

        return new acolyte.jdbc.ParameterMetaData(params);
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
     */
    public void setBlob(final int parameterIndex, 
                        final InputStream inputStream, 
                        final long length) throws SQLException {

        setBlob(parameterIndex, createBlob(inputStream, length));
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
     */
    public void setBinaryStream(final int parameterIndex, 
                                final InputStream x, 
                                final long length) throws SQLException {

        setBytes(parameterIndex, createBytes(x, length));
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
     */
    public void setBinaryStream(final int parameterIndex, final InputStream x) 
        throws SQLException {

        setBytes(parameterIndex, createBytes(x, -1));
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
     *
     * @throws SQLException if fails to set the big decimal value
     */
    void setDecimal(final int parameterIndex, 
                    final BigDecimal x) throws SQLException {

        final ParameterDef def = (x == null) ? Decimal : Decimal(x);

        setParam(parameterIndex, def, x);
    } // end of setBigDecimal

    /**
     * Sets parameter as REAL.
     *
     * @param parameterIndex the index of the float parameter
     * @param x the float value
     * @throws SQLException if fails to set the float value
     */
    public void setReal(final int parameterIndex, 
                        final float x) throws SQLException {

        setParam(parameterIndex, Real(x), x);
    } // end of setReal

    // ---

    /**
     * Set parameter
     *
     * @param inded the index of the parameter
     * @param meta the metadata for the parameter
     * @param val the parameter value
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

    /**
     * Normalizes parameter class name.
     */
    private String normalizeClassName(final Class<?> c) {
        if (Blob.class.isAssignableFrom(c)) return "java.sql.Blob";
        else if (Array.class.isAssignableFrom(c)) return "java.sql.Array";
        
        return c.getName();
    } // end of normalizeClassName

    /**
     * Creates bytes array from input stream.
     *
     * @param stream Input stream
     * @param length
     */
    private byte[] createBytes(InputStream stream, long length) 
        throws SQLException {

        ByteArrayOutputStream buff = null;

        try {
            buff = new ByteArrayOutputStream();

            if (length > 0) IOUtils.copyLarge(stream, buff, 0, length);
            else IOUtils.copy(stream, buff);

            return buff.toByteArray();
        } catch (IOException e) {
            throw new SQLException("Fails to create BLOB", e);
        } finally {
            IOUtils.closeQuietly(buff);
        } // end of finally
    } // end of createBytes

    /**
     * Creates BLOB from input stream.
     *
     * @param stream Input stream
     * @param length
     */
    private acolyte.jdbc.Blob createBlob(InputStream stream, long length) 
        throws SQLException {

        final acolyte.jdbc.Blob blob = acolyte.jdbc.Blob.Nil();

        blob.setBytes(0L, createBytes(stream, length));

        return blob;
    } // end of createBlob
} // end of PreparedStatement
