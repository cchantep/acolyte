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
     */
    public Connection connect(final String url, final Properties info) 
        throws SQLException {

        throw new SQLException("No yet implemented");
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
        return 0;
    } // end of getMajorVersion

    /**
     * {@inheritDoc}
     */
    public int getMinorVersion() {
        return 1;
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
    public void setHandler(Object/*JdbcHandler*/ handler) {
        throw new RuntimeException("Not yet implemented");
    } // end of setHandler
} // end of class Driver
