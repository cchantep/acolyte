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
} // end of interface Row
