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

    // --- Driver impl ---

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if |info| doesn't contain handler
     * (ConnectionHandler) for property "connection.handler".
     */
    public Connection connect(final String url, final Properties info) 
        throws SQLException {

        if (!acceptsURL(url)) {
            return null;
        } // end of if

        if (info == null || !info.containsKey("connection.handler")) {
            throw new IllegalArgumentException("Invalid properties");
        } // end of if

        // ---

        try {
            return new acolyte.
                Connection(url, info, 
                           (ConnectionHandler) info.get("connection.handler"));

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid handler: " + 
                                               e.getMessage());

        } // end of catch
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

    // ---

    /**
     * Properties with handler
     */
    public static Properties properties(final ConnectionHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        final Properties props = new Properties();

        props.put("connection.handler", handler);

        return props;
    } // end of properties
} // end of class Driver
