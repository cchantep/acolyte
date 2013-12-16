package acolyte;

import java.io.File;

/**
 * JDBC driver file filter.
 */
final class JDBCDriverFileFilter extends javax.swing.filechooser.FileFilter {
    // --- Constructors ---

    /** 
     * No-arg constructor.
     */
    protected JDBCDriverFileFilter() {
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "JDBC Driver";
    } // end of getDescription

    /**
     * {@inheritDoc}
     */
    public boolean accept(final File f) {
        if (f == null || !f.exists() || !f.canRead()) {
            return false;
        } // end of if

        if (f.isDirectory()) {
            return true;
        } // end of if

        // ---

        final String name = f.getName();

        if (!name.endsWith(".jar")) {
            return false;
        } // end of if

        // ---

        try {
            return JDBC.loadDriver(f.toURL()) != null;
        } catch (java.net.MalformedURLException e) {
            System.err.println("Invalid url to JDBC driver");
            return false;
        } // end of catch
    } // end of accept
} // end of class JDBCDriverFileFilter
