package acolyte;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.math.BigDecimal;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Type-safe list of row.
 *
 * @author Cedric Chantepie
 */
public abstract class RowList<R extends Row> {

    /**
     * Returns unmodifiable rows list.
     */
    public abstract List<R> getRows();

    /**
     * Appends |row|.
     *
     * @return Updated row list
     */
    public abstract RowList<R> append(R row);

    /**
     * Returns copy of row list with updated column names/labels.
     *
     * @param columnIndex Index of column (first index is 1)
     * @param label Column name/label
     */
    public abstract RowList<R> withLabel(int columnIndex, String label);

    /**
     * Returns result set from these rows.
     */
    public RowResultSet<R> resultSet() {
        return new RowResultSet<R>(getRows());
    } // end of resultSet

    /**
     * Returns this row list wrapped as handler result.
     */
    public QueryResult asResult() {
        return new QueryResult.Default(this);
    } // end of asResult

    /**
     * Returns ordered classes of columns.
     */
    public abstract List<Class<?>> getColumnClasses();

    /**
     * Gets column mappings, from index to label.
     *
     * @return Column mappings, or empty map (not null) if none
     */
    public abstract Map<String,Integer> getColumnLabels();

    // --- Shared ---

    /**
     * Nil row list instance
     */
    public static final NilRowList Nil = new NilRowList();

    // --- Inner classes ---

    /**
     * Nil row list.
     */
    private static final class NilRowList extends RowList<Row.Nothing> {
        // --- Properties ---

        /**
         * Empty list of row
         */
        private final List<Row.Nothing> rows = 
            new ArrayList<Row.Nothing>(0);

        /**
         * Empty list of column classes
         */
        private final List<Class<?>> colClasses = new ArrayList<Class<?>>(0);

        /**
         * Empty labels
         */
        private final Map<String,Integer> labels = 
            new HashMap<String,Integer>(0);

        // --- Constructors ---

        /**
         * No-arg constructor.
         */
        private NilRowList() { }

        // ---

        /**
         * Returns empty list of row
         */
        public List<Row.Nothing> getRows() {
            return this.rows;
        } // end of getRows

        /**
         * Returns unchanged nil row list.
         */
        public RowList<Row.Nothing> append(final Row.Nothing row) {
            return this;
        } // end of append

        /**
         * Returns unchanged nil row list.
         */
        public RowList<Row.Nothing> withLabel(final int columnIndex, 
                                              final String label) {

            return this;
        } // end of withLabel

        /**
         * Returns empty list of columns classes.
         */
        public List<Class<?>> getColumnClasses() {
            return this.colClasses;
        } // end of getColumnClasses

        /**
         * Returns empty list of column labels.
         */
        public Map<String,Integer> getColumnLabels() {
            return this.labels;
        } // end of getColumnLabels
    } // end of class Nil

    /**
     * Column definition.
     */
    public static final class Column<T> {
        /**
         * Column class
         */
        public final Class<T> columnClass;

        /**
         * Column name/label
         */
        public final String name;

        /**
         * Bulk constructor.
         */
        private Column(final Class<T> columnClass, final String name) {
            if (columnClass == null) {
                throw new IllegalArgumentException("No column class");
            } // end of if

            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("Invalid column name: " + 
                                                   name);

            } // end of if

            this.columnClass = columnClass;
            this.name = name;
        } // end of <init>

