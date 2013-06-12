package acolyte;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import org.apache.commons.lang3.tuple.ImmutablePair;

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
     * Column from a row.
     */
    public static final class Column<A> {
        public final A value;

        public Column(final A v) {
            this.value = v;
        } // end of <init>
    } // end of class Column

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
            if (!isOn()) {
                throw new SQLException("Not on a row");
            } // end of if

            final int idx = columnIndex - 1;
            final List<ImmutablePair<Object,String>> cells = 
                this.rows.get(this.row-1).cells();

            if (idx < 0 || idx >= cells.size()) {
                throw new SQLException("Invalid column index: " + columnIndex);
            } // end of if

            // ---

            final Object val = cells.get(idx).left;

            this.last = new Column<Object>(val);
            
            return val;
        } // end of getObject

        /**
         * {@inheritDoc}
         */
        public String getString(final int columnIndex) throws SQLException {
            final Object val = getObject(columnIndex);

            if (val instanceof String) {
                return (String) val;
            } // end of if

            return String.valueOf(val);
        } // end of getString
    } // end of class RowResultSet
} // end of class RowList
