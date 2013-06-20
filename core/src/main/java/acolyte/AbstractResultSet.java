package acolyte;

import java.io.InputStream;
import java.io.Reader;

import java.math.BigDecimal;

import java.util.Calendar;
import java.util.Map;

import java.net.URL;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Ref;

/**
 * Acolyte base resultset.
 *
 * @author Cedric Chantepie
 */
public abstract class AbstractResultSet implements java.sql.ResultSet {

    // --- Properties ---

    /**
     * Closed?
     */
    protected boolean closed = false;

    /**
     * Fetch size
     */
    protected int fetchSize = 0;

    /**
     * Fetch direction
     */
    protected int fetchDirection = FETCH_FORWARD;

    /**
     * Current row
     */
    protected int row = 0;

    /**
     * Cursor name
     */
    protected final String cursorName;

    // --- Constructors ---

    /**
     * No-arg constructor
     */
    protected AbstractResultSet() {
        this.cursorName = 
            String.format("cursor-%d", System.identityHashCode(this));

    } // end of <init>

    /**
     * Cursor constructor.
     */
    protected AbstractResultSet(final String cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException();
        } // end of if

        this.cursorName = cursor;
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getMetaData
    
    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        this.closed = true;
    } // end of close

    /**
     * {@inheritDoc}
     */
    public boolean wasNull() throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of wasNull

    /**
     * {@inheritDoc}
     */
    public String getString(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getString

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getBoolean

    /**
     * {@inheritDoc}
     */
    public byte getByte(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getByte

    /**
     * {@inheritDoc}
     */
    public short getShort(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getShort

    /**
     * {@inheritDoc}
     */
    public int getInt(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getInt

    /**
     * {@inheritDoc}
     */
    public long getLong(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getLong

    /**
     * {@inheritDoc}
     */
    public float getFloat(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getFloat

    /**
     * {@inheritDoc}
     */
    public double getDouble(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getDouble

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final int columnIndex, 
                                    final int scale) throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public byte[] getBytes(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getBytes

    /**
     * {@inheritDoc}
     */
    public Date getDate(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Time getTime(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public InputStream getAsciiStream(final int columnIndex) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getAsciiStream

    /**
     * {@inheritDoc}
     */
    public InputStream getUnicodeStream(final int columnIndex) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getUnicodeStream

    /**
     * {@inheritDoc}
     */
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public String getString(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getString

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getBoolean

    /**
     * {@inheritDoc}
     */
    public byte getByte(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getByte

    /**
     * {@inheritDoc}
     */
    public short getShort(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public int getInt(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public long getLong(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getLong

    /**
     * {@inheritDoc}
     */
    public float getFloat(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getFloat

    /**
     * {@inheritDoc}
     */
    public double getDouble(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getDouble

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final String columnLabel, 
                                    final int scale) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public byte[] getBytes(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getBytes

    /**
     * {@inheritDoc}
     */
    public Date getDate(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Time getTime(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public InputStream getAsciiStream(final String columnLabel) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getAsciiStream

    /**
     * {@inheritDoc}
     */
    public InputStream getUnicodeStream(final String columnLabel) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getUnicodeStream

    /**
     * {@inheritDoc}
     */
    public InputStream getBinaryStream(final String columnLabel) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getBinaryStream

    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarnings() throws SQLException {
        return null;
    } // end of getWarnings

    /**
     * {@inheritDoc}
     */
    public void clearWarnings() throws SQLException {
        // Nothing to do for this base impl
    } // end of clearWarings

    /**
     * {@inheritDoc}
     */
    public String getCursorName() throws SQLException {
        return this.cursorName;
    } // end of getCursorName

    /**
     * {@inheritDoc}
     */
    public Object getObject(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Object getObject(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public int findColumn(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of findColumn

    /**
     * {@inheritDoc}
     */
    public Reader getCharacterStream(final int columnIndex) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getCharacterStream

    /**
     * {@inheritDoc}
     */
    public Reader getCharacterStream(final String columnLabel) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getCharacterStream

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final int columnIndex) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final String columnLabel) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public boolean isBeforeFirst() throws SQLException {
        return (this.row < 1);
    } // end of isBeforeFirst

    /**
     * Returns true if at/after start and at/before end.
     */
    public boolean isOn() throws SQLException {
        return (!isBeforeFirst() && !isAfterLast());
    } // end of isOn

    /**
     * {@inheritDoc}
     */
    public boolean isAfterLast() throws SQLException {
        return (this.row > this.fetchSize);
    } // end of isAfterLast

    /**
     * {@inheritDoc}
     */
    public boolean isFirst() throws SQLException {
        return (this.row == 1);
    } // end of isFirst

    /**
     * {@inheritDoc}
     */
    public boolean isLast() throws SQLException {
        return (this.row == this.fetchSize);
    } // end of isLast

    /**
     * {@inheritDoc}
     */
    public void beforeFirst() throws SQLException {
        if (this.row > 0) {
            throw new SQLException("Backward move");
        } // end of if
    } // end of beforeFirst

    /**
     * {@inheritDoc}
     */
    public void afterLast() throws SQLException {
        this.row = this.fetchSize + 1;
    } // end of afterLast

    /**
     * {@inheritDoc}
     */
    public boolean first() throws SQLException {
        return absolute(1);
    } // end of first

    /**
     * {@inheritDoc}
     */
    public boolean last() throws SQLException {
        return absolute(-1);
    } // end of last

    /**
     * {@inheritDoc}
     */
    public int getRow() throws SQLException {
        return this.row;
    } // end of getRow

    /**
     * {@inheritDoc}
     */
    public boolean absolute(final int row) throws SQLException {
        final int r = (row < 0) ? this.fetchSize+1 + row : row;

        if (r < this.row) {
            throw new SQLException("Backward move");
        } // end of if

        if (r > this.fetchSize) {
            this.row = this.fetchSize + 1;

            return false;
        } // end of if

        this.row = r;

        return true;
    } // end of absolute

    /**
     * {@inheritDoc}
     */
    public boolean relative(final int rows) throws SQLException {
        if (rows == 0) return true;
        if (rows < 0) throw new SQLException("Backward move");

        return absolute(this.row + rows);
    } // end of relative

    /**
     * {@inheritDoc}
     */
    public boolean previous() throws SQLException {
        return relative(-1);
    } // end of previous

    /**
     * {@inheritDoc}
     */
    public boolean next() throws SQLException {
        return relative(1);
    } // end of next

    /**
     * {@inheritDoc}
     */
    public void setFetchDirection(final int direction) throws SQLException {
        this.fetchDirection = direction;
    } // end of setFetchDirection

    /**
     * {@inheritDoc}
     */
    public int getFetchDirection() throws SQLException {
        return this.fetchDirection;
    } // end of getFetchDirectory

    /**
     * {@inheritDoc}
     */
    public void setFetchSize(final int rows) throws SQLException {
        this.fetchSize = rows;
    } // end of setFetchSize

    /**
     * {@inheritDoc}
     */
    public int getFetchSize() throws SQLException {
        return this.fetchSize;
    } // end of getFetchSize

    /**
     * {@inheritDoc}
     */
    public int getType() throws SQLException {
        return TYPE_FORWARD_ONLY;
    } // end of getType

    /**
     * {@inheritDoc}
     * @return ResultSet.CONCUR_READ_ONLY
     */
    public int getConcurrency() throws SQLException {
        return CONCUR_READ_ONLY;
    } // end of getConcurrency

    /**
     * {@inheritDoc}
     */
    public boolean rowUpdated() throws SQLException {
        return false;
    } // end of rowUpdated

    /**
     * {@inheritDoc}
     */
    public boolean rowInserted() throws SQLException {
        return false;
    } // end of rowInserted

    /**
     * {@inheritDoc}
     */
    public boolean rowDeleted() throws SQLException {
        return false;
    } // end of rowDeleted

    /**
     * {@inheritDoc}
     * This base implementation returns null.
     * It must be overriden returning the related statement, if any.
     */
    public Statement getStatement() throws SQLException {
        return null;
    } // end of getStatement

    /**
     * {@inheritDoc}
     */
    public Object getObject(final int columnIndex, 
                            final Map<String, Class<?>> typemap) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Ref getRef(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getRef

    /**
     * {@inheritDoc}
     */
    public Blob getBlob(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getBlob

    /**
     * {@inheritDoc}
     */
    public Clob getClob(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getClob

    /**
     * {@inheritDoc}
     */
    public Array getArray(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getArray

    /**
     * {@inheritDoc}
     */
    public Object getObject(final String columnLabel, 
                            final Map<String, Class<?>> typemap) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Ref getRef(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getRef

    /**
     * {@inheritDoc}
     */
    public Blob getBlob(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getBlob

    /**
     * {@inheritDoc}
     */
    public Clob getClob(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getClob

    /**
     * {@inheritDoc}
     */
    public Array getArray(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getArray

    /**
     * {@inheritDoc}
     */
    public Date getDate(final int columnIndex, 
                        final Calendar calendar) throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Date getDate(final String columnLabel, 
                        final Calendar calendar) throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Time getTime(final int columnIndex, 
                        final Calendar calendar) throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Time getTime(final String columnLabel, 
                        final Calendar calendar) throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final int columnIndex, 
                                  final Calendar calendar) throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final String columnLabel, 
                                  final Calendar calendar) throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public URL getURL(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public URL getURL(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getURL

    /**
     * {@inheritDoc}
     */
    public <T extends Object> T getObject(final int columnIndex, 
                                          final Class<T> type) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public <T extends Object> T getObject(final String columnLabel, 
                                          final Class<T> clazz) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public RowId getRowId(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getRowId

    /**
     * {@inheritDoc}
     */
    public RowId getRowId(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getRowId

    /**
     * {@inheritDoc}
     */
    public NClob getNClob(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public NClob getNClob(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of 

    /**
     * {@inheritDoc}
     */
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getSQLXML

    /**
     * {@inheritDoc}
     */
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getSQLXML

    /**
     * {@inheritDoc}
     */
    public String getNString(final int columnIndex) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getNString

    /**
     * {@inheritDoc}
     */
    public String getNString(final String columnLabel) throws SQLException {
        throw new RuntimeException("Not implemented");
    } // end of getNString

    /**
     * {@inheritDoc}
     */
    public Reader getNCharacterStream(final int columnIndex) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getNCharacterStream

    /**
     * {@inheritDoc}
     */
    public Reader getNCharacterStream(final String columnLabel) 
        throws SQLException {

        throw new RuntimeException("Not implemented");
    } // end of getNCharacterStream

    /**
     * {@inheritDoc}
     */
    public int getHoldability() throws SQLException {
        return CLOSE_CURSORS_AT_COMMIT;
    } // end of getHoldability

    /**
     * {@inheritDoc}
     */
    public boolean isClosed() throws SQLException {
        return this.closed;
    } // end of isClosed

    /**
     * {@inheritDoc}
     */
    public void updateNull(final int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    } // end of updateNull

    /**
     * {@inheritDoc}
     */
    public void updateBoolean(final int columnIndex, 
                              final boolean b) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBoolean

    /**
     * {@inheritDoc}
     */
    public void updateByte(final int columnIndex, 
                           final byte b) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateByte

    /**
     * {@inheritDoc}
     */
    public void updateShort(final int columnIndex, 
                            final short s) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateShort

    /**
     * {@inheritDoc}
     */
    public void updateInt(final int columnIndex, 
                          final int i) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateInt

    /**
     * {@inheritDoc}
     */
    public void updateLong(final int columnIndex, 
                           final long l) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateLong

    /**
     * {@inheritDoc}
     */
    public void updateFloat(final int columnIndex, 
                            final float f) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateFloat

    /**
     * {@inheritDoc}
     */
    public void updateDouble(final int columnIndex, 
                             final double d) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateDouble

    /**
     * {@inheritDoc}
     */
    public void updateBigDecimal(final int columnIndex, 
                                 final BigDecimal bd) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBigDecimal

    /**
     * {@inheritDoc}
     */
    public void updateString(final int columnIndex, 
                             final String str) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateString

    /**
     * {@inheritDoc}
     */
    public void updateBytes(final int columnIndex, 
                            final byte[] bin) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBytes

    /**
     * {@inheritDoc}
     */
    public void updateDate(final int columnIndex, 
                           final Date date) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateDate

    /**
     * {@inheritDoc}
     */
    public void updateTime(final int columnIndex, 
                           final Time t) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateTime

    /**
     * {@inheritDoc}
     */
    public void updateTimestamp(final int columnIndex, 
                                final Timestamp ts) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateTimestamp

    /**
     * {@inheritDoc}
     */
    public void updateAsciiStream(final int columnIndex, 
                                  final InputStream x, 
                                  final int length) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateAsciiStream

    /**
     * {@inheritDoc}
     */
    public void updateBinaryStream(final int columnIndex, 
                                   final InputStream x, 
                                   final int length) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBinaryStream

    /**
     * {@inheritDoc}
     */
    public void updateCharacterStream(final int columnIndex, 
                                      final Reader r, 
                                      final int length) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateObject(final int columnIndex, 
                             final Object o, 
                             final int scale) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateObject

    /**
     * {@inheritDoc}
     */
    public void updateObject(final int columnIndex, 
                             final Object o) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateObject

    /**
     * {@inheritDoc}
     */
    public void updateNull(final String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    } // end of updateNull

    /**
     * {@inheritDoc}
     */
    public void updateBoolean(final String columnLabel, 
                              final boolean b) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBoolean

    /**
     * {@inheritDoc}
     */
    public void updateByte(final String columnLabel, 
                           final byte b) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateByte

    /**
     * {@inheritDoc}
     */
    public void updateShort(final String columnLabel, 
                            final short s) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateShort

    /**
     * {@inheritDoc}
     */
    public void updateInt(final String columnLabel, 
                          final int i) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateInt

    /**
     * {@inheritDoc}
     */
    public void updateLong(final String columnLabel, 
                           final long l) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateLong

    /**
     * {@inheritDoc}
     */
    public void updateFloat(final String columnLabel, 
                            final float f) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateFloat

    /**
     * {@inheritDoc}
     */
    public void updateDouble(final String columnLabel, 
                             final double d) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateDouble

    /**
     * {@inheritDoc}
     */
    public void updateBigDecimal(final String columnLabel, 
                                 final BigDecimal bd) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBigDecimal

    /**
     * {@inheritDoc}
     */
    public void updateString(final String columnLabel, 
                             final String str) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateString

    /**
     * {@inheritDoc}
     */
    public void updateBytes(final String columnLabel, 
                            final byte[] b) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBytes

    /**
     * {@inheritDoc}
     */
    public void updateDate(final String columnLabel, 
                           final Date d) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateDate

    /**
     * {@inheritDoc}
     */
    public void updateTime(final String columnLabel, 
                           final Time t) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateTime

    /**
     * {@inheritDoc}
     */
    public void updateTimestamp(final String columnLabel, 
                                final Timestamp ts) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateTimestamp

    /**
     * {@inheritDoc}
     */
    public void updateAsciiStream(final String columnLabel, 
                                  final InputStream x, 
                                  final int length) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateAsciiStream

    /**
     * {@inheritDoc}
     */
    public void updateBinaryStream(final String columnLabel, 
                                   final InputStream x, 
                                   final int length) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBinaryStream

    /**
     * {@inheritDoc}
     */
    public void updateCharacterStream(final String columnLabel, 
                                      final Reader reader, 
                                      final int length) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateObject(final String columnLabel, 
                             final Object o, 
                             final int scale) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateObject

    /**
     * {@inheritDoc}
     */
    public void updateObject(final String columnLabel, 
                             final Object o) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateObject

    /**
     * {@inheritDoc}
     */
    public void insertRow() throws SQLException {
        throw new UnsupportedOperationException();
    } // end of insertRow

    /**
     * {@inheritDoc}
     */
    public void updateRow() throws SQLException {
        throw new UnsupportedOperationException();
    } // end of updateRow

    /**
     * {@inheritDoc}
     */
    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException();
    } // end of deleteRow

    /**
     * {@inheritDoc}
     */
    public void refreshRow() throws SQLException {
        throw new UnsupportedOperationException();
    } // end of refreshRow

    /**
     * {@inheritDoc}
     */
    public void cancelRowUpdates() throws SQLException {
        throw new UnsupportedOperationException();
    } // end of cancelRowUpdates

    /**
     * {@inheritDoc}
     */
    public void moveToInsertRow() throws SQLException {
        throw new UnsupportedOperationException();
    } // end of moveToInsertRow

    /**
     * {@inheritDoc}
     */
    public void moveToCurrentRow() throws SQLException {
        throw new UnsupportedOperationException();
    } // end of moveToCurrentRow

    /**
     * {@inheritDoc}
     */
    public void updateRef(final int columnIndex, 
                          final Ref ref) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateRef

    /**
     * {@inheritDoc}
     */
    public void updateRef(final String columnLabel, 
                          final Ref ref) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateRef

    /**
     * {@inheritDoc}
     */
    public void updateBlob(final int columnIndex, 
                           final Blob b) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBlob

    /**
     * {@inheritDoc}
     */
    public void updateBlob(final String columnLabel, 
                           final Blob b) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBlob

    /**
     * {@inheritDoc}
     */
    public void updateClob(final int columnIndex, 
                           final Clob c) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateClob

    /**
     * {@inheritDoc}
     */
    public void updateClob(final String columnLabel, 
                           final Clob c) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateClob

    /**
     * {@inheritDoc}
     */
    public void updateArray(final int columnIndex, 
                            final Array array) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateArray

    /**
     * {@inheritDoc}
     */
    public void updateArray(final String columnLabel, 
                            final Array array) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateArray

    /**
     * {@inheritDoc}
     */
    public void updateRowId(final int columnIndex, 
                            final RowId rid) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateRowId

    /**
     * {@inheritDoc}
     */
    public void updateRowId(final String columnLabel, 
                            final RowId rid) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateRowId

    /**
     * {@inheritDoc}
     */
    public void updateNString(final int columnIndex, 
                              final String str) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNString

    /**
     * {@inheritDoc}
     */
    public void updateNString(final String columnLabel, 
                              final String str) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNString

    /**
     * {@inheritDoc}
     */
    public void updateNClob(final int columnIndex, 
                            final NClob c) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNClob

    /**
     * {@inheritDoc}
     */
    public void updateNClob(final String columnLabel, 
                            final NClob c) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNClob

    /**
     * {@inheritDoc}
     */
    public void updateSQLXML(final int columnIndex, 
                             final SQLXML x) throws SQLException {
        throw new UnsupportedOperationException();
    } // end of updateSQLXML

    /**
     * {@inheritDoc}
     */
    public void updateSQLXML(final String columnLabel, 
                             final SQLXML x) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateSQLXML

    /**
     * {@inheritDoc}
     */
    public void updateNCharacterStream(final int columnIndex, 
                                       final Reader reader, 
                                       final long length) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateNCharacterStream(final String columnLabel, 
                                       final Reader reader, 
                                       final long length) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateAsciiStream(final int columnIndex, 
                                  final InputStream x, 
                                  final long length) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updayeAsciiStream

    /**
     * {@inheritDoc}
     */
    public void updateBinaryStream(final int columnIndex,
                                   final InputStream x,
                                   final long length) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBinaryStream

    /**
        * {@inheritDoc}
    */
    public void updateCharacterStream(final int columnIndex,
                                      final Reader reader,
                                      final long length) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateAsciiStream(final String columnLabel,
                                  final InputStream x,
                                  final long length) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateAsciiStream

    /**
     * {@inheritDoc}
     */
    public void updateBinaryStream(final String columnLabel,
                                   final InputStream x,
                                   final long length) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBinaryStream

    /**
     * {@inheritDoc}
     */
    public void updateCharacterStream(final String columnLabel,
                                      final Reader reader,
                                      final long length) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateBlob(final int columnIndex,
                           final InputStream x,
                           final long length) throws SQLException {
        throw new UnsupportedOperationException();
    } // end of updateBlob

    /**
     * {@inheritDoc}
     */
    public void updateBlob(final String columnLabel,
                           final InputStream x,
                           final long length) throws SQLException {
        throw new UnsupportedOperationException();
    } // end of updateBlob

    /**
     * {@inheritDoc}
     */
    public void updateClob(final int columnIndex,
                           final Reader reader,
                           final long length) throws SQLException {
        throw new UnsupportedOperationException();
    } // end of updateClob

    /**
     * {@inheritDoc}
     */
    public void updateClob(final String columnLabel,
                           final Reader reader,
                           final long length) throws SQLException {
        throw new UnsupportedOperationException();
    } // end of updateClob

    /**
     * {@inheritDoc}
     */
    public void updateNClob(final int columnIndex,
                            final Reader reader,
                            final long length) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNClob

    /**
     * {@inheritDoc}
     */
    public void updateNClob(final String columnLabel,
                            final Reader reader,
                            final long length) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNClob

    /**
     * {@inheritDoc}
     */
    public void updateNCharacterStream(final int columnIndex, 
                                       final Reader reader) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateNCharacterStream(final String columnLabel, 
                                       final Reader reader) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateAsciiStream(final int columnIndex,
                                  final InputStream x) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateAsciiStream

    /**
     * {@inheritDoc}
     */
    public void updateBinaryStream(final int columnIndex,
                                   final InputStream x) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBinaryStream

    /**
     * {@inheritDoc}
     */
    public void updateCharacterStream(final int columnIndex,
                                      final Reader reader) throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateAsciiStream(final String columnLabel, final InputStream x)
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateAsciiStream

    /**
     * {@inheritDoc}
     */
    public void updateBinaryStream(final String columnLabel, 
                                   final InputStream x)
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBinaryStream

    /**
     * {@inheritDoc}
     */
    public void updateCharacterStream(final String columnLabel, 
                                      final Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    } // end of updateCharacterStream

    /**
     * {@inheritDoc}
     */
    public void updateBlob(final int columnIndex, final InputStream x) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBlob

    /**
     * {@inheritDoc}
     */
    public void updateBlob(final String columnLabel, final InputStream x) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateBlob

    /**
     * {@inheritDoc}
     */
    public void updateClob(final int columnIndex, final Reader reader) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateClob

    /**
     * {@inheritDoc}
     */
    public void updateClob(final String columnLabel, final Reader reader) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateClob

    /**
     * {@inheritDoc}
     */
    public void updateNClob(final int columnIndex, final Reader reader) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNClob

    /**
     * {@inheritDoc}
     */
    public void updateNClob(final String columnLabel, final Reader reader) 
        throws SQLException {

        throw new UnsupportedOperationException();
    } // end of updateNClob

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
     * Throws a SQLException("Result set is closed") if connection is closed.
     */
    protected void checkClosed() throws SQLException {
        if (this.closed) {
            throw new SQLException("Result set is closed");
        } // end of if
    } // end of checkClosed
} // end of class AbstractResultSet
