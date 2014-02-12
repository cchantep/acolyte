package acolyte;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.File;

import java.nio.charset.Charset;

import java.net.URLClassLoader;
import java.net.URL;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Driver;

/**
 * Format JDBC data as Acolyte syntax.
 *
 * @author Cedric Chantepie
 */
public final class RowFormatter {
    // --- Shared ---

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger("acolyte-studio");

    /**
     * Appender to sysout
     */
    private static final Appender sysAppender = new Appender() {
            public void append(final String s) {
                System.out.print(s);
            }
        };

    // --- Properties ---

    /**
     * JDBC driver
     */
    private final Driver jdbcDriver;

    /**
     * JDBC url
     */
    private final String jdbcUrl;

    /**
     * JDBC user
     */
    private final String user;

    /**
     * User password.
     */
    private final String pass; 

    /**
     * SQL statement
     */
    private final String sql;

    /**
     * Encoding
     */
    private final Charset charset;

    /**
     * Column descriptors
     */
    private final Iterable<ColumnType> cols;

    /**
     * Formatting properties
     */
    private final Formatting formatting;

    // --- Constructors ---

    /**
     * No-arg constructor.
     */
    public RowFormatter(final Driver jdbcDriver,
                        final String jdbcUrl, 
                        final String user, 
                        final String pass, 
                        final String sql, 
                        final Charset charset,
                        final Iterable<ColumnType> cols,
                        final Formatting formatting) {

        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.pass = pass;
        this.sql = sql;
        this.charset = charset;
        this.cols = cols;
        this.formatting = formatting;
    } // end of <init>
        
    // ---

