package acolyte.jdbc;

import java.sql.SQLException;

/**
 * Resource handler: allow to intercept management operations 
 * about the connection resources.
 */
public interface ResourceHandler {
    /**
     * Is fired when the transaction of |connection| is commited
     * (but not for implicit commit in case of auto-commit).
     *
     * @see java.sql.Connection#commit
     */
    public void whenCommitTransaction(Connection connection)
        throws SQLException;

    /**
     * Is fired when the transaction of |connection| is rollbacked.
     *
     * @see java.sql.Connection#rollback
     */
    public void whenRollbackTransaction(Connection connection)
        throws SQLException;

    // --- Inner classes ---

    /**
     * Default implementation.
     */
    public static final class Default implements ResourceHandler {
        public Default() {
        }

        /**
         * {@inheritDoc}
         */
        public void whenCommitTransaction(Connection connection)
            throws SQLException {}

        /**
         * {@inheritDoc}
         */
        public void whenRollbackTransaction(Connection connection)
            throws SQLException {}
    }
}
