package acolyte;

import java.util.ServiceLoader;
import java.util.Iterator;

import java.net.URLClassLoader;
import java.net.URL;

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

    // ---

    /**
     * No-arg constructor.
     */
    private JDBC() { }
} // end of class JDBC
