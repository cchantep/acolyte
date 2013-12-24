package acolyte;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;

import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;

import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Driver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Color;

import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import javax.swing.JPasswordField;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.LayoutStyle;
import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.GroupLayout.Alignment;

import javax.swing.filechooser.FileFilter;

import javax.swing.table.TableColumn;

import melasse.StringLengthToBooleanTransformer;
import melasse.NegateBooleanTransformer;
import melasse.UnaryFunction;
import melasse.BindingOptionMap;
import melasse.TextBindingKey;
import melasse.BindingKey;
import melasse.Binder;

/**
 * Acolyte studio UI.
 *
 * @author Cedric Chantepie
 */
public final class Studio {
    // --- Shared ---

    /**
     * Do nothing - only returns false.
     */
    private static final Callable<Boolean> retFalse = new Callable<Boolean>() {
        public Boolean call() { return false; }
    };

    // --- Properties ---

    /**
     * Executor service
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * Runtime configuration
     */
    private final Properties conf = new Properties();

    /**
     * Main model
     */
    private final StudioModel model = new StudioModel();

    /**
     * Callable configuration persistence
     */
    private final Callable<Boolean> saveConf;

    // --- Constructors ---

    /**
     * No-arg constructor.
     */
    public Studio() {
        final File userDir = new File(System.getProperty("user.home"));
        final File prefDir = (userDir.canWrite()) 
            ? new File(userDir, ".acolyte") : null;

        if (prefDir != null && !prefDir.exists()) {
            prefDir.mkdir();
        } // end of if

        final File prefFile = 
            (prefDir != null && prefDir.canRead() && prefDir.canWrite())
            ? new File(prefDir, "studio.properties") : null;

        if (prefFile.exists()) {
            reloadConfig(prefFile);
        } // end of if

        this.saveConf = (prefFile == null) 
            ? retFalse : new Callable<Boolean>() {
            public Boolean call() {
                OutputStreamWriter w = null;

                try {
                    final FileOutputStream out = new FileOutputStream(prefFile);
                    w = new OutputStreamWriter(out, "UTF-8");

                    conf.store(w, "Acolyte studio configuration");
                    w.flush();

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (w != null) {
                        try {
                            w.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } // end of catch
                    } // end of if
                } // end of finally

                return false;
            }
        };

    } // end of <init>

    // ---

