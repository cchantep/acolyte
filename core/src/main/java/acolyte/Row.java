package acolyte;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Row marker interface
 */
public interface Row {
    /**
     * Returns information for cell(s) of row.
     * Each cell is decribed with a value (left) and an optional value (right).
     */
    public List<Object> cells();
    
    // ---

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
     * Row with 1 cell.
     */
    public static final class Row1<A> implements Row {
        public final A _1;
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
} // end of interface Row
