package acolyte;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import java.sql.SQLException;

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
     * Creates a row with 1 cell.
     */
    public static <A> Row.Row1<A> row1(final A c1) {
        return new Row.Row1<A>(c1);
    } // end of row1

    /**
     * Creates a row with 2 cells.
     */
    public static <A,B> Row.Row2<A,B> row2(final A c1, final B c2) {
        return new Row.Row2<A,B>(c1, c2);
    } // end of row2

    /**
     * Creates a row with 3 cells.
     */
    public static <A,B,C> Row.Row3<A,B,C> row3(final A c1, 
                                               final B c2, 
                                               final C c3) {

        return new Row.Row3<A,B,C>(c1, c2, c3);
    } // end of row3

    // --- Inner classes ---

    /**
     * Result set made from list of row.
     */
    private final class RowResultSet<R> extends AbstractResultSet {
        final List<R> rows;

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
            return this.fetchSize;
        } // end of getFetchSize
    } // end of class RowResultSet
} // end of class RowList
