package acolyte;

import java.util.Properties;

import java.util.logging.Logger;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * Acolyte driver.
 *
 * @author Cedric Chantepie
 */
public final class Driver implements java.sql.Driver {
    // --- Constants ---

    /**
     * Major version
     */
    public static final int MAJOR_VERSION = 0;

    /**
     * Minor version
     */
    public static final int MINOR_VERSION = 1;

    // --- Shared ---

    static {
        try {
            java.sql.DriverManager.registerDriver(new Driver());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } // end of catch
    } // end of <cinit>

    // --- Properties ---

    /**
     * Handler to be used on next connection.
     */
    private ConnectionHandler handler = null;

    // --- Driver impl ---

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if no handler is set for connection
     */
    public Connection connect(final String url, final Properties info) 
        throws SQLException {

        if (!acceptsURL(url)) {
            return null;
        } // end of if

        if (this.handler == null) {
            throw new IllegalStateException("No connection handler");
        } // end of if

        return new acolyte.Connection(url, info, this.handler);
    } // end of connect

    /**
     * {@inheritDoc}
     */
    public boolean acceptsURL(final String url) throws SQLException {
        return (url != null && url.startsWith("jdbc:acolyte:"));
    } // end of acceptsUrl

    /**
     * {@inheritDoc}
     */
    public DriverPropertyInfo[] getPropertyInfo(final String url, 
                                                final Properties info) 
        throws SQLException {

        return new DriverPropertyInfo[0];
    } // end of getPropertyInfo

    /**
     * {@inheritDoc}
     */
    public int getMajorVersion() {
        return MAJOR_VERSION;
    } // end of getMajorVersion

    /**
     * {@inheritDoc}
     */
    public int getMinorVersion() {
        return MINOR_VERSION;
    } // end of getMinorVersion

    /**
     * {@inheritDoc}
     */
    public boolean jdbcCompliant() {
        return false;
    } // end of jdbcCompliant

    /**
     * {@inheritDoc}
     */
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    } // end of getParentLogger

    // --- Handler support ---

    /**
     * TODO: Will allow to handle queries/returns results by handle.
     */
    public void setHandler(final ConnectionHandler handler) {
        this.handler = handler;
    } // end of setHandler
} // end of class Driver
