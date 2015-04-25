package acolyte.jdbc;

import java.util.Properties;
import java.util.HashMap;

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

    /**
     * Handler registry
     */
    public static final HashMap<String,ConnectionHandler> handlers =
        new HashMap<String,ConnectionHandler>();

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
    public acolyte.jdbc.Connection connect(final String url, 
                                           final Properties info) 
        throws SQLException {

        if (!acceptsURL(url)) {
            return null;
        } // end of if
        
        final String[] parts = url.substring(url.lastIndexOf("?")+1).split("&");
        String h = null;

        for (final String p : parts) {
            if (p.startsWith("handler=")) {
                h = p.substring(8);
                break;
            } // end of if
        } // end of for

        if (h == null || h.length() == 0) {
            throw new IllegalArgumentException("Invalid handler ID: " + h);
        } // end of if

        final String id = h;
        final ConnectionHandler handler = handlers.get(id);

        if (handler == null) {
            throw new IllegalArgumentException("No matching handler: " + id);
        } // end of if

        // ---

        return new acolyte.jdbc.Connection(url, info, handler);
    } // end of connect

    /**
     * Creates a connection to specified |url| with given configuration |info|.
     *
     * @see #connect(java.lang.String, java.util.Properties)
     */
    public acolyte.jdbc.Connection connect(final String url,
                                           final Property... info) 
        throws SQLException {
    
        return connect(url, props(info));
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

    /**
     * Direct connection, with given |handler| and random URL.
     *
     * @throws IllegalArgumentException if handler is null
     */
    public static acolyte.jdbc.Connection connection(ConnectionHandler handler) {
        return connection(handler, (Properties) null);
    } // end of connection

    /**
     * Direct connection, with given |handler| and random URL.
     *
     * @param handler Connection handler
     * @param info Connection properties (optional)
     * @throws IllegalArgumentException if handler is null
     * @see #connection(acolyte.jdbc.ConnectionHandler, java.util.Properties)
     */
    public static acolyte.jdbc.Connection connection(final ConnectionHandler handler, final Property... info) {
        return connection(handler, props(info));
    } // end of connnection

    /**
     * Direct connection, with given |handler| and random URL.
     *
     * @param handler Connection handler
     * @param info Connection properties (optional)
     * @throws IllegalArgumentException if handler is null
     * @see #connection(acolyte.jdbc.ConnectionHandler)
     */
    public static acolyte.jdbc.Connection connection(final ConnectionHandler handler, final Properties info) {

        if (handler == null) {
            throw new IllegalArgumentException();
        } // end of if

        final String url = String.
            format("jdbc:acolyte:direct-%d", System.identityHashCode(handler));

        return new acolyte.jdbc.Connection(url, info, handler);
    } // end of connection

    /**
     * Direct connection, with given |handler| and random URL.
     *
     * @throws IllegalArgumentException if handler is null
     */
    public static acolyte.jdbc.Connection connection(StatementHandler handler) {
        return connection(handler, (Properties) null);
    } // end of connection

    /**
     * Direct connection, with given |handler| and random URL.
     *
     * @param handler Statement handler
     * @param info Connection properties (optional)
     * @throws IllegalArgumentException if handler is null
     */
    public static acolyte.jdbc.Connection connection(final StatementHandler handler, final Properties info) {

        if (handler == null) {
            throw new IllegalArgumentException();
        } // end of if

        return connection(new ConnectionHandler.Default(handler), info);
    } // end of connection

    /**
     * Direct connection, with given |handler| and random URL.
     *
     * @param handler Statement handler
     * @param info Connection properties (optional)
     * @throws IllegalArgumentException if handler is null
     */
    public static acolyte.jdbc.Connection connection(final StatementHandler handler, final Property... info) {
        return connection(handler, props(info));
    } // end of connection

    // ---

    /**
     * Registers a connection handler.
     *
     * @param id Handler unique ID
     * @param handler Connection handler
     * @return Handler previously registered for |id|, or null if none
     * @throws IllegalArgumentException if |id| or |handler| is null (or empty)
     * @see #unregister
     */
    public static ConnectionHandler register(final String id, 
                                             final ConnectionHandler handler) {

        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("Invalid ID: " + id);
        } // end of if

        if (handler == null) {
            throw new IllegalArgumentException("Invalid handler: " + handler);
        } // end of if

        // ---

        return handlers.put(id, handler);
    } // end of register

    /**
     * Registers a connection handler.
     *
     * @param id Handler unique ID
     * @param handler Connection handler
     * @return Handler previously registered for |id|, or null if none
     * @throws IllegalArgumentException if |id| or |handler| is null (or empty)
     * @see #register(java.lang.String,acolyte.jdbc.ConnectionHandler)
     * @see #unregister
     */
    public static ConnectionHandler register(final String id, 
                                             final StatementHandler handler) {

        if (handler == null) {
            throw new IllegalArgumentException("Invalid handler: " + handler);
        } // end of if

        return register(id, new ConnectionHandler.Default(handler));
    } // end of register

    /**
     * Unregisters specified handler.
     *
     * @param id Handler ID
     * @return Handler, or null if none
     * @see #register
     */
    public static ConnectionHandler unregister(final String id) {
        if (id == null || id.length() == 0) {
            return null; // Not possible
        } // end of if

        return handlers.remove(id);
    } // end of unregister

    /** Returns prepared properties. */
    private static Properties props(final Property[] info) {
        final Properties ps = new Properties();
        
        for (final Property p : info) {
            ps.put(p.name, p.value);
        }

        return ps;
    } // end of props

    // --- Inner classes ---

    /**
     * Driver (configuration) property.
     */
    public static final class Property {
        public final String name;
        public final String value;

        /**
         * Bulk constructor.
         *
         * @param name the name of the property
         * @param value the property value
         */
        public Property(String name, String value) {
            this.name = name;
            this.value = value;
        }
    } // end of class Property
} // end of class Driver
