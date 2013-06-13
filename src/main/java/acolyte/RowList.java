package acolyte;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.math.BigDecimal;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;

import org.apache.commons.lang3.tuple.ImmutablePair;

import acolyte.Row.Column;
import acolyte.Row.Row1;

/**
 * Type-safe list of row.
 *
 * @author Cedric Chantepie
 */
public class RowList<R extends Row> {
    // --- Properties ---

    /**
     * Rows
     */
    private final List<R> rows;

    // --- Constructors ---

    /**
     * No-arg constructor.
     */
    public RowList() {
        this(new ArrayList<R>());
    } // end of <init>

    /**
     * Bulk constructor.
     *
     * @throws IllegalArgumentException if rows is null
     */
    protected RowList(final List<R> rows) {
        if (rows == null) {
            throw new IllegalArgumentException();
        } // end of if

        this.rows = Collections.unmodifiableList(rows);
    } // end of <init>

    // ---

    /**
     * Appends |row|.
     *
     * @return Update row list
     */
    public RowList<R> append(final R row) {
        final ArrayList<R> copy = new ArrayList<R>(this.rows);

        copy.add(row);

        return new RowList<R>(copy);
    } // end of append

    /**
     * Returns result set from these rows.
     */
    public AbstractResultSet resultSet() {
        return new RowResultSet<R>(this.rows);
    } // end of resultSet

    // --- Row creation ---

    /**
     * Creates a row with 1 unnamed cell.
     */
    public static <A> Row.Row1<A> row1(final A c1) {
        return new Row.Row1<A>(c1, null);
    } // end of row1

    /**
     * Creates a row with 1 named cell.
     *
     * @param n1 Name of cell (or null)
     */
    public static <A> Row.Row1<A> row1(final A c1, final String n1) {
        return new Row.Row1<A>(c1, n1);
    } // end of row1

    /**
     * Creates a row with 2 unnamed cells.
     */
    public static <A,B> Row.Row2<A,B> row2(final A c1, final B c2) {
        return new Row.Row2<A,B>(c1, null, c2, null);
    } // end of row2

    /**
     * Creates a row with 2 named cells.
     *
     * @param n1 Name of cell #1
     * @param n2 Name of cell #2
     */
    public static <A,B> Row.Row2<A,B> row2(final A c1, final String n1,
                                           final B c2, final String n2) {

        return new Row.Row2<A,B>(c1, n1, c2, n2);
    } // end of row2

    /**
     * Creates a row with 3 unnamed cells.
     */
    public static <A,B,C> Row.Row3<A,B,C> row3(final A c1, 
                                               final B c2, 
                                               final C c3) {

        return new Row.Row3<A,B,C>(c1, null, c2, null, c3, null);
    } // end of row3

    /**
     * Creates a row with 3 named cells.
     *
     * @param n1 Name of cell #1
     * @param n2 Name of cell #2
     * @param n3 Name of cell #3
     */
    public static <A,B,C> Row.Row3<A,B,C> row3(final A c1, final String n1,
                                               final B c2, final String n2,
                                               final C c3, final String n3) {

        return new Row.Row3<A,B,C>(c1, n1, c2, n2, c3, n3);
    } // end of row3

    // --- Inner classes ---

    /**
     * Result set made from list of row.
     *
     * @param R Row
     */
    private final class RowResultSet<R extends Row> extends AbstractResultSet {
        final List<R> rows;
        private Column<? extends Object> last;

        // --- Constructors ---

        /**
         * Constructor
         */
        protected RowResultSet(final List<R> rows) {
            if (rows == null) {
                // Impossible
                throw new IllegalArgumentException();
            } // end of if

            this.rows = rows;
            this.last = null;
            super.fetchSize = rows.size();
        } // end of <init>

        // --- ResultSet implementation ---
        
