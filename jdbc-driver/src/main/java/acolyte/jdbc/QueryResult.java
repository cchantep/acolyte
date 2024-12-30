package acolyte.jdbc;

import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * Query result.
 *
 * @author Cedric Chantepie
 */
public interface QueryResult extends Result<QueryResult> {

    /**
     * Returns underlying row list.
     * @return the list of rows in this result
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
        final SQLException exception;

        /**
         * Rows constructor.
         *
         * @param list the list of rows for this result
         */
        protected Default(final RowList<?> list) {
            this(list, null, null);
        } // end of <init>

        /**
         * Bulk constructor
         *
         * @param list the list of rows for this result
         * @param warning the SQL warning
         */
        private Default(final RowList<?> list,
                        final SQLWarning warning,
                        final SQLException exception) {

            this.rowList = list;
            this.warning = warning;
            this.exception = exception;
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
            return new Default(this.rowList, warning, this.exception);
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
        public QueryResult withException(final SQLException exception) {
            return new Default(this.rowList, this.warning, exception);
        } // end of withException

        /**
         * {@inheritDoc}
         */
        public QueryResult withException(final String message) {
            return withException(new SQLException(message));
        } // end of withException

        /**
         * {@inheritDoc}
         */
        public SQLException getException() {
            return this.exception;
        } // end of getException

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof QueryResult.Default)) {
                return false;
            } // end of if

            final QueryResult.Default other = (QueryResult.Default) o;

            return (((this.rowList == null && other.rowList == null) ||
                     (this.rowList != null && 
                      this.rowList.equals(other.rowList))) &&
                    ((this.warning == null && other.warning == null) ||
                     (this.warning != null && this.warning.equals(other.warning))) &&
                    ((this.exception == null && other.exception == null) ||
                     (this.exception != null && this.exception.equals(other.exception))));

        } // end of equals

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return (this.rowList == null) ? -1 : this.rowList.hashCode();
        } // end of hashCode
    } // end of class Default
} // end of interface QueryResult
