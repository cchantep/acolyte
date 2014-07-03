package acolyte;

import java.sql.SQLWarning;

/**
 * Query result.
 *
 * @author Cedric Chantepie
 * @deprecated Use {@link acolyte.jdbc.QueryResult}
 */
@Deprecated
public interface QueryResult extends Result<QueryResult> {

    /**
     * Returns underlying row list.
     */
    public RowList<?> getRowList();

    // --- Shared ---

    /**
     * Nil query result
     */
    public static final QueryResult Nil = new Default(RowList.Nil);

    // --- Inner classes ---

    /**
     * Default implementation.
     */
    final class Default implements QueryResult {
        final RowList<?> rowList;
        final SQLWarning warning;

        /**
         * Rows constructor.
         */
        protected Default(final RowList<?> list) {
            this(list, null);
        } // end of <init>

        /**
         * Bulk constructor
         */
        private Default(final RowList<?> list, final SQLWarning warning) {
            this.rowList = list;
            this.warning = warning;
        } // end of <init>

        // ---

        /**
         * {@inheritDoc}
         */
        public RowList<?> getRowList() { 
            return this.rowList;
        } // end of getRowList

        /**
         * {@inheritDoc}
         */
        public QueryResult withWarning(final SQLWarning warning) {
            return new Default(this.rowList, warning);
        } // end of withWarning

        /**
         * {@inheritDoc}
         */
        public QueryResult withWarning(final String reason) {
            return withWarning(new SQLWarning(reason));
        } // end of withWarning

        /**
         * {@inheritDoc}
         */
        public SQLWarning getWarning() {
            return this.warning;
        } // end of getWarning

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof QueryResult.Default)) {
                return false;
            } // end of if

            final QueryResult.Default other = (QueryResult.Default) o;

            return ((this.rowList == null && other.rowList == null) ||
                    (this.rowList != null && 
                     this.rowList.equals(other.rowList)));

        } // end of equals

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return (this.rowList == null) ? -1 : this.rowList.hashCode();
        } // end of hashCode
    } // end of class Default
} // end of interface QueryResult