        /**
         * {@inheritDoc}
         */
        public void setFetchSize(final int rows) throws SQLException {
            throw new UnsupportedOperationException();
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
        public boolean wasNull() throws SQLException {
            checkClosed();

            return (this.last != null && this.last.value == null);
        } // end of wasNull

        /**
         * {@inheritDoc}
         */
        public Object getObject(final int columnIndex) throws SQLException {
            checkClosed();

            if (!isOn()) {
                throw new SQLException("Not on a row");
            } // end of if

            final int idx = columnIndex - 1;
            final List<Object> cells = this.rows.get(this.row-1).cells();

            if (idx < 0 || idx >= cells.size()) {
                throw new SQLException("Invalid column index: " + columnIndex);
            } // end of if

            // ---

            final Object val = cells.get(idx);

            this.last = new Column<Object>(val);
            
            return val;
        } // end of getObject

        /**
         * {@inheritDoc}
         */
        public Object getObject(final String columnLabel) throws SQLException {
            checkClosed();

            if (!isOn()) {
                throw new SQLException("Not on a row");
            } // end of if

            if (columnLabel == null) {
                throw new SQLException("Invalid label: " + columnLabel);
            } // end of if

            // ---

            final Column<?> col = this.rows.get(this.row-1).cell(columnLabel);

            if (col == null) {
                throw new SQLException("Invalid label: " + columnLabel);
            } // end of if

            // ---

            this.last = col;
            
            return col.value;
        } // end of getObject            

        /**
         * {@inheritDoc}
         */
        public String getString(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return null;
            } // end of if

            if (val instanceof String) {
                return (String) val;
            } // end of if

            // ---

            return String.valueOf(val);
        } // end of getString

        /**
         * {@inheritDoc}
         */
        public String getString(final String columnLabel) throws SQLException {
            final Object val = getObject(columnLabel);

            if (val == null) {
                return null;
            } // end of if

            if (val instanceof String) {
                return (String) val;
            } // end of if

            // ---

            return String.valueOf(val);
        } // end of getString

        /**
         * {@inheritDoc}
         */
        public boolean getBoolean(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return false;
            } // end of if

            if (val instanceof Boolean) {
                return (Boolean) val;
            } // end of if

            return (val.toString().charAt(0) != '0');
        } // end of getBoolean

        /**
         * {@inheritDoc}
         */
        public boolean getBoolean(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return false;
            } // end of if

            if (val instanceof Boolean) {
                return (Boolean) val;
            } // end of if

