package acolyte;

import java.util.Properties;
import java.util.ArrayList;
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
    private final List<ColumnType> cols;

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
                        final List<ColumnType> cols,
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
            
            while (rs.next()) {
                ap.append(formatting.rowStart);
                appendValues(ap, charset, formatting, this.cols, rs, 0);
                ap.append(formatting.rowEnd);
            } // end of while
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
                           final ColumnType col) throws SQLException {

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
    static void appendValues(final Appender ap,
                             final Charset charset,
                             final Formatting fmt,
                             final List<ColumnType> cols, 
                             final ResultSet rs,
                             final int index) 
        throws SQLException, UnsupportedEncodingException {

        if (index >= cols.size()) {
            return;
        } // end of if

        // ---

        if (index > 0) {
            ap.append(fmt.valueSeparator);
        } // end of if

        final int pos = index+1;
        final ColumnType col = cols.get(index);

        if (rs.getObject(pos) == null) {
            appendNull(ap, fmt, col);
            appendValues(ap, charset, fmt, cols, rs, index+1);

            return;
        } // end of if

        // ---

        switch (col) {
        case BigDecimal:
            ap.append(String.format(fmt.someBigDecimal, rs.getString(pos)));
            break;

        case Boolean:
            ap.append(String.format(fmt.someBoolean, rs.getBoolean(pos)));
            break;

        case Byte:
            ap.append(String.format(fmt.someByte, rs.getByte(pos)));
            break;

        case Short:
            ap.append(String.format(fmt.someShort, rs.getShort(pos)));
            break;

        case Date:
            ap.append(String.format(fmt.someDate, rs.getDate(pos).getTime()));
            break;

        case Double:
            ap.append(String.format(fmt.someDouble, rs.getDouble(pos)));
            break;

        case Float:
            ap.append(String.format(fmt.someFloat, rs.getFloat(pos)));
            break;

        case Int:
            ap.append(String.format(fmt.someInt, rs.getInt(pos)));
            break;

        case Long:
            ap.append(String.format(fmt.someLong, rs.getLong(pos)));
            break;

        case Time:
            ap.append(String.format(fmt.someTime, rs.getTime(pos).getTime()));
            break;

        case Timestamp:
            ap.append(String.format(fmt.someTimestamp, 
                                    rs.getTimestamp(pos).getTime()));
            break;

        default:
            ap.append(String.format(fmt.someString, 
                                    new String(rs.getString(pos).
                                               getBytes(charset)).
                                    replaceAll("\"", "\\\"")));
            break;
        } // end of switch

        appendValues(ap, charset, fmt, cols, rs, index+1);
    } // end of appendValues

    // ---

    /**
     * CLI runner.
     *
     * @param args Execution arguments : args[0] - JDBC URL, 
     * args[1] - Path to JAR or JDBC driver,
     * args[2] - connexion user, 
     * args[3] - User password, 
     * args[4] - SQL statement, 
     * args[5] - Encoding,
     * args[6] - Output format (either "java" or "scala"),
     * args[7] to args[n] - type(s) of column from 1 to m.
     *
     * @see ColumnType
     */
    public static void main(final String[] args) throws Exception {
        final File config = Studio.preferencesFile();
        final Appender ap = new Appender() {
                public void append(final String s) {
                    System.out.println(s);
                }
            };

        if (config.exists()) {
            FileInputStream in = null;

            try {
                in = new FileInputStream(config);

                final Properties conf = new Properties();

                conf.load(in);

                execWith(ap, conf, args, 0);
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
        } else {
            final Properties conf = new Properties();

            conf.put("jdbc.url", args[0]);
            conf.put("jdbc.driverPath", args[1]);
            conf.put("db.user", args[2]);
            conf.put("password", args[3]);
            conf.put("charset", args[4]);

            execWith(ap, conf, args, 5);
        } // end of else
    } // end of main

    /**
     * Executes
     */
    private static void execWith(final Appender ap,
                                 final Properties config,
                                 final String[] args,
                                 final int argsOffset) throws Exception {

        logger.log(Level.FINER, "config={0}", config);

        final File driverFile = new File(config.getProperty("jdbc.driverPath"));

        if (!driverFile.exists()) {
            throw new RuntimeException("JDBC driver not found: " + 
                                       driverFile.getAbsolutePath());

        } // end of if

        final Driver jdbcDriver = JDBC.loadDriver(driverFile.toURI().toURL());

        logger.log(Level.FINER, "jdbcDriver={0}", jdbcDriver);

        if (jdbcDriver == null) {
            throw new RuntimeException("Cannot load JDBC driver: " + 
                                       driverFile.getAbsolutePath());

        } // end of if

        // ---

        final String jdbcUrl = config.getProperty("jdbc.url");
        final String user = config.getProperty("db.user");
        final String pass = config.getProperty("password");
        final String sql = args[argsOffset];
        final Formatting formatting = Formatting.forName(args[argsOffset+1]);
        final Charset charset = Charset.forName(config.getProperty("charset"));
        final ArrayList<ColumnType> cols = new ArrayList<ColumnType>();

        for (int i = argsOffset+2; i < args.length; i++) {
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
                           List.class, Formatting.class).
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
} // end of class RowFormatter
