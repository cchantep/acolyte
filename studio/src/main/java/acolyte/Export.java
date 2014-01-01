package acolyte;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.File;

import java.net.URLClassLoader;
import java.net.URL;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Driver;

/**
 * Export live data as Acolyte Java syntax.
 *
 * @author Cedric Chantepie
 */
public final class Export {

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
    private final String encoding;

    /**
     * Column descriptors
     */
    private final List<Integer> cols;

    /**
     * Formatting properties
     */
    private final Properties format;

    // --- Constructors ---

    /**
     * No-arg constructor.
     */
    public Export(final Driver jdbcDriver,
                  final String jdbcUrl, 
                  final String user, 
                  final String pass, 
                  final String sql, 
                  final String encoding,
                  final List<Integer> cols,
                  final Properties format) {

        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.pass = pass;
        this.sql = sql;
        this.encoding = encoding;
        this.cols = cols;
        this.format = format;
    } // end of <init>
        
    // ---

    /**
     * Performs export.
     */
    public void perform() throws SQLException, UnsupportedEncodingException {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = JDBC.connect(this.jdbcDriver, this.jdbcUrl, 
                               this.user, this.pass);

            stmt = con.createStatement();
            rs = stmt.executeQuery(this.sql);
            
            while (rs.next()) {
                System.out.print(format.getProperty("row.start"));
                printValues(this.cols, rs, 0);
                System.out.print(format.getProperty("row.end"));
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

    private void printNull(final int col) throws SQLException {
        switch (col) {
        case 0:
            System.out.print(format.getProperty("bigDecimal.none"));
            break;

        case 1:
            System.out.print(format.getProperty("bool.none"));
            break;

        case 2:
            System.out.print(format.getProperty("byte.none"));
            break;

        case 3:
            System.out.print(format.getProperty("short.none"));
            break;

        case 4:
            System.out.print(format.getProperty("date.none"));
            break;

        case 5:
            System.out.print(format.getProperty("double.none"));
            break;

        case 6:
            System.out.print(format.getProperty("float.none"));
            break;

        case 7:
            System.out.print(format.getProperty("int.none"));
            break;

        case 8:
            System.out.print(format.getProperty("long.none"));
            break;

        case 9:
            System.out.print(format.getProperty("time.none"));
            break;

        case 10:
            System.out.print(format.getProperty("timestamp.none"));
            break;

        default:
            System.out.print(format.getProperty("string.none"));
            break;
        } // end of switch
    } // end of printNull

    private void printValues(final List<Integer> cols, 
                             final ResultSet rs,
                             final int index) 
        throws SQLException, UnsupportedEncodingException {

        if (index >= cols.size()) {
            return;
        }

        // ---

        if (index > 0) {
            System.out.print(format.getProperty("value.separator"));
        } // end of if

        final int pos = index+1;
        final int col = cols.get(index);

        if (rs.getObject(pos) == null) {
            printNull(col);
            printValues(cols, rs, index+1);

            return;
        } // end of if

        // ---

        switch (col) {
        case 0:
            System.out.print(String.format(format.
                                           getProperty("bigDecimal.some"), 
                                           rs.getString(pos)));
            break;

        case 1:
            System.out.print(String.format(format.getProperty("bool.some"), 
                                           rs.getBoolean(pos)));
            break;

        case 2:
            System.out.print(String.format(format.getProperty("byte.some"), 
                                           rs.getByte(pos)));
            break;

        case 3:
            System.out.print(String.format(format.getProperty("short.some"), 
                                           rs.getShort(pos)));
            break;

        case 4:
            System.out.print(String.format(format.getProperty("date.some"), 
                                           rs.getDate(pos).getTime()));
            break;

        case 5:
            System.out.print(String.format(format.getProperty("double.some"), 
                                           rs.getDouble(pos)));
            break;

        case 6:
            System.out.print(String.format(format.getProperty("float.some"), 
                                           rs.getFloat(pos)));
            break;

        case 7:
            System.out.print(String.format(format.getProperty("int.some"), 
                                           rs.getInt(pos)));
            break;

        case 8:
            System.out.print(String.format(format.getProperty("long.some"), 
                                           rs.getLong(pos)));
            break;

        case 9:
            System.out.print(String.format(format.getProperty("time.some"), 
                                           rs.getTime(pos).getTime()));
            break;

        case 10:
            System.out.print(String.format(format.
                                           getProperty("timestamp.some"), 
                                           rs.getTimestamp(pos).getTime()));
            break;

        default:
            System.out.print(String.format(format.getProperty("string.some"), 
                                           new String(rs.getString(pos).
                                                      getBytes(encoding)).
                                           replaceAll("\"", "\\\"")));
            break;
        } // end of switch

        printValues(cols, rs, index+1);
    }

    // ---

    /**
     * CLI runner.
     * Supported column types are: "bigdecimal", "bool", "byte", "short", 
     * "date", "double", "float", "int", "long", "time", "timestamp", "string".
     *
     * @param args Execution arguments : args[0] - JDBC URL, 
     * args[1] - Path to JAR or JDBC driver,
     * args[2] - connexion user, args[3] - User password, 
     * args[4] - SQL statement, args[5] - Encoding,
     * args[6] to args[n] - type(s) of column from 1 to m.
     */
    public static void main(final String[] args) throws Exception {
        final File config = Studio.preferencesFile();

        if (config.exists()) {
            FileInputStream in = null;

            try {
                in = new FileInputStream(config);

                final Properties conf = new Properties();

                conf.load(in);

                execWith(conf, args, 0);
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
            conf.put("encoding", args[4]);

            execWith(conf, args, 5);
        } // end of else
    } // end of main

    /**
     * Executes
     */
    private static void execWith(final Properties config,
                                 final String[] args,
                                 final int argsOffset) throws Exception {

        System.out.println("config=" + config);

        final File driverFile = new File(config.getProperty("jdbc.driverPath"));

        if (!driverFile.exists()) {
            throw new RuntimeException("JDBC driver not found: " + 
                                       driverFile.getAbsolutePath());

        } // end of if

        final Driver jdbcDriver = JDBC.loadDriver(driverFile.toURI().toURL());

        System.out.println("jdbcDriver=" + jdbcDriver);

        if (jdbcDriver == null) {
            throw new RuntimeException("Cannot load JDBC driver: " + 
                                       driverFile.getAbsolutePath());

        } // end of if

        // ---

        final String jdbcUrl = config.getProperty("jdbc.url");
        final String user = config.getProperty("db.user");
        final String pass = config.getProperty("password");
        final String sql = args[argsOffset];
        final String encoding = config.getProperty("encoding");
        final ArrayList<ColumnType> cols = new ArrayList<ColumnType>();

        for (int i = argsOffset+1; i < args.length; i++) {
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

        final Properties props = new Properties();

        props.put("row.start", ".append(");
        props.put("row.end", ")\r\n");
        props.put("value.separator", ", ");
        props.put("bigDecimal.some", "new java.math.BigDecimal(\"%s\")");
        props.put("bool.some", "%b");
        props.put("byte.some", "%d.toByte");
        props.put("short.some", "%d.toShort");
        props.put("date.some", "new java.sql.Date(%dl)");
        props.put("double.some", "%fd");
        props.put("float.some", "%f");
        props.put("int.some", "%d");
        props.put("long.some", "%dl");
        props.put("time.some", "new java.sql.Time(%dl)");
        props.put("timestamp.some", "new java.sql.Time(%dl)");
        props.put("string.some", "\"%s\"");
        props.put("bigDecimal.none", "null");
        props.put("bool.none", "null.asInstanceOf[Boolean]");
        props.put("byte.none", "null.asInstanceOf[Byte]");
        props.put("short.none", "null.asInstanceOf[Short]");
        props.put("date.none", "null");
        props.put("double.none", "null.asInstanceOf[Double]");
        props.put("float.none", "null.asInstanceOf[Float]");
        props.put("int.none", "null.asInstanceOf[Int]");
        props.put("long.none", "null.asInstanceOf[Long]");
        props.put("time.none", "null");
        props.put("timestamp.none", "null");
        props.put("string.none", "null");

        final URLClassLoader cl = URLClassLoader.
            newInstance(new URL[] { driverFile.toURI().toURL() }, 
                        Export.class.getClassLoader());

        @SuppressWarnings("unchecked")
        final Class<Export> clazz = (Class<Export>) cl.
            loadClass("acolyte.Export");

        final Export export = clazz.
            getConstructor(Driver.class, String.class, String.class, 
                           String.class, String.class, String.class, 
                           List.class, Properties.class).
            newInstance(jdbcDriver, jdbcUrl, user, pass, 
                        sql, encoding, cols, props);

        export.perform();
    } // end of execWith
} // end of class Export
