package acolyte.jdbc;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Row with 1 cell.
 */
public final class Row1<A> implements Row {
    // --- Properties ---

    /**
     * Value for cell #1
     */
    public final A _1;

    /**
     * Cell list
     */
    public final List<Object> cells;

    // --- Constructors ---

    /**
     * Copy constructor.
     *
     * @param c1 Value for cell #1
     */
    protected Row1(final A c1) {
        this._1 = c1;

        final ArrayList<Object> cs = new ArrayList<Object>(1);

        cs.add(this._1);

        this.cells = Collections.unmodifiableList(cs);
    } // end of <init>

    // ---
        
    /**
     * {@inheritDoc}
     */
    public List<Object> cells() {
        return this.cells;
    } // end of cells

    // --- Object support ---

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 7).
            append(this._1).
            toHashCode();
                
    } // end of hashCode

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof Row1)) {
            return false;
        } // end of if

        // --- 

        @SuppressWarnings("unchecked")
        final Row1<A> other = (Row1<A>) o;

        return new EqualsBuilder().
            append(this._1, other._1).
            isEquals();

    } // end of equals

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("Row1(%s)", this._1);
    } // end of toString
} // end of class Row1
