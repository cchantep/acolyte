package acolyte;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Row marker interface
 */
public interface Row { 

    /**
     * Row with 1 cell.
     */
    public static final class Row1<A> implements Row {
        public final A _1;

        // --- Constructors ---

        /**
         * Copy constructor.
         *
         * @param c1 Value for cell #1
         */
        protected Row1(final A c1) {
            this._1 = c1;
        } // end of <init>

        // --- Object support ---

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return (this._1 == null) ? -1 : this._1.hashCode();
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
            
            return ((this._1 == null && other._1 == null) ||
                    (this._1 != null && this._1.equals(other._1)));

        } // end of equals

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return String.format("Row1(%s)", this._1);
        } // end of toString
    } // end of class Row1

    /**
     * Row with 2 cells.
     */
    public static final class Row2<A,B> implements Row {

        public final A _1;
        public final B _2;

        // --- Constructors ---

        /**
         * No arg constructor, with null cells.
         */
        public Row2() {
            this._1 = null;
            this._2 = null;
        } // end of <init>

        /**
         * Copy constructor.
         */
        public Row2(final A c1, final B c2) {
            this._1 = c1;
            this._2 = c2;
        } // end of <init>

        // --- 

        /**
         * Sets value for cell #1.
         *
         * @return Updated row
         */
        public Row2<A,B> set1(final A value) {
            return new Row2<A,B>(value, this._2);
        } // end of set1

        /**
         * Sets value for cell #2.
         *
         * @return Updated row
         */
        public Row2<A,B> set2(final B value) {
            return new Row2<A,B>(this._1, value);
        } // end of set1

        // --- Object support ---

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return new HashCodeBuilder(1, 3).
                append(this._1).append(this._2).
                toHashCode();

        } // end of hashCode

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof Row2)) {
                return false;
            } // end of if

            // --- 

            @SuppressWarnings("unchecked")
            final Row2<A,B> other = (Row2<A,B>) o;
            
            return new EqualsBuilder().
                append(this._1, other._1).
                append(this._2, other._2).
                isEquals();

        } // end of equals

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return String.format("Row2(%s, %s)", this._1, this._2);
        } // end of toString
    } // end of class Row2

    /**
     * Row with 3 cells.
     */
    public static final class Row3<A,B,C> implements Row {
        public final A _1;
        public final B _2;
        public final C _3;

        // --- Constructors ---

        /**
         * No arg constructor, with null cells.
         */
        public Row3() {
            this._1 = null;
            this._2 = null;
            this._3 = null;
        } // end of <init>

        /**
         * Copy constructor.
         */
        public Row3(final A c1, final B c2, final C c3) {
            this._1 = c1;
            this._2 = c2;
            this._3 = c3;
        } // end of <init>

        // --- 

        /**
         * Sets value for cell #1.
         *
         * @return Updated row
         */
        public Row3<A,B,C> set1(final A value) {
            return new Row3<A,B,C>(value, this._2, this._3);
        } // end of set1

        /**
         * Sets value for cell #2.
         *
         * @return Updated row
         */
        public Row3<A,B,C> set2(final B value) {
            return new Row3<A,B,C>(this._1, value, this._3);
        } // end of set2

        /**
         * Sets value for cell #3.
         *
         * @return Updated row
         */
        public Row3<A,B,C> set3(final C value) {
            return new Row3<A,B,C>(this._1, this._2, value);
        } // end of set3

        // --- Object support ---

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return new HashCodeBuilder(3, 1).
                append(this._1).
                append(this._2).
                append(this._3).
                toHashCode();

        } // end of hashCode

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof Row3)) {
                return false;
            } // end of if

            // --- 

            @SuppressWarnings("unchecked")
            final Row3<A,B,C> other = (Row3<A,B,C>) o;
            
            return new EqualsBuilder().
                append(this._1, other._1).
                append(this._2, other._2).
                append(this._3, other._3).
                isEquals();

        } // end of equals

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return String.format("Row3(%s, %s, %s)", this._1, this._2, this._3);
        } // end of toString
    } // end of class Row3
} // end of interface Row
