package acolyte.jdbc;

import java.util.List;

/**
 * Row marker interface
 */
public interface Row {
    /**
     * Returns information for cell(s) of row.
     * Each cell is decribed with a value (left) and an optional value (right).
     *
     * @return the cells of this row
     */
    public List<Object> cells();

    // --- Shared ---

    /**
     * Nothing of Row.
     */
    public static final class Nothing implements Row {
        /**
         * Returns null/no cell.
         *
         * @return null
         */
        public List<Object> cells() { return null; }
    } // end of class Nothing

    /**
     *
     */
    static final class Untyped implements Row {
        protected final List<Object> cells;

        /**
         */
        protected Untyped(final List<Object> cells) {
            if (cells == null) {
                throw new IllegalArgumentException();
            }
            
            this.cells = cells;
        }

        /**
         * {@inheritDoc}
         */
        public List<Object> cells() { return this.cells; }
    }
} // end of interface Row
