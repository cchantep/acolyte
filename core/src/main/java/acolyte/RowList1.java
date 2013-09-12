package acolyte;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acolyte.Row.Row1;

/**
 * Row list of row with 1 cell.
 *
 * @author Cedric Chantepie
 */
public final class RowList1<A> extends RowList<Row1<A>> {
    // --- Properties ---
        
    /**
     * Rows
     */
    private final List<Row1<A>> rows;
        
    /**
     * Column names
     */
    private final Map<String,Integer> colNames;

    /**
     * Class of column #1
     */
    private final Class<A> c1;

    /**
     * Column classes
     */
    private final ArrayList<Class<?>> colClasses;

    // --- Constructors ---

    /**
     * Bulk constructor.
     *
     * @throws IllegalArgumentException if rows is null
     */
    protected RowList1(final Class<A> c1,
                       final List<Row1<A>> rows,
                       final Map<String,Integer> colNames) {

        if (c1 == null) {
            throw new IllegalArgumentException("Invalid class for column #1");
        } // end of if

        if (rows == null) {
            throw new IllegalArgumentException("Invalid rows");
        } // end of if

        if (colNames == null) {
            throw new IllegalArgumentException("Invalid names");
        } // end of if

        this.rows = Collections.unmodifiableList(rows);
        this.colNames = Collections.unmodifiableMap(colNames);

        // Column classes
        final ArrayList<Class<?>> colClasses = new ArrayList<Class<?>>();

        this.c1 = c1;
        colClasses.add(c1);

        this.colClasses = colClasses;
    } // end of <init>

    /**
     * No-arg constructor.
     */
    public RowList1(final Class<A> c1) {
        this(c1, new ArrayList<Row1<A>>(), new HashMap<String,Integer>());
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public List<Row1<A>> getRows() { return this.rows; }

    /**
     * {@inheritDoc}
     */
    public Map<String,Integer> getColumnLabels() { return this.colNames; }

    /**
     * {@inheritDoc}
     */
    public RowList1<A> append(final Row1<A> row) {
        final ArrayList<Row1<A>> copy = new ArrayList<Row1<A>>(this.rows);
            
        copy.add(row);
            
        return new RowList1<A>(this.c1, copy, this.colNames);
    } // end of append

    /**
     * Convinience append.
     */
    public RowList1<A> append(final A value) {
        return append(new Row1<A>(value));
    } // end of append

    /**
     * {@inheritDoc}
     */
    public RowList1<A> withLabel(final int columnIndex, final String label) {
        if (label == null) {
            throw new IllegalArgumentException("Invalid label");
        } // end of if

        // ---

        final HashMap<String,Integer> cols = 
            new HashMap<String,Integer>(this.colNames);

        cols.put(label, (Integer) columnIndex);

        return new RowList1<A>(this.c1, this.rows, cols);
    } // end of withLabel
        
    /**
     * {@inheritDoc}
     */
    public List<Class<?>> getColumnClasses() {
        return this.colClasses;
    } // end of getColumnClasses
} // end of class RowList1
