package acolyte;

import java.io.File;

import java.util.ServiceLoader;
import java.util.Properties;
import java.util.Iterator;

import java.net.URLClassLoader;
import java.net.URL;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Driver;

/**
 * JDBC utility.
 *
 * @author Cedric Chantepie
 */
public final class JDBC {

    /**
     * Returns JDBC driver declared in specified JAR.
     *
     * @param jar JAR file
     */
    public static Driver loadDriver(final File jar) {
        try {
            return loadDriver(jar.toURI().toURL());
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        } // end of catch
    } // end of loadDriver

    /**
     * Returns JDBC driver declared in specified JAR.
     *
     * @param jarUrl URL to JAR file
     */
    public static Driver loadDriver(final URL jarUrl) {
        final URLClassLoader driverCl = URLClassLoader.
            newInstance(new URL[] { jarUrl }, null);

        final Iterator<Driver> iter = ServiceLoader.
            load(Driver.class, driverCl).iterator();

        if (!iter.hasNext()) {
            System.err.println("No JDBC driver found: " + jarUrl);
            return null;
        } // end of if

        // ---

        Driver d;
        while (iter.hasNext()) {
            d = iter.next();

            if (driverCl.equals(d.getClass().getClassLoader())) {
                return d; // Ignore driver at system classloader
                // e.g. sun.jdbc.odbc.JdbcOdbcDriver
            } // end of if
        } // end of while

        return null;
    } // end of loadDriver

    /**
     * Returns connection using given |driver|.
     *
     * @param driver JDBC driver
     * @param url JDBC url
     * @param user DB user name
     * @param pass Password for DB user
     */
    public static Connection connect(final Driver driver, final String url,
                                     final String user, final String pass) 
        throws SQLException {
        
        final Properties props = new Properties();

        props.put("user", user);
        props.put("password", pass);

        return driver.connect(url, props);
    } // end of connect

    /**
     * Returns value of specified |column| according its type.
     */
    public static Object getObject(final ResultSet rs,
                                   final String name,
                                   final ColumnType type) throws SQLException {
        
        switch (type) {
        case BigDecimal: return rs.getBigDecimal(name);
        case Boolean: return rs.getBoolean(name);
        case Byte: return rs.getByte(name);
        case Short: return rs.getShort(name);
        case Date: return rs.getDate(name);
        case Double: return rs.getDouble(name);
        case Float: return rs.getFloat(name);
        case Int: return rs.getInt(name);
        case Long: return rs.getLong(name);
        case Time: return rs.getTime(name);
        case Timestamp: return rs.getTimestamp(name);
        default: return rs.getString(name);
        }
    } // end of getObject

    // ---

    /**
     * No-arg constructor.
     */
    private JDBC() { }
} // end of class JDBC
