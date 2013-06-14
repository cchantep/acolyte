package acolyte;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.math.BigDecimal;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;


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

    /**
     * Column names
     */
    private final Map<String,Integer> colNames;

    // --- Constructors ---

    /**
     * Bulk constructor.
     *
     * @throws IllegalArgumentException if rows is null
     */
    protected RowList(final List<R> rows,
                      final Map<String,Integer> colNames) {

        if (rows == null) {
            throw new IllegalArgumentException("Invalid rows");
        } // end of if

        if (colNames == null) {
            throw new IllegalArgumentException("Invalid names");
        } // end of if

        this.rows = Collections.unmodifiableList(rows);
        this.colNames = Collections.unmodifiableMap(colNames);
    } // end of <init>

    /**
     * No-arg constructor.
     */
    public RowList() {
        this(new ArrayList<R>(), new HashMap<String,Integer>());
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

        return new RowList<R>(copy, this.colNames);
    } // end of append

    /**
     * Returns copy of row list with updated column names.
     *
     * @param columnIndex Index of column (first index is 1)
     * @param label Column name/label
     */
    public RowList<R> withLabel(final int columnIndex, final String label) {
        if (label == null) {
            throw new IllegalArgumentException("Invalid label");
        } // end of if

        // ---

        final HashMap<String,Integer> cols = 
            new HashMap<String,Integer>(this.colNames);

        cols.put(label, (Integer) columnIndex);

        return new RowList<R>(this.rows, cols);
    } // end of withLabel

    /**
     * Returns result set from these rows.
     */
    public RowResultSet<R> resultSet() {
        return new RowResultSet<R>(this.rows);
    } // end of resultSet

    // --- Row creation ---

    /**
     * Creates a row with 1 unnamed cell.
     */
    public static <A> Row.Row1<A> row1(final A c1) {
        return new Row.Row1<A>(c1);
    } // end of row1

    /**
     * Creates a row with 2 unnamed cells.
     */
    public static <A,B> Row.Row2<A,B> row2(final A c1, final B c2) {
        return new Row.Row2<A,B>(c1, c2);
    } // end of row2

    /**
     * Creates a row with 3 unnamed cells.
     */
    public static <A,B,C> Row.Row3<A,B,C> row3(final A c1, 
                                               final B c2, 
                                               final C c3) {

        return new Row.Row3<A,B,C>(c1, c2, c3);
    } // end of row3

    // --- Inner classes ---

    /**
     * Result set made from list of row.
     *
     * @param R Row
     */
    public final class RowResultSet<R extends Row> extends AbstractResultSet {
        final List<R> rows;
        final Statement statement;
        private Column<?> last;

        // --- Constructors ---

        /**
         * Constructor
         */
        protected RowResultSet(final List<R> rows) {
            if (rows == null) {
                // Impossible
                throw new IllegalArgumentException();
            } // end of if

            this.rows = Collections.unmodifiableList(rows);
            this.statement = null; // dettached
            this.last = null;
            super.fetchSize = rows.size();
        } // end of <init>

        /**
         * Copy constructor.
         */
        private RowResultSet(final List<R> rows,
                             final Column<?> last,
                             final Statement statement) {

            if (rows == null) {
                // Impossible
                throw new IllegalArgumentException();
            } // end of if

            this.rows = Collections.unmodifiableList(rows);
            this.statement = statement;
            this.last = null;
            super.fetchSize = rows.size();
        } // end of <init>

        // --- 

        /**
         * Returns updated resultset, attached with given |statement|.
         */
        public RowResultSet<R> withStatement(final Statement statement) {
            return new RowResultSet<R>(this.rows, this.last, statement);
        } // end of withStatement

        // --- ResultSet implementation ---

        /**
         * {@inheritDoc}
         */
        public Statement getStatement() {
            return this.statement;
        } // end of getStatement
        
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

            if (columnLabel == null || !colNames.containsKey(columnLabel)) {
                throw new SQLException("Invalid label: " + columnLabel);
            } // end of if

            // ---

            final int columnIndex = findColumn(columnLabel);
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
        public Object getObject(final int columnIndex, 
                                final Map<String, Class<?>> typemap) 
            throws SQLException {

            return getObject(columnIndex);
        } // end of getObject

        /**
         * {@inheritDoc}
         */
        public Object getObject(final String columnLabel, 
                                final Map<String, Class<?>> typemap) 
            throws SQLException {
            
            return getObject(columnLabel);
        } // end of getObject

        /**
         * {@inheritDoc}
         */
        public <T extends Object> T getObject(final int columnIndex, 
                                              final Class<T> type) 
            throws SQLException {
            
            if (type == null) {
                throw new SQLException("Invalid type");
            } // end of if

            // ---

            final Object val = getObject(columnIndex);

            if (val == null) {
                return null;
            } // end of if

            // ---

            return convert(val, type);
        } // end of getObject

        /**
         * {@inheritDoc}
         */
        public <T extends Object> T getObject(final String columnLabel,
                                              final Class<T> type) 
            throws SQLException {
            
            if (type == null) {
                throw new SQLException("Invalid type");
            } // end of if

            // ---

            final Object val = getObject(columnLabel);

            if (val == null) {
                return null;
            } // end of if

            return convert(val, type);
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
         */
        public Date getDate(final int columnIndex, 
                            final Calendar cal) throws SQLException {

            return getDate(columnIndex);
        } // end of getDate

        /**
         * {@inheritDoc}
         */
        public Date getDate(final String columnLabel, 
                            final Calendar cal) throws SQLException {

            return getDate(columnLabel);
        } // end of getDate

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
        public Time getTime(final int columnIndex, 
                            final Calendar cal) throws SQLException {

            return getTime(columnIndex);
        } // end of getTime

        /**
         * {@inheritDoc}
         */
        public Time getTime(final String columnLabel, 
                            final Calendar cal) throws SQLException {

            return getTime(columnLabel);
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

        /**
         * {@inheritDoc}
         */
        public Timestamp getTimestamp(final int columnIndex, 
                                      final Calendar cal) throws SQLException {
            
            return getTimestamp(columnIndex);
        } // end of getTimestamp

        /**
         * {@inheritDoc}
         */
        public Timestamp getTimestamp(final String columnLabel, 
                                      final Calendar cal) throws SQLException {
            
            return getTimestamp(columnLabel);
        } // end of getTimestamp

        /**
         * {@inheritDoc}
         */
        public int findColumn(final String columnLabel) throws SQLException {
            return colNames.get(columnLabel);
        } // end of findColumn

        /**
         * Convert not null value.
         */
        private <T extends Object> T convert(final Object val, 
                                             final Class<T> type) 
            throws SQLException {

            final Class clazz = val.getClass();

            if (type.isAssignableFrom(clazz)) {
                return type.cast(val);
            } // end of if

            if (java.util.Date.class.isAssignableFrom(type) &&
                java.util.Date.class.isAssignableFrom(clazz)) {

                @SuppressWarnings("unchecked")
                final Class<? extends java.util.Date> origType = 
                    (Class<? extends java.util.Date>) clazz;
                
                final java.util.Date orig = origType.cast(val);

                if (Date.class.isAssignableFrom(type)) {
                    return type.cast(new Date(orig.getTime()));
                } else if (Time.class.isAssignableFrom(type)) {
                    return type.cast(new Time(orig.getTime()));
                } else if (Timestamp.class.isAssignableFrom(type)) {
                    return type.cast(new Timestamp(orig.getTime()));
                } // end of else if
                
                throw new SQLException("Fails to convert temporal type");
            } // end of if

            if (Number.class.isAssignableFrom(type) &&
                Number.class.isAssignableFrom(clazz)) {

                @SuppressWarnings("unchecked")
                final Class<? extends Number> origType = 
                    (Class<? extends Number>) clazz;
                
                final Number num = origType.cast(val);

                if (Byte.class.isAssignableFrom(type)) {
                    return type.cast(new Byte(num.toString()));
                } else if (Double.class.isAssignableFrom(type)) {
                    return type.cast(new Double(num.toString()));
                } else if (Float.class.isAssignableFrom(type)) {
                    return type.cast(new Float(num.toString()));
                } else if (Integer.class.isAssignableFrom(type)) {
                    return type.cast(new Integer(num.toString()));
                } else if (Long.class.isAssignableFrom(type)) {
                    return type.cast(new Long(num.toString()));
                } else if (Short.class.isAssignableFrom(type)) {
                    return type.cast(new Short(num.toString()));
                } // end of if

                throw new SQLException("Fails to convert numeric type");
            } // end of if

            throw new SQLException("Incompatible type: " + type + ", " + clazz);
        } // end of convert
    } // end of class RowResultSet
} // end of class RowList
