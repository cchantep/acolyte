package acolyte.jdbc;

import java.util.List;

import java.util.regex.Pattern;

import java.sql.SQLException;
import java.sql.ResultSet;

import acolyte.jdbc.StatementHandler.Parameter;

/**
 * Default implementation for composite handler.
 *
 * @author Cedric Chantepie
 */
public class CompositeHandler 
    extends AbstractCompositeHandler<CompositeHandler> {

    // --- Constructors ---

    /**
     * Constructor
     */
    public CompositeHandler() { super(); }

    /**
     * Copy constructor.
     *
     * @param queryDetection the patterns to detect a query
     * @param queryHandler the handler for the queries
     * @param updateHandler the handler for the updates
     */
    public CompositeHandler(final Pattern[] queryDetection,
                            final QueryHandler queryHandler,
                            final UpdateHandler updateHandler) {

        super(queryDetection, queryHandler, updateHandler);
    } // end of <init>

    /**
     * Returns 'empty' statement handler, 
     * without detection pattern, query handler or update handler.
     *
     * @return An empty handler
     */
    public static CompositeHandler empty() {
        return new CompositeHandler();
    } // end of empty

    // ---

    /**
     * Returns a new handler based on this one, 
     * but with given query |handler| appended.
     *
     * @param handler Query handler
     * @return a new composite handler with given query handler
     * @throws IllegalArgumentException if handler is null
     */
    public CompositeHandler withQueryHandler(final QueryHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        return new CompositeHandler(this.queryDetection,
                                    handler,
                                    this.updateHandler);
        
    } // end of withQueryHandler

    /**
     * Returns a new handler based on this one, 
     * but with given update |handler| appended.
     *
     * @param handler Update handler
     * @return a new composite handler with given update handler
     * @throws IllegalArgumentException if handler is null
     */
    public CompositeHandler withUpdateHandler(final UpdateHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        return new CompositeHandler(this.queryDetection,
                                    this.queryHandler,
                                    handler);
        
    } // end of withUpdateHandler

    /**
     * {@inheritDoc}
     */
    public CompositeHandler withQueryDetection(final Pattern... pattern) {
        return new CompositeHandler(queryDetectionPattern(pattern),
                                    this.queryHandler, this.updateHandler);

    } // end of withQueryDetection
} // end of class CompositeHandler
