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

    /**
     * Returns resource handler.
     * @return the resource handler
     */
    public ResourceHandler getResourceHandler();

    /**
     * Returns this connection handler with its resource |handler| updated.
     *
     * @param handler the new resource handler
     */
    public ConnectionHandler withResourceHandler(ResourceHandler handler);

    // --- Inner classes ---

    /**
     * Default implementation.
     */
    public static final class Default implements ConnectionHandler {
        final StatementHandler stmtHandler;
        final ResourceHandler resHandler;

        /**
         * Statement constructor.
         *
         * @param stmtHandler the statement handler
         */
        public Default(final StatementHandler stmtHandler) {
            this(stmtHandler, new ResourceHandler.Default());
        }

        /**
         * Bulk constructor.
         *
         * @param stmtHandler the statement handler
         * @param resHandler the resource handler
         */
        public Default(final StatementHandler stmtHandler,
                       final ResourceHandler resHandler) {

            if (stmtHandler == null) {
                throw new IllegalArgumentException("Statement handler");
            } // end of if

            if (resHandler == null) {
                throw new IllegalArgumentException("Resource handler");
            } // end of if

            this.stmtHandler = stmtHandler;
            this.resHandler = resHandler;
        } // end of <init>

        /**
         * {@inheritDoc}
         */
        public StatementHandler getStatementHandler() {
            return this.stmtHandler;
        } // end of getStatementHandler

        /**
         * {@inheritDoc}
         */
        public ResourceHandler getResourceHandler() {
            return this.resHandler;
        } // end of getResourceHandler
        
        /**
         * {@inheritDoc}
         */
        public ConnectionHandler withResourceHandler(ResourceHandler handler) {
            return new Default(this.stmtHandler, handler);
        } // end of withResourceHandler
    } // end of class DefaultHandler
} // end of interface ConnectionHandler
