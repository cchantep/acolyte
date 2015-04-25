package acolyte.jdbc;

/**
 * Connection handler.
 *
 * @author Cedric Chantepie
 * @see StatementHandler
 */
public interface ConnectionHandler {
    
    /**
     * Returns statement handler.
     * @return the statement handler
     */
    public StatementHandler getStatementHandler();

    //  --- Inner classes ---

    /**
     * Default implementation.
     */
    public static final class Default implements ConnectionHandler {
        final StatementHandler stmtHandler;

        /**
         * Bulk constructor.
         * @param handler the statement handler
         */
        public Default(final StatementHandler handler) {
            if (handler == null) {
                throw new IllegalArgumentException();
            } // end of if

            this.stmtHandler = handler;
        } // end of <init>

        /**
         * {@inheritDoc}
         */
        public StatementHandler getStatementHandler() {
            return this.stmtHandler;
        } // end of getStatementHandler
    } // end of class DefaultHandler
} // end of interface ConnectionHandler
