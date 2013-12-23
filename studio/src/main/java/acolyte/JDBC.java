package acolyte;

import java.util.ServiceLoader;
import java.util.Properties;
import java.util.Iterator;

import java.net.URLClassLoader;
import java.net.URL;

import java.sql.SQLException;
import java.sql.Connection;
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

        return iter.next();
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

    // ---

    /**
     * No-arg constructor.
     */
    private JDBC() { }
} // end of class JDBC
