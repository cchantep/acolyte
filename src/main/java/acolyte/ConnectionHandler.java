package acolyte;

/**
 * Connection handler.
 *
 * @author Cedric Chantepie
 * @see StatementHandler
 */
public interface ConnectionHandler {
    
    /**
     * Returns statement handler.
     */
    public StatementHandler getStatementHandler();

} // end of interface ConnectionHandler