            return (val.toString().charAt(0) != '0');
        } // end of getBoolean

        /**
         * {@inheritDoc}
         */
        public byte getByte(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).byteValue();
            } // end of if

            return -1;
        } // end of getByte

        /**
         * {@inheritDoc}
         */
        public byte getByte(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).byteValue();
            } // end of if

            return -1;
        } // end of getByte

        /**
         * {@inheritDoc}
         */
        public short getShort(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).shortValue();
            } // end of if

            return -1;
        } // end of getShort

        /**
         * {@inheritDoc}
         */
        public short getShort(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).shortValue();
            } // end of if

            return -1;
        } // end of getShort

        /**
         * {@inheritDoc}
         */
        public int getInt(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).intValue();
            } // end of if

            return -1;
        } // end of getInt

        /**
         * {@inheritDoc}
         */
        public int getInt(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).intValue();
            } // end of if

            return -1;
        } // end of getInt

        /**
         * {@inheritDoc}
         */
        public long getLong(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).longValue();
            } // end of if

            return -1;
        } // end of getLong

        /**
         * {@inheritDoc}
         */
        public long getLong(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).longValue();
            } // end of if

            return -1;
        } // end of getLong

        /**
         * {@inheritDoc}
         */
        public float getFloat(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).floatValue();
            } // end of if

            return -1;
        } // end of getFloat

        /**
         * {@inheritDoc}
         */
        public float getFloat(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).floatValue();
            } // end of if

            return -1;
        } // end of getFloat

        /**
         * {@inheritDoc}
         */
        public double getDouble(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            } // end of if

            return -1;
        } // end of getDouble

        /**
         * {@inheritDoc}
         */
        public double getDouble(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return 0;
            } // end of if

            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            } // end of if

            return -1;
        } // end of getDouble

        /**
         * {@inheritDoc}
         */
        public BigDecimal getBigDecimal(final int columnIndex) 
            throws SQLException {

            final Object val = getObject(columnIndex);

            if (val == null) {
                return null;
            } // end of if

            if (val instanceof BigDecimal) {
                return (BigDecimal) val;
            } // end of if

            if (val instanceof Number) {
                return new BigDecimal(val.toString());
            } // end of if

            throw new SQLException("Not a BigDecimal: " + columnIndex);
        } // end of getBigDecimal

        /**
         * {@inheritDoc}
         */
        public BigDecimal getBigDecimal(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return null;
            } // end of if

            if (val instanceof BigDecimal) {
                return (BigDecimal) val;
            } // end of if

            if (val instanceof Number) {
                return new BigDecimal(val.toString());
            } // end of if

            throw new SQLException("Not a BigDecimal: " + columnLabel);
        } // end of getBigDecimal

        /**
         * {@inheritDoc}
         */
        public BigDecimal getBigDecimal(final int columnIndex,
                                        final int scale) 
            throws SQLException {

            final Object val = getObject(columnIndex);

            if (val == null) {
                return null;
            } // end of if

            final BigDecimal bd = (val instanceof BigDecimal) 
                ? (BigDecimal) val
                : (val instanceof Number) 
                ? new BigDecimal(val.toString())
                : null;

            if (bd != null) {
                return bd.setScale(scale, BigDecimal.ROUND_DOWN);
            } // end of if

            throw new SQLException("Not a BigDecimal: " + columnIndex);
        } // end of getBigDecimal

        /**
         * {@inheritDoc}
         */
        public BigDecimal getBigDecimal(final String columnLabel, 
                                        final int scale) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return null;
            } // end of if

            final BigDecimal bd = (val instanceof BigDecimal) 
                ? (BigDecimal) val
                : (val instanceof Number) 
                ? new BigDecimal(val.toString())
                : null;

            if (bd != null) {
                return bd.setScale(scale, BigDecimal.ROUND_DOWN);
            } // end of if

            throw new SQLException("Not a BigDecimal: " + columnLabel);
        } // end of getBigDecimal

        /**
         * {@inheritDoc}
         */
        public Date getDate(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return null;
            } // end of if

            // ---

            if (val instanceof Date) {
                return (Date) val;
            } // end of if

            if (val instanceof java.util.Date) {
                return new Date(((java.util.Date) val).getTime());
            } // end of if

            throw new SQLException("Not a Date: " + columnIndex);
        } // end of getDate

        /**
         * {@inheritDoc}
         */
        public Date getDate(final String columnLabel) throws SQLException {
            final Object val = getObject(columnLabel);

            if (val == null) {
                return null;
            } // end of if

            // ---

            if (val instanceof Date) {
                return (Date) val;
            } // end of if

            if (val instanceof java.util.Date) {
                return new Date(((java.util.Date) val).getTime());
            } // end of if

            throw new SQLException("Not a Date: " + columnLabel);
        } // end of getDate

        /**
         * {@inheritDoc}
        public Date getDate(final int columnIndex, 
                            final Calendar cal) throws SQLException {

            final Object val = getObject(columnIndex);

            if (val == null) {
                return null;
            } // end of if

            // ---

            final Date date = (val instanceof Date) 
                ? (Date) val
                : new Date(((java.util.Date) val).getTime());

            if (date == null) {
                throw new SQLException("Not a Date: " + columnIndex);
            } // end of 
        } // end of getDate
        */

        /**
         * {@inheritDoc}
         */
        public Time getTime(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val == null) {
                return null;
            } // end of if

            // ---

            if (val instanceof Time) {
                return (Time) val;
            } // end of if

            if (val instanceof java.util.Date) {
                return new Time(((java.util.Date) val).getTime());
            } // end of if

            throw new SQLException("Not a Time: " + columnIndex);
        } // end of getTime

        /**
         * {@inheritDoc}
         */
        public Time getTime(final String columnLabel) throws SQLException {
            final Object val = getObject(columnLabel);

            if (val == null) {
                return null;
            } // end of if

            // ---

            if (val instanceof Time) {
                return (Time) val;
            } // end of if

            if (val instanceof java.util.Date) {
                return new Time(((java.util.Date) val).getTime());
            } // end of if

            throw new SQLException("Not a Time: " + columnLabel);
        } // end of getTime

        /**
         * {@inheritDoc}
         */
        public Timestamp getTimestamp(final int columnIndex) 
            throws SQLException {

            final Object val = getObject(columnIndex);

            if (val == null) {
                return null;
            } // end of if

            // ---

            if (val instanceof Timestamp) {
                return (Timestamp) val;
            } // end of if

            if (val instanceof java.util.Date) {
                return new Timestamp(((java.util.Date) val).getTime());
            } // end of if

            throw new SQLException("Not a Timestamp: " + columnIndex);
        } // end of getTimestamp

        /**
         * {@inheritDoc}
         */
        public Timestamp getTimestamp(final String columnLabel) 
            throws SQLException {

            final Object val = getObject(columnLabel);

            if (val == null) {
                return null;
            } // end of if

            // ---

            if (val instanceof Timestamp) {
                return (Timestamp) val;
            } // end of if

            if (val instanceof java.util.Date) {
                return new Timestamp(((java.util.Date) val).getTime());
            } // end of if

            throw new SQLException("Not a Timestamp: " + columnLabel);
        } // end of getTimestamp
    } // end of class RowResultSet
} // end of class RowList