        /**
         * Creates column definition.
         *
         * @throws IllegalArgumentException if |columnClass| is null, 
         * or |name| is empty.
         */
        public static <T> Column<T> defineCol(final Class<T> columnClass,
                                              final String name) {

            return new Column<T>(columnClass, name);
        } // end of column
    } // end of Column

    /**
     * Cell on a row.
     */
    private static final class Cell<C> {
        public final C value;

        public Cell(final C v) {
            this.value = v;
        } // end of <init>
    } // end of class Cell

    /**
     * Result set made from list of row.
     *
     * @param R Row
     */
    public final class RowResultSet<R extends Row> extends AbstractResultSet {
        final List<Class<?>> columnClasses;
        final Map<String,Integer> columnLabels;
        final List<R> rows;
        final Statement statement;
        private Cell<?> last;

        // --- Constructors ---

        /**
         * Constructor
         */
        protected RowResultSet(final List<R> rows) {
            if (rows == null) {
                // Impossible
                throw new IllegalArgumentException();
            } // end of if

            this.columnClasses = getColumnClasses();
            this.columnLabels = getColumnLabels();
            this.rows = Collections.unmodifiableList(rows);
            this.statement = null; // dettached
            this.last = null;
            super.fetchSize = rows.size();
        } // end of <init>

        /**
         * Copy constructor.
         */
        private RowResultSet(final List<R> rows,
                             final Cell<?> last,
                             final Statement statement) {

            if (rows == null) {
                // Impossible
                throw new IllegalArgumentException();
            } // end of if

            this.columnClasses = getColumnClasses();
            this.columnLabels = getColumnLabels();
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

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof RowResultSet)) {
                return false;
            } // end of if

            // ---

            @SuppressWarnings("unchecked")
            final RowResultSet<R> other = (RowResultSet<R>) o;

            return new EqualsBuilder().
                append(this.rows, other.rows).
                append(this.last, other.last).
                append(this.columnClasses, other.columnClasses).
                append(this.columnLabels, other.columnLabels).
                isEquals();

        } // end of equals

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return new HashCodeBuilder(11, 9).
                append(this.rows).
                append(this.last).
                append(this.columnClasses).
                append(this.columnLabels).
                toHashCode();

        } // end of hashCode

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

            this.last = new Cell<Object>(val);
            
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

            if (columnLabel == null || 
                !this.columnLabels.containsKey(columnLabel)) {

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

            this.last = new Cell<Object>(val);
            
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
            return this.columnLabels.get(columnLabel);
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

        /**
         * {@inheritDoc}
         */
        public ResultSetMetaData getMetaData() throws SQLException {
            final Map<String,Integer> colNames = this.columnLabels;
            final HashMap<Integer,String> labels =
                new HashMap<Integer,String>(colNames.size());

            for (final Map.Entry<String,Integer> kv : colNames.entrySet()) {
                labels.put(kv.getValue(), kv.getKey());
            } // end of for
                
            return new RowListMetaData();
        } // end of getMetaData
    } // end of class RowResultSet

    /**
     * Result set metadata for RowList.
     */
    public final class RowListMetaData implements ResultSetMetaData {
        final List<Class<?>> columnClasses;
        final Map<String,Integer> columnLabels;

        // --- Constructors ---

        /**
         * No-arg constructor.
         */
        private RowListMetaData() {
            this.columnClasses = getColumnClasses();
            this.columnLabels = getColumnLabels();
        } // end of <init>

        // ---

        /**
         * {@inheritDoc}
         */
        public String getCatalogName(final int column) throws SQLException {
            return "";
        } // end of getCatalogName

        /**
         * {@inheritDoc}
         */
        public String getSchemaName(final int column) throws SQLException {
            return "";
        } // end of getSchemaName

        /**
         * {@inheritDoc}
         */
        public String getTableName(final int column) throws SQLException {
            return "";
        } // end of getTableName

        /**
         * {@inheritDoc}
         */
        public int getColumnCount() throws SQLException {
            return this.columnClasses.size();
        } // end of getColumnCount

        /**
         * {@inheritDoc}
         */
        public String getColumnClassName(final int column) throws SQLException {
            return this.columnClasses.get(column-1).getName();
        } // end of getColumnClassName

        /**
         * {@inheritDoc}
         */
        public int getColumnDisplaySize(final int column) 
            throws SQLException {

            return Integer.MAX_VALUE;
        } // end of getColumnDisplaySize

        /**
         * {@inheritDoc}
         */
        public String getColumnName(final int column) throws SQLException {
            for (final Map.Entry<String,Integer> vk : this.columnLabels.
                     entrySet()) {

                if (vk.getValue().intValue() == column) {
                    return vk.getKey();
                } // end of if
            } // end of for

            return null;
        } // end of getColumnName

        /**
         * {@inheritDoc}
         */
        public String getColumnLabel(final int column) throws SQLException {
            return getColumnName(column);
        } // end of getColumnLabel

        /**
         * {@inheritDoc}
         */
        public boolean isSigned(final int column) throws SQLException {
            final int type = getColumnType(column);

            if (type == -1) {
                return false;
            } // end of if

            final Boolean s = Defaults.jdbcTypeSigns.get(type);

            return (s == null) ? false : s;
        } // end of isSigned

        /**
         * {@inheritDoc}
         */
        public int isNullable(final int column) throws SQLException {
            return ResultSetMetaData.columnNullableUnknown;
        } // end of isNullable

        /**
         * {@inheritDoc}
         */
        public boolean isCurrency(final int column) throws SQLException {
            return false;
        } // end of isCurrency

        /**
         * {@inheritDoc}
         */
        public int getPrecision(final int column) throws SQLException {
            final int type = getColumnType(column);
            
            if (type == -1) {
                return 0;
            } // end of if

            final Integer p = Defaults.jdbcTypePrecisions.get(type);

            return (p == null) ? 0 : p;
        } // end of getPrecision

        /**
         * {@inheritDoc}
         */
        public int getScale(final int column) throws SQLException {
            final int type = getColumnType(column);
            
            if (type == -1) {
                return 0;
            } // end of if

            final Integer s = Defaults.jdbcTypeScales.get(type);

            return (s == null) ? 0 : s;
        } // end of getScale

        /**
         * {@inheritDoc}
         */
        public int getColumnType(final int column) throws SQLException {
            final Class<?> clazz = this.columnClasses.get(column-1);

            if (clazz == null) {
                return -1;
            } // end of if

            // ---

            String cn = null;

            if (clazz.isPrimitive()) {
                if (clazz.equals(Boolean.TYPE)) { 
                    cn = Boolean.class.getName();
                } // end of if

                if (clazz.equals(Character.TYPE)) {
                    cn = Character.class.getName();
                } // end of if

                if (clazz.equals(Byte.TYPE)) {
                    cn = Byte.class.getName();
                } // end of if

                if (clazz.equals(Short.TYPE)) {
                    cn = Short.class.getName();
                } // end of if

                if (clazz.equals(Integer.TYPE)) {
                    cn = Integer.class.getName();
                } // end of if

                if (clazz.equals(Long.TYPE)) {
                    cn = Long.class.getName();
                } // end of if

                if (clazz.equals(Float.TYPE)) {
                    cn = Float.class.getName();
                } // end of if

                if (clazz.equals(Double.TYPE)) {
                    cn = Double.class.getName();
                } // end of if
            } else {
                cn = clazz.getName();
            } // end of else

            final String className = cn;

            if (className == null) {
                return -1;
            } // end of if

            // ---

            for (final Map.Entry<Integer,String> kv : Defaults.
                     jdbcTypeMappings.entrySet()) {

                if (kv.getValue().equals(className)) {
                    return kv.getKey();
                } // end of if
            } // end of for

            return -1;
        } // end of getColumnType
                    
        /**
         * {@inheritDoc}
         */
        public String getColumnTypeName(final int column) 
            throws SQLException {

            return Defaults.jdbcTypeNames.get(getColumnType(column));
        } // end of getColumnTypeName

        /**
         * {@inheritDoc}
         */
        public boolean isSearchable(final int column) throws SQLException {
            return true;
        } // end of isSearchable

        /**
         * {@inheritDoc}
         */
        public boolean isCaseSensitive(final int column) throws SQLException {
            return true;
        } // end of isCaseSensitive

        /**
         * {@inheritDoc}
         */
        public boolean isAutoIncrement(final int column) throws SQLException {
            return false;
        } // end of isAutoIncrement

        /**
         * {@inheritDoc}
         */
        public boolean isReadOnly(final int column) throws SQLException {
            return true;
        } // end of isReadOnly

        /**
         * {@inheritDoc}
         */
        public boolean isWritable(final int column) throws SQLException {
            return false;
        } // end of isWritable

        /**
         * {@inheritDoc}
         */
        public boolean isDefinitelyWritable(final int column) 
            throws SQLException {

            return false;
        } // end of isDefinitelyWritable

        /**
         * {@inheritDoc}
         */
        public boolean isWrapperFor(final Class<?> iface) 
            throws SQLException {

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
    } // end of RowListMetaData
} // end of class RowList