    /**
     * Performs export.
     */
    public void perform(final Appender ap) 
        throws SQLException, UnsupportedEncodingException {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = JDBC.connect(this.jdbcDriver, this.jdbcUrl, 
                               this.user, this.pass);

            stmt = con.createStatement();
            rs = stmt.executeQuery(this.sql);

            appendRows(new ResultIterator(rs), ap, 
                       this.charset, this.formatting, this.cols);

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } // end of catch
            } // end of if

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } // end of catch
            } // end of if

            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } // end of catch
            } // end of if
        } // end of finally
    } // end of perform

    /**
     * Appends a null value.
     */
    static void appendNull(final Appender ap, 
                           final Formatting fmt, 
                           final ColumnType col) {

        switch (col) {
        case BigDecimal:
            ap.append(fmt.noneBigDecimal);
            break;

        case Boolean:
            ap.append(fmt.noneBoolean);
            break;

        case Byte:
            ap.append(fmt.noneByte);
            break;

        case Short:
            ap.append(fmt.noneShort);
            break;

        case Date:
            ap.append(fmt.noneDate);
            break;

        case Double:
            ap.append(fmt.noneDouble);
            break;

        case Float:
            ap.append(fmt.noneFloat);
            break;

        case Int:
            ap.append(fmt.noneInt);
            break;

        case Long:
            ap.append(fmt.noneLong);
            break;

        case Time:
            ap.append(fmt.noneTime);
            break;

        case Timestamp:
            ap.append(fmt.noneTimestamp);
            break;

        default:
            ap.append(fmt.noneString);
            break;
        } // end of switch
    } // end of appendNull

    /**
     * Result set values.
     */
    static void appendValues(final ResultRow rs,
                             final Appender ap,
                             final Charset charset,
                             final Formatting fmt,
                             final Iterator<ColumnType> cols, 
                             final int colIndex) {

        if (!cols.hasNext()) {
            return;
        } // end of if

        // ---

        if (colIndex > 0) {
            ap.append(fmt.valueSeparator);
        } // end of if

        final ColumnType col = cols.next();

        if (rs.isNull(colIndex)) {
            appendNull(ap, fmt, col);
            appendValues(rs, ap, charset, fmt, cols, colIndex+1);

            return;
        } // end of if

        // ---

        switch (col) {
        case BigDecimal:
            ap.append(String.format(fmt.someBigDecimal, 
                                    rs.getString(colIndex)));
            break;

        case Boolean:
            ap.append(String.format(fmt.someBoolean, 
                                    rs.getBoolean(colIndex)));
            break;

        case Byte:
            ap.append(String.format(fmt.someByte, 
                                    rs.getByte(colIndex)));
            break;

        case Short:
            ap.append(String.format(fmt.someShort, 
                                    rs.getShort(colIndex)));
            break;

        case Date:
            ap.append(String.format(fmt.someDate, 
                                    rs.getDate(colIndex).getTime()));
            break;

        case Double:
            ap.append(String.format(fmt.someDouble, 
                                    rs.getDouble(colIndex)));
            break;

        case Float:
            ap.append(String.format(fmt.someFloat, 
                                    rs.getFloat(colIndex)));
            break;

        case Int:
            ap.append(String.format(fmt.someInt, 
                                    rs.getInt(colIndex)));
            break;

        case Long:
            ap.append(String.format(fmt.someLong, 
                                    rs.getLong(colIndex)));
            break;

        case Time:
            ap.append(String.format(fmt.someTime, 
                                    rs.getTime(colIndex).getTime()));
            break;

        case Timestamp:
            ap.append(String.format(fmt.someTimestamp, 
                                    rs.getTimestamp(colIndex).getTime()));
            break;

        default:
            ap.append(String.format(fmt.someString, 
                                    new String(rs.getString(colIndex).
                                               getBytes(charset)).
                                    replaceAll("\"", "\\\"")));
            break;
        } // end of switch

        appendValues(rs, ap, charset, fmt, cols, colIndex+1);
    } // end of appendValues

    /**
     * Appends rows
     */
    protected static void appendRows(final Iterator<ResultRow> it,
                                     final Appender ap,
                                     final Charset charset,
                                     final Formatting fmt,
                                     final Iterable<ColumnType> cols) {

        int i = 0;
        while (it.hasNext()) {
            if (i++ > 0) {
                ap.append("\r\n");
            } // end of if

            ap.append("  ");
            ap.append(fmt.rowStart);
            appendValues(it.next(), ap, charset, fmt, cols.iterator(), 0);
            ap.append(fmt.rowEnd);
        } // end of while

        ap.append(";");
    } // end of appendRows

    // ---

    /**
     * CLI runner.
     *
     * @param args Execution arguments : args[0] - JDBC URL, 
     * args[1] - Path to JAR or JDBC driver,
     * args[2] - DB user, 
     * args[3] - Encoding,
     * args[4] - User password, 
     * args[5] - Output format (either "java" or "scala"),
     * args[6] - SQL statement, 
     * args[7] to args[n] - type(s) of column from 1 to m.
     *
     * @see ColumnType
     */
    public static void main(final String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException();
        } // end of if

        // ---

        if (new File(args[1]).exists()) { // driver path
            final Properties conf = new Properties();

            conf.put("jdbc.url", args[0]);
            conf.put("jdbc.driverPath", args[1]);
            conf.put("db.user", args[2]);
            conf.put("db.charset", args[3]);

            execWith(sysAppender, conf, args, 4);

            return;
        } // end of if

        // ---

        final File config = Studio.preferencesFile();

        if (!config.exists()) {
            throw new IllegalArgumentException("Cannot find configuration");
        } // end of if

        // ---

        FileInputStream in = null;

        try {
            in = new FileInputStream(config);

            final Properties conf = new Properties();

            conf.load(in);

            execWith(sysAppender, conf, args, 0);
        } catch (Exception e) {
            throw new RuntimeException("Fails to load configuration: " +
                                       config.getAbsolutePath(), e);
                
        } finally { 
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } // end of catch
            } // end of if
        } // end of finally
    } // end of main

    /**
     * Executes
     */
    private static void execWith(final Appender ap,
                                 final Properties config,
                                 final String[] args,
                                 final int argsOffset) throws Exception {

        logger.log(Level.FINER, "config = {0}", config);

        final File driverFile = new File(config.getProperty("jdbc.driverPath"));

        if (!driverFile.exists()) {
            throw new RuntimeException("JDBC driver not found: " + 
                                       driverFile.getAbsolutePath());

        } // end of if

        final Driver jdbcDriver = JDBC.loadDriver(driverFile.toURI().toURL());

        logger.log(Level.FINER, "jdbcDriver = {0}", jdbcDriver);

        if (jdbcDriver == null) {
            throw new RuntimeException("Cannot load JDBC driver: " + 
                                       driverFile.getAbsolutePath());

        } // end of if

        // ---

        final String jdbcUrl = config.getProperty("jdbc.url");
        final String user = config.getProperty("db.user");
        final String pass = args[argsOffset];
        final Formatting formatting = Formatting.forName(args[argsOffset+1]);
        final String sql = args[argsOffset+2];
        final Charset charset = Charset.
            forName(config.getProperty("db.charset"));
        final ArrayList<ColumnType> cols = new ArrayList<ColumnType>();

        for (int i = argsOffset+3; i < args.length; i++) {
            final ColumnType t = ColumnType.typeFor(args[i]);

            if (t == null) {
                throw new RuntimeException("Invalid column type: " + args[i]);
            } // end of if

            cols.add(t);
        } // end of for

        if (cols.isEmpty()) {
            throw new RuntimeException("No column descriptor");
        }  // end of if

        // ---

        final URLClassLoader cl = URLClassLoader.
            newInstance(new URL[] { driverFile.toURI().toURL() }, 
                        RowFormatter.class.getClassLoader());

        @SuppressWarnings("unchecked")
        final Class<RowFormatter> clazz = (Class<RowFormatter>) cl.
            loadClass("acolyte.RowFormatter");

        final RowFormatter fmt = clazz.
            getConstructor(Driver.class, String.class, String.class, 
                           String.class, String.class, Charset.class, 
                           Iterable.class, Formatting.class).
            newInstance(jdbcDriver, jdbcUrl, user, pass, 
                        sql, charset, cols, formatting);

        fmt.perform(ap);
    } // end of execWith

    // --- Inner classes ---

    /**
     * Output appender.
     */
    static interface Appender {
        public void append(final String str);
    } // end of interface Appender

    /**
     * Result row.
     */
    static interface ResultRow {
        public String getString(int p);
        public boolean getBoolean(int p);
        public byte getByte(int p);
        public short getShort(int p);
        public java.sql.Date getDate(int p);
        public double getDouble(int p);
        public float getFloat(int p);
        public int getInt(int p);
        public long getLong(int p);
        public java.sql.Time getTime(int p);
        public java.sql.Timestamp getTimestamp(int p);
        public boolean isNull(int p);
    } // end of interface ResultRow

    /**
     * Result row from SQL resultset.
     */
    private final class SqlResultRow implements ResultRow {
        private final ResultSet rs;

        /**
         * Bulk constructor.
         */
        SqlResultRow(final ResultSet rs) {
            this.rs = rs;
        } // end of <init>

        public String getString(int p) { 
            try {
                return rs.getString(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public boolean getBoolean(int p) { 
            try {
                return rs.getBoolean(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public byte getByte(int p) { 
            try {
                return rs.getByte(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public short getShort(int p) {
            try {
                return rs.getShort(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public java.sql.Date getDate(int p) { 
            try {
                return rs.getDate(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public double getDouble(int p) { 
            try {
                return rs.getDouble(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public float getFloat(int p) { 
            try {
                return rs.getFloat(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public int getInt(int p) { 
            try {
                return rs.getInt(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public long getLong(int p) { 
            try {
                return rs.getLong(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public java.sql.Time getTime(int p) { 
            try {
                return rs.getTime(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public java.sql.Timestamp getTimestamp(int p) { 
            try {
                return rs.getTimestamp(p+1); 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }

        public boolean isNull(int p) { 
            try {
                return rs.getObject(p+1) == null; 
            } catch (SQLException e) {
                throw new RuntimeException("Fails to get value", e);
            }
        }
    } // end of class SqlResultRow

    /**
     * Result iterator.
     */
    private final class ResultIterator implements Iterator<ResultRow> {
        private final ResultSet rs;

        /**
         * Bulk constructor.
         */
        ResultIterator(final ResultSet rs) {
            this.rs = rs;
        } // end of <init>

        // ---

        /**
         * {@inheritDoc}
         */
        public void remove() { throw new UnsupportedOperationException(); }
        
        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            try {
                return !this.rs.isLast();
            } catch (Exception e) {
                throw new RuntimeException("Fails to check result", e);
            } // end of catch
        } // end of hasNext

        /**
         * {@inheritDoc}
         */
        public ResultRow next() {
            try {
                this.rs.next();
            } catch (Exception e) {
                throw new RuntimeException("Fails to get next result", e);
            } // end of catch

            return new SqlResultRow(this.rs);
        } // end of next
    } // end of class ResultIterator
} // end of class RowFormatter