    /**
     * Sets up UI.
     */
    public void setUp() {
        final JFrame frm = new JFrame("Acolyte Studio");
        final Dimension screenSize = java.awt.Toolkit.
            getDefaultToolkit().getScreenSize();

        frm.setMinimumSize(new Dimension((int) (screenSize.getWidth()/2.4d),
                                         (int) (screenSize.getHeight()/1.2f)));
        frm.setPreferredSize(frm.getMinimumSize());

        // Prepare config
        final String driverPath = conf.getProperty("jdbc.driverPath");
        final String jdbcUrl = conf.getProperty("jdbc.url");
        final String dbUser = conf.getProperty("db.user");

        final JTextField driverField = new JTextField();
        driverField.setEditable(false);
        
        if (driverPath != null && driverPath.length() > 0) {
            final File driverFile = new File(driverPath);

            try {
                this.model.setDriver(JDBC.loadDriver(driverFile));
                driverField.setText(driverPath);
            } catch (Exception e) {
                e.printStackTrace();
            } // end of catch
        } else {
            driverField.setForeground(Color.LIGHT_GRAY);
            driverField.setText("Path to driver.jar");
        } // end of else

        // 
        final Container content = frm.getContentPane();
        final GroupLayout layout = new GroupLayout(content);
        final JLabel confLabel = 
            new JLabel("<html><b>JDBC access</b></html>");
        final JLabel confValidated = new JLabel("(validated)");
        confValidated.setForeground(new Color(0, 124, 0));
        final JLabel driverLabel = new JLabel("JDBC driver");
        de.sciss.syntaxpane.DefaultSyntaxKit.initKit();
        final JLabel urlLabel = new JLabel("JDBC URL");
        final JTextField urlField = new JTextField(jdbcUrl);
        final JLabel invalidUrl = new JLabel("Driver doesn't accept URL.");
        invalidUrl.setForeground(Color.RED);
        final JLabel userLabel = new JLabel("DB user");
        final JTextField userField = new JTextField(dbUser);
        final JLabel passLabel = new JLabel("Password");
        final JPasswordField passField = new JPasswordField();
        final JLabel invalidCred = 
            new JLabel("Can't connect using these credentials");
        invalidCred.setForeground(Color.RED);
        invalidCred.setVisible(false);
        final JLabel sqlLabel = new JLabel("<html><b>SQL query</b></html>");
        final JEditorPane sqlArea = new JEditorPane();
        final JScrollPane sqlPanel = 
            new JScrollPane(sqlArea, 
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sqlArea.setContentType("text/sql");
        final JLabel colLabel = 
            new JLabel("<html><b>Column mappings</b></html>");
        final JTextField colName = new JTextField();
        final JTable colTable = new JTable();
        final JTable resTable = new JTable();
        final JScrollPane colPanel = new JScrollPane(colTable);
        final JLabel resLabel = new JLabel("<html><b>Mapped result</b></html>");
        final JScrollPane resPanel = 
            new JScrollPane(resTable, 
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        final JComboBox colTypes = 
            new JComboBox(Export.colTypes.toArray());
        colTypes.setSelectedItem("string");

        final Runnable selectDriver = new Runnable() {
                public void run() {
                    final JFileChooser chooser = new JFileChooser(new File("."));

                    for (final FileFilter ff : chooser.getChoosableFileFilters()) chooser.removeChoosableFileFilter(ff); // clean choosable filters

                    chooser.setDialogTitle("Chooser JDBC driver");
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setFileFilter(new JDBCDriverFileFilter());

                    final int choice = chooser.showOpenDialog(frm);
                    if (choice != JFileChooser.APPROVE_OPTION) {
                        return;
                    } // end of if

                    // ---

                    final File driverFile = chooser.getSelectedFile();
                    final String driverPath = driverFile.getAbsolutePath();

                    driverField.setForeground(colName.getForeground());
                    driverField.setText(driverPath);

                    try {
                        model.setDriver(JDBC.loadDriver(driverFile));

                        // !! Side-effect
                        conf.put("jdbc.driverPath", driverPath);
                        updateConfig();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        final AbstractAction chooseDriver = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    selectDriver.run();
                }
            };
        chooseDriver.putValue(Action.NAME, "Choose...");
        chooseDriver.putValue(Action.SHORT_DESCRIPTION, "Choose JDBC driver");
        chooseDriver.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
        final JButton driverBut = new JButton(chooseDriver);
        driverField.addMouseListener(new MouseAdapter() {
                public void mouseClicked(final MouseEvent e) {
                    selectDriver.run();
                }
            });

        final JLabel checkConLabel = new JLabel();
        final AbstractAction checkCon = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    final ImageIcon waitIco = 
                        new ImageIcon(this.getClass().
                                      getResource("loader.gif"));

                    model.setProcessing(true);
                    model.setConnectionValidated(false);

                    checkConLabel.setIcon(waitIco);
                    waitIco.setImageObserver(checkConLabel);

                    final Callable<Boolean> c = new Callable<Boolean>() {
                        public Boolean call() {
                            Connection con = null;

                            try {
                                con = JDBC.connect(model.getDriver(), 
                                                   model.getUrl(),
                                                   model.getUser(), 
                                                   model.getPassword());

                                return con != null;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return false;
                            } finally {
                                if (con != null) {
                                    try { 
                                        con.close(); 
                                    } catch (Exception ex) { }
                                } // end of if
                            } // end of finally
                        } // end of call
                    };

                    final UnaryFunction<Callable<Boolean>,Boolean> tx = 
                        new UnaryFunction<Callable<Boolean>,Boolean>() {
                        public Boolean apply(final Callable<Boolean> x) {
                            Boolean res = null;

                            try {
                                res = x.call();
                            } catch (Exception e) { }

                            final boolean v = (res == null) ? false : res;

                            checkConLabel.setIcon(null);
                            waitIco.setImageObserver(null);
                            model.setConnectionValidated(v);
                            invalidCred.setVisible(!v);

                            return v;
                        }
                    };

                    SwingWorker<Boolean,Boolean> sw = studioProcess(5, c, tx);

                    sw.execute();
                }
            };
        checkCon.putValue(Action.NAME, "Check connection...");
        checkCon.putValue(Action.SHORT_DESCRIPTION, 
                          "Check connection to database");
        checkCon.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
        final JButton checkConBut = new JButton(checkCon);
        final AbstractAction checkConFromField = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    if (!checkCon.isEnabled()) {
                        return;
                    } // end of if

                    // ---

                    checkCon.actionPerformed(e);
                }
            };
        urlField.setAction(checkConFromField);
        userField.setAction(checkConFromField);
        passField.setAction(checkConFromField);

        final AbstractAction addCol = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    final String name = colName.getText();
                    final String type = (String) colTypes.getSelectedItem();

                    final TableColumn col = new TableColumn();
                    col.setHeaderValue(name);

                    colTable.addColumn(col);

                    colName.setText("");
                    colName.grabFocus();
                }
            };
        addCol.putValue(Action.NAME, "Add column");
        addCol.putValue(Action.SHORT_DESCRIPTION, "Add column to list");
        addCol.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        final JButton colBut = new JButton(addCol);

        final AbstractAction testSql = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    System.out.println("-> test SQL");

                    model.setProcessing(true);

                    final String sql = sqlArea.getText();

                    testSql(frm, sql, 100);
                }
            };
        testSql.putValue(Action.NAME, "Test query");
        testSql.putValue(Action.SHORT_DESCRIPTION, 
                         "Test SQL query and get raw result");
        testSql.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
        final JButton testBut = new JButton(testSql);

        sqlPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        final JLabel extractLabel = 
            new JLabel("Fetch results executing query and", 
                       SwingConstants.RIGHT);
        extractLabel.setForeground(Color.DARK_GRAY);
        final AbstractAction extract = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) { }
            };
        extract.putValue(Action.NAME, "Extract");
        extract.putValue(Action.SHORT_DESCRIPTION, 
                               "Extract result using column mappings");
        extract.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
        final JButton extractBut = new JButton(extract);

        final JComboBox convertFormats = 
            new JComboBox(new String[] { "Java", "Scala" });
        final AbstractAction convert = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) { }
            };
        convert.putValue(Action.NAME, "Convert");
        convert.putValue(Action.SHORT_DESCRIPTION, 
                         "Convert extracted data to some Acolyte syntax");
        convert.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        final JButton convertBut = new JButton(convert);

        content.setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

        final JSeparator zeroSep = new JSeparator();
        final JSeparator firstSep = new JSeparator();
        final JSeparator secondSep = new JSeparator();
        final GroupLayout.SequentialGroup vgroup = 
            layout.createSequentialGroup().
            addGroup(layout.createParallelGroup(Alignment.BASELINE).
                     addComponent(confLabel,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(confValidated,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
            addGroup(layout.createParallelGroup(Alignment.BASELINE).
                     addComponent(driverLabel).
                     addComponent(driverField).
                     addComponent(driverBut)).
            addGroup(layout.createParallelGroup(Alignment.BASELINE).
                     addComponent(urlLabel).
                     addComponent(urlField)).
            addComponent(invalidUrl).
            addGroup(layout.createParallelGroup(Alignment.BASELINE).
                     addComponent(userLabel).
                     addComponent(userField).
                     addComponent(passLabel).
                     addComponent(passField)).
            addGroup(layout.createParallelGroup(Alignment.TRAILING).
                     addComponent(invalidCred).
                     addComponent(checkConLabel).
                     addComponent(checkConBut)).
            addComponent(zeroSep,
                         GroupLayout.PREFERRED_SIZE, 
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addComponent(sqlLabel).
            addComponent(sqlPanel, 
                         (int) screenSize.getHeight()/10,
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addComponent(testBut, 
                         GroupLayout.PREFERRED_SIZE,
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addComponent(firstSep,
                         GroupLayout.PREFERRED_SIZE, 
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addComponent(colLabel).
            addGroup(layout.
                     createParallelGroup(Alignment.BASELINE).
                     addComponent(colName).
                     addComponent(colTypes).
                     addComponent(colBut)).
            addComponent(colPanel,
                         (int) screenSize.getHeight()/16,
                         GroupLayout.DEFAULT_SIZE,
                         (int) screenSize.getHeight()/16).
            addGroup(layout.
                     createParallelGroup(Alignment.BASELINE).
                     addComponent(extractLabel).
                     addComponent(extractBut)).
            addComponent(secondSep,
                         GroupLayout.PREFERRED_SIZE, 
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addComponent(resLabel).
            addComponent(resPanel,
                         (int) (screenSize.getHeight()/6.5f),
                         GroupLayout.DEFAULT_SIZE,
                         Short.MAX_VALUE).
            addGroup(layout.
                     createParallelGroup(Alignment.BASELINE).
                     addComponent(convertFormats).
                     addComponent(convertBut));

        final GroupLayout.ParallelGroup hgroup = 
            layout.createParallelGroup(Alignment.LEADING).
            addGroup(layout.createSequentialGroup().
                     addComponent(confLabel,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(confValidated, 
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE)).
            addGroup(layout.createSequentialGroup().
                     addComponent(driverLabel,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(driverField,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE).
                     addComponent(driverBut)).
            addGroup(layout.createSequentialGroup().
                     addComponent(urlLabel).
                     addComponent(urlField,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE)).
            addComponent(invalidUrl).
            addGroup(layout.createSequentialGroup().
                     addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                     GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).
                     addComponent(checkConLabel,
                                  GroupLayout.PREFERRED_SIZE, 
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(checkConBut,
                                  GroupLayout.PREFERRED_SIZE, 
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
            addComponent(zeroSep).
            addGroup(layout.createSequentialGroup().
                     addComponent(userLabel).
                     addComponent(userField,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE).
                     addComponent(passLabel).
                     addComponent(passField,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE)).
            addComponent(sqlLabel).
            addComponent(sqlPanel).
            addGroup(layout.createSequentialGroup().
                     addComponent(invalidCred,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE).
                     addComponent(testBut,
                                  GroupLayout.PREFERRED_SIZE, 
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
            addComponent(firstSep).
            addComponent(colLabel).
            addGroup(layout.createSequentialGroup().
                     addComponent(colName,
                                  GroupLayout.PREFERRED_SIZE, 
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE).
                     addComponent(colTypes,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(colBut,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
            addComponent(colPanel).
            addGroup(layout.createSequentialGroup().
                     addComponent(extractLabel,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE).
                     addComponent(extractBut,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
            addComponent(secondSep).
            addComponent(resLabel).
            addComponent(resPanel).
            addGroup(layout.createSequentialGroup().
                     addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                     GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).
                     addComponent(convertFormats,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(convertBut,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE));
                     
        layout.setVerticalGroup(vgroup);
        layout.setHorizontalGroup(hgroup);

        layout.linkSize(SwingConstants.HORIZONTAL, 
                        driverLabel, urlLabel, userLabel);

        layout.linkSize(SwingConstants.VERTICAL, checkConBut, checkConLabel);

        frm.pack();
        frm.setLocationRelativeTo(null);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setVisible(true);

        // Sets up model bindings
        final BindingOptionMap txtOpts = new BindingOptionMap().
            add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE);

        Binder.bind("text", userField, "user", this.model, txtOpts);
        Binder.bind("text", passField, "password", this.model, txtOpts);
        Binder.bind("text", urlField, "url", this.model, txtOpts);

        // Sets up binding to disable action while processing
        final BindingOptionMap negOpts = new BindingOptionMap().
            add(BindingKey.INPUT_TRANSFORMER, 
                NegateBooleanTransformer.getInstance());
        
        Binder.bind("processing", this.model, "enabled", chooseDriver, negOpts);
        Binder.bind("processing", this.model, "enabled", driverLabel, negOpts);
        Binder.bind("processing", this.model, "enabled", urlField, negOpts);
        Binder.bind("processing", this.model, "enabled", userField, negOpts);
        Binder.bind("processing", this.model, "enabled", passField, negOpts);
        
        Binder.bind("processing", this.model, "enabled[]", checkCon, negOpts);
        Binder.bind("processing", this.model, "enabled[]", testSql, negOpts);
        Binder.bind("processing", this.model, "enabled[]", extract, negOpts);
        Binder.bind("processing", this.model, "enabled[]", convert, negOpts);
        
        // Sets up binding to disable action until connection validated
        Binder.bind("connectionValidated", this.model, "enabled[]", testSql, 
                    BindingOptionMap.targetModeOptions);
        
        Binder.bind("connectionValidated", this.model, "enabled[]", extract, 
                    BindingOptionMap.targetModeOptions);
        
        Binder.bind("connectionValidated", this.model, "enabled[]", convert, 
                    BindingOptionMap.targetModeOptions);

        Binder.bind("connectionValidated", this.model, 
                    "visible", confValidated, 
                    BindingOptionMap.targetModeOptions);
        
        // Sets up bindings
        final BindingOptionMap txtLenOpts = 
            new BindingOptionMap().
            add(BindingKey.INPUT_TRANSFORMER,
                StringLengthToBooleanTransformer.
                getTrimmingInstance()).
            add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE);
    
        colName.setAction(addCol);
	Binder.bind("text", colName, "enabled", addCol, txtLenOpts);

	Binder.bind("connectionConfig", model, "visible", invalidUrl,
		    new BindingOptionMap().
		    add(BindingKey.INPUT_TRANSFORMER,
                        new UnaryFunction<Long,Boolean>() {
                            public Boolean apply(final Long r) {
                                final Driver d = model.getDriver();
                                final String t = model.getUrl();

                                try {
                                    return (t != null && t.length() > 0 &&
                                            d != null && !d.acceptsURL(t));

                                } catch (Exception e) { }

                                return false;
                            }
                    }));

        // Bindings for check connection action
	Binder.bind("connectionConfig", model, "enabled[]", checkCon,
		    new BindingOptionMap().
		    add(BindingKey.INPUT_TRANSFORMER, 
                        new UnaryFunction<Long,Boolean>() {
                            public Boolean apply(final Long r) {
                                final Driver d = model.getDriver();
                                final String t = model.getUrl();

                                // !! Side-effect
                                updateConfig();

                                try {
                                    return (d != null && d.acceptsURL(t));
                                } catch (Exception e) { }

                                return false;
                            }
                    }));

        Binder.bind("user", this.model, "enabled[]", checkCon, txtLenOpts);
        Binder.bind("password", this.model, "enabled[]", checkCon, txtLenOpts);

        // Bindings for SQL test action
	Binder.bind("text", sqlArea, "enabled[]", testSql, txtLenOpts);
        Binder.bind("enabled", testSql, "enabled[]", extract, null);
    } // end of setUp

    /**
     * Studio process.
     */
    private <T> SwingWorker<T,T> studioProcess(final int timeout, final Callable<T> c, final UnaryFunction<Callable<T>,T> f) {

        final Callable<T> cw = new Callable<T>() {
            public T call() {
                final FutureTask<T> t = new FutureTask<T>(c);

                try {
                    executor.submit(t);
                    return t.get(timeout, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } // end of catch
                
                return null;
            }
        };

        return new SwingWorker<T,T>() {
            public T doInBackground() throws Exception {
                try {
                    f.apply(cw);
                } finally {
                    model.setProcessing(false);
                } // end of finally
                
                return null;
            }
        };
    } // end of studioProcess

    /**
     * Reloads configuration from file.
     */
    private void reloadConfig(final File f) {
        InputStreamReader r = null;

        try {
            final FileInputStream in = new FileInputStream(f);
            r = new InputStreamReader(in, "UTF-8");

            this.conf.load(r);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } // end of catch
            } // end of if
        } // end of finally
    } // end of reloadConfig

    /**
     * Updates application configuration.
     */
    private void updateConfig() {
        // JDBC URL
        final String u = this.model.getUrl();
        final String url = (u != null) ? u.trim() : null;

        if (url == null || url.length() == 0) {
            this.conf.remove("jdbc.url");
        } else {
            this.conf.put("jdbc.url", url);
        } // end of else

        // DB user name
        final String n = this.model.getUser();
        final String user = (n != null) ? n.trim() : null;

        if (user == null || user.length() == 0) {
            this.conf.remove("db.user");
        } else {
            this.conf.put("db.user", user);
        } // end of else

        try {
            this.saveConf.call();
        } catch (Exception e) {
            e.printStackTrace();
        } // end of catch
    } // end of updateConfig

    /**
     * Test SQL query.
     */
    private void testSql(final JFrame frm, final String sql, final int limit) {
        System.out.println("sql=" + sql);

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = JDBC.connect(model.getDriver(), model.getUrl(),
                               model.getUser(), model.getPassword());
                        
            stmt = con.createStatement();
            stmt.setMaxRows(limit);
            rs = stmt.executeQuery(sql);

            if (!rs.next()) {
                JOptionPane.
                    showMessageDialog(frm, "No result fetched for this query.",
                                      "No result", JOptionPane.WARNING_MESSAGE);
                                              
                return;
            } // end of if

            // ---

            final ResultSetMetaData meta = rs.getMetaData();
            final int c = meta.getColumnCount();
            final JDialog dlg = new JDialog(frm, "Test result");
            final Vector<String> ns = new Vector<String>();

            for (int p = 1; p <= c; p++) {
                final String n = meta.getColumnLabel(p);
                ns.add((n == null || "".equals(n)) ? "col" + p : n);
            } // end of for

            final Vector<Vector<String>> rows = new Vector<Vector<String>>();
            Vector<String> row = new Vector<String>();

            for (int p = 1; p <= c; p++) {
                row.add(rs.getString(p));
            } // end of for

            rows.add(row);

            while (rs.next()) {
                row = new Vector<String>();
                
                for (int p = 1; p <= c; p++) {
                    row.add(rs.getString(p));
                } // end of for

                rows.add(row);
            } // end of if

            final JTable table = new JTable(rows, ns);
            final JScrollPane pane = new JScrollPane(table);

            dlg.setContentPane(pane);
            dlg.pack();
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (rs != null) {
                try { 
                    rs.close(); 
                } catch (Exception ex) { }
            } // end of if

            if (stmt != null) {
                try { 
                    stmt.close(); 
                } catch (Exception ex) { }
            } // end of if

            if (con != null) {
                try { 
                    con.close(); 
                } catch (Exception ex) { }
            } // end of if

            model.setProcessing(false);
        } // end of finally
    } // end of testSql

    // ---

    /**
     * Starts studio application.
     */
    public static void main(final String[] args) throws Exception {
        final Studio studio = new Studio();

        studio.setUp();
    } // end of main
} // end of class Studio
