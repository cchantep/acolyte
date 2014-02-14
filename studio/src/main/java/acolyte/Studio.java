package acolyte;

import java.math.BigDecimal;

import java.util.LinkedHashMap;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map;

import java.text.MessageFormat;

import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;

import java.nio.charset.Charset;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Driver;
import java.sql.Time;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.Color;
import java.awt.Image;

import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
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
import javax.swing.UIManager;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JLabel;

import javax.swing.GroupLayout.Alignment;

import javax.swing.filechooser.FileFilter;

import javax.swing.text.Document;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;

import org.apache.commons.lang3.tuple.ImmutablePair;

import melasse.StringLengthToBooleanTransformer;
import melasse.IntegerToBooleanTransformer;
import melasse.NumberToStringTransformer;
import melasse.NegateBooleanTransformer;
import melasse.BindingOptionMap;
import melasse.QuietWrapAction;
import melasse.TextBindingKey;
import melasse.UnaryFunction;
import melasse.BindingKey;
import melasse.Binder;

import melasse.PropertyChangeSupport.PropertyEditSession;

/**
 * Acolyte studio UI.
 *
 * @author Cedric Chantepie
 */
public final class Studio {
    // --- Shared ---

    /**
     * Do nothing - only returns false
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
        final File prefFile = preferencesFile();

        if (prefFile.exists()) {
            Configuration.loadConfig(this.conf, prefFile);
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
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();

        frm.setMinimumSize(new Dimension((int) (screenSize.getWidth()/2.4d),
                                         (int) (screenSize.getHeight()/1.2f)));
        frm.setPreferredSize(frm.getMinimumSize());

        // Prepare config
        final String driverPath = conf.getProperty("jdbc.driverPath");
        final String jdbcUrl = conf.getProperty("jdbc.url");
        final String dbUser = conf.getProperty("db.user");
        final String dbCharset = conf.getProperty("db.charset");
        final Charset charset =
            (dbCharset != null && Charset.isSupported(dbCharset))
            ? Charset.forName(dbCharset) : Charset.defaultCharset();

        final JTextField driverField = new JTextField();
        driverField.setEditable(false);

        if (driverPath != null) {
            final File driverFile = new File(driverPath);

            try {
                this.model.setDriver(JDBC.loadDriver(driverFile));
                driverField.setText(driverPath);
            } catch (Throwable e) {
                e.printStackTrace();
            } // end of catch
        } else {
            driverField.setForeground(Color.LIGHT_GRAY);
            driverField.setText("Path to driver.jar");
        } // end of else

        //
        final Container content = frm.getContentPane();
        final GroupLayout layout = new GroupLayout(content);

        // Configuration UI
        final JLabel confLabel = new JLabel("<html><b>JDBC access</b></html>");
        final JLabel confValidated = new JLabel("(validated)");
        confValidated.setForeground(new Color(0, 124, 0));
        final JLabel driverLabel = new JLabel("JDBC driver");
        de.sciss.syntaxpane.DefaultSyntaxKit.initKit();
        final JLabel urlLabel = new JLabel("JDBC URL");
        final JTextField urlField = new JTextField(jdbcUrl);
        urlLabel.setLabelFor(urlField);
        final JLabel invalidUrl = new JLabel("Driver doesn't accept URL.");
        invalidUrl.setForeground(Color.RED);
        invalidUrl.setVisible(false);
        final JLabel userLabel = new JLabel("DB user");
        final JTextField userField = new JTextField(dbUser);
        userLabel.setLabelFor(userField);
        final JLabel passLabel = new JLabel("Password");
        final JPasswordField passField = new JPasswordField();
        passLabel.setLabelFor(passField);
        final JLabel charsetLabel = new JLabel("Charset");
        charsetLabel.setToolTipText("Character set");
        final JComboBox charsets =
            new JComboBox(Charset.availableCharsets().values().toArray());
        charsets.setSelectedItem(charset);
        final JLabel invalidCred =
            new JLabel("Can't connect using these credentials");
        invalidCred.setForeground(Color.RED);
        invalidCred.setVisible(false);

        final AbstractAction chooseDriver = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    chooseDriver(frm, driverField);
                }
            };
        chooseDriver.putValue(Action.NAME, "Choose...");
        chooseDriver.putValue(Action.SHORT_DESCRIPTION, "Choose JDBC driver");
        chooseDriver.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
        final JButton driverBut = new JButton(chooseDriver);
        driverField.addMouseListener(new MouseAdapter() {
                public void mouseClicked(final MouseEvent e) {
                    chooseDriver(frm, driverField);
                }
            });

        final JLabel checkConLabel = new JLabel();
        final AbstractAction checkCon = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    model.setProcessing(true);
                    model.setConnectionValidated(false);

                    final ImageIcon waitIco = setWaitIcon(checkConLabel);
                    final Callable<Boolean> c = new Callable<Boolean>() {
                        public Boolean call() {
                            Connection con = null;

                            try {
                                con = getConnection();

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

                    studioProcess(5, c, tx).execute();
                }
            };
        checkCon.putValue(Action.NAME, "Check connection...");
        checkCon.putValue(Action.SHORT_DESCRIPTION,
                          "Check connection to database");
        checkCon.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
        final JButton checkConBut = new JButton(checkCon);
        final Action checkConFromField = new QuietWrapAction(checkCon);
        urlField.setAction(checkConFromField);
        userField.setAction(checkConFromField);
        passField.setAction(checkConFromField);

        // SQL test UI
        final JLabel sqlLabel = new JLabel("<html><b>SQL query</b></html>");
        final JEditorPane sqlArea = new JEditorPane();
        final JScrollPane sqlPanel =
            new JScrollPane(sqlArea,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sqlPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        sqlArea.setContentType("text/sql"); // should be after sqlPanel setup

        final JLabel testSqlLabel = new JLabel("(Ctrl+T)");
        testSqlLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        testSqlLabel.setForeground(Color.DARK_GRAY);
        final AbstractAction testSql = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    model.setProcessing(true);

                    final ImageIcon waitIco = setWaitIcon(testSqlLabel);
                    final String sql = sqlArea.getText();

                    try {
                        testSql(frm, sql, 100);
                    } finally {
                        model.setProcessing(false);
                        waitIco.setImageObserver(null);
                        testSqlLabel.setIcon(null);
                    } // end of finally
                }
            };
        testSql.putValue(Action.NAME, "Test query");
        testSql.putValue(Action.SHORT_DESCRIPTION,
                         "Test SQL query and get raw result");
        testSql.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
        final JButton testBut = new JButton(testSql);
        sqlArea.addKeyListener(new KeyAdapter() {
                public void keyReleased(final KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_T && e.isControlDown())
                        testBut.doClick();
                }
            });

        // Column mappings UI
        final JLabel colLabel =
            new JLabel("<html><b>Column mappings</b></html>");
        final JTextField colName = new JTextField();
        final Vector<String> colNames = new Vector<String>();
        final Vector<Vector<ColumnType>> colData =
            new Vector<Vector<ColumnType>>();
        final NotEditableTableModel colModel =
            new NotEditableTableModel(colData, colNames);
        final JTable colTable = new JTable(colModel);
        colTable.setRowSelectionAllowed(false);
        final int colTableHeight = 5 + (colTable.getRowHeight() * 2);
        final JScrollPane colPanel = new JScrollPane(colTable);
        final JComboBox colTypes = new JComboBox(ColumnType.values());
        colTypes.setSelectedItem(ColumnType.String);
        colData.add(new Vector<ColumnType>());

        final AbstractAction addCol = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    final String name = colName.getText();

                    if (colNames.contains(name)) {
                        JOptionPane.
                            showMessageDialog(frm, "Column is already mapped",
                                              "Duplicate column",
                                              JOptionPane.ERROR_MESSAGE);

                        return;
                    } // end of if

                    // ---

                    final ColumnType type =
                        (ColumnType) colTypes.getSelectedItem();
                    final Vector<ColumnType> cd = colData.elementAt(0);
                    final PropertyEditSession s = colModel.willChange();

                    colNames.add(name);
                    cd.add(type);

                    colModel.fireTableStructureChanged();
                    colModel.fireTableDataChanged();
                    s.propertyDidChange();

                    colName.setText("");
                    colName.grabFocus();
                }
            };
        addCol.putValue(Action.NAME, "Add column");
        addCol.putValue(Action.SHORT_DESCRIPTION, "Add column to list");
        addCol.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        final JButton colBut = new JButton(addCol);
        colName.setAction(new QuietWrapAction(addCol));

        final AbstractAction removeCol = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    final int col = colTable.getSelectedColumn();

                    if (col == -1) {
                        return; // No selection
                    } // end of if

                    // ---

                    final Vector<ColumnType> cd = colData.get(0);
                    final PropertyEditSession s = colModel.willChange();

                    colNames.removeElementAt(col);
                    cd.removeElementAt(col);

                    colModel.fireTableStructureChanged();
                    colModel.fireTableDataChanged();
                    s.propertyDidChange();
                }
            };
        removeCol.putValue(Action.NAME, "Remove selected");
        removeCol.putValue(Action.SHORT_DESCRIPTION,
                           "Remove column selected in list");
        removeCol.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
        final JButton removeColBut = new JButton(removeCol);

        colTable.addKeyListener(new KeyAdapter() {
                public void keyReleased(final KeyEvent e) {
                    final int kc = e.getKeyCode();

                    if (kc == KeyEvent.VK_DELETE ||
                        kc == KeyEvent.VK_BACK_SPACE) {

                        removeColBut.doClick();
                    } // end of if
                }
            });

        final UnaryFunction<ImmutablePair<Vector<String>,Vector<ColumnType>>,Void> updateCols = new UnaryFunction<ImmutablePair<Vector<String>,Vector<ColumnType>>,Void>() {
            public Void apply(final ImmutablePair<Vector<String>,Vector<ColumnType>> d) {
                final PropertyEditSession s = colModel.willChange();

                colNames.clear();
                colNames.addAll(d.left);

                colData.clear();
                colData.add(d.right);

                colModel.fireTableStructureChanged();
                colModel.fireTableDataChanged();
                s.propertyDidChange();

                return null;
            }
        };

        final AbstractAction editCols = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    editColumns(frm, colNames,
                                colData.elementAt(0), updateCols);
                }
            };
        editCols.putValue(Action.NAME, "Edit as CSV");
        editCols.putValue(Action.SHORT_DESCRIPTION,
                          "Edit column mappings as CSV");
        editCols.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
        final JButton editColsBut = new JButton(editCols);

        final Vector<Vector<Object>> resData = new Vector<Vector<Object>>();
        final Vector<Map.Entry<String,ColumnType>> resCols = 
            new Vector<Map.Entry<String,ColumnType>>();
        final NotEditableTableModel resModel = 
            new NotEditableTableModel(resData, resCols);
        final JTable resTable = withColRenderers(new JTable(resModel));
        final JLabel extractLabel1 = new JLabel("Fetch", SwingConstants.RIGHT);
        final JLabel extractLabel2 =
            new JLabel("result rows executing query and", SwingConstants.LEFT);
        extractLabel1.setForeground(Color.DARK_GRAY);
        extractLabel2.setForeground(Color.DARK_GRAY);
        final SpinnerNumberModel xlm =
            new SpinnerNumberModel(100, 1, Integer.MAX_VALUE, 1);
        final JSpinner extractLimField = new JSpinner(xlm);
        final JSpinner.NumberEditor xlf =
            new JSpinner.NumberEditor(extractLimField);
        final AbstractAction extract = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    model.setProcessing(true);

                    final ImageIcon waitIco = setWaitIcon(extractLabel1);
                    final Number n = xlm.getNumber();
                    final Vector<ColumnType> cd = colData.elementAt(0);
                    final int len = cd.size();
                    final LinkedHashMap<String,ColumnType> map =
                        new LinkedHashMap<String,ColumnType>(len);

                    for (int c = 0; c < len; c++) { // zip col data
                        map.put(colNames.elementAt(c), cd.elementAt(c));
                    } // end of for

                    final ExtractFunction f = new ExtractFunction(map);
                    Connection con = null;
                    Statement stmt = null;
                    ResultSet rs = null;

                    try {
                        con = getConnection();
                        stmt = con.createStatement();

                        stmt.setMaxRows(n.intValue());
                        rs = stmt.executeQuery(sqlArea.getText());

                        final PropertyEditSession s = resModel.willChange();

                        resCols.clear();
                        resData.clear();

                        if (rs.next()) {
                            final TableData<Object> td = tableData(rs, f);

                            for (final Map.Entry<String,ColumnType> c : map.entrySet()) {

                                resCols.add(new TableHeaderEntry(c));
                            } // end of for

                            for (final ArrayList<Object> r : td.rows) {
                                resData.add(new Vector<Object>(r));
                            } // end of for
                        } // end of if
                        
                        resModel.fireTableStructureChanged();
                        resModel.fireTableDataChanged();
                        s.propertyDidChange();
                    } catch (Exception ex) {
                        JOptionPane.
                            showMessageDialog(frm, ex.getMessage(),
                                              "Query error",
                                              JOptionPane.ERROR_MESSAGE);

                    } finally {
                        if (rs != null) {
                            try { rs.close(); } catch (Exception ex) {}
                        } // end of if

                        if (stmt != null) {
                            try { stmt.close(); } catch (Exception ex) {}
                        } // end of if

                        if (con != null) {
                            try { con.close(); } catch (Exception ex) {}
                        } // end of if

                        model.setProcessing(false);
                        waitIco.setImageObserver(null);
                        extractLabel1.setIcon(null);
                    } // end of finally
                }
            };
        extract.putValue(Action.NAME, "Extract");
        extract.putValue(Action.SHORT_DESCRIPTION,
                         "Extract result using column mappings");
        extract.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
        final JButton extractBut = new JButton(extract);

        // Mapped result UI
        final JLabel resLabel = new JLabel("<html><b>Mapped result</b></html>");

        final JScrollPane resPanel =
            new JScrollPane(resTable,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final JLabel resCountLabel = new JLabel();
        resCountLabel.setForeground(Color.DARK_GRAY);
        final JComboBox convertFormats = new JComboBox(Formatting.values());
        final AbstractAction convert = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    final Charset charset = model.getCharset();
                    final Formatting fmt = (Formatting) convertFormats.
                        getSelectedItem();

                    model.setProcessing(true);

                    final Callable<Void> end = new Callable<Void>() {
                        public Void call() {
                            model.setProcessing(false);
                            return null;
                        }
                    };

                    try {
                        displayRows(frm, resCols, resData, charset, fmt, end);
                    } catch (Exception ex) {
                        try { end.call(); } catch (Exception x) { }

                        throw new RuntimeException("Fails to display rows", ex);
                    } // end of catch
                }
            };
        convert.putValue(Action.NAME, "Convert");
        convert.putValue(Action.SHORT_DESCRIPTION,
                         "Convert extracted data to some Acolyte syntax");
        convert.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        final JButton convertBut = new JButton(convert);

        colName.addKeyListener(new KeyAdapter() {
                public void keyReleased(final KeyEvent e) {
                    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_X) {
                        extractBut.doClick();
                    } else if (e.isControlDown() &&
                               e.getKeyCode() == KeyEvent.VK_C) {

                        convertBut.doClick();
                    } else if (e.isControlDown() &&
                               e.getKeyCode() == KeyEvent.VK_E) {

                        editColsBut.doClick();
                    }
                }
            });

        // Lays out UI components
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
            addGroup(layout.createParallelGroup(Alignment.BASELINE).
                     addComponent(charsetLabel).
                     addComponent(charsets)).
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
                         (int) (screenSize.getHeight()/13.75f),
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addGroup(layout.createParallelGroup(Alignment.TRAILING).
                     addComponent(testBut,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(testSqlLabel,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
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
                         colTableHeight,
                         GroupLayout.DEFAULT_SIZE,
                         colTableHeight).
            addGroup(layout.
                     createParallelGroup(Alignment.BASELINE).
                     addComponent(removeColBut).
                     addComponent(editColsBut)).
            addGroup(layout.
                     createParallelGroup(Alignment.BASELINE).
                     addComponent(extractLabel1).
                     addComponent(extractLimField).
                     addComponent(extractLabel2).
                     addComponent(extractBut)).
            addComponent(secondSep,
                         GroupLayout.PREFERRED_SIZE,
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addGroup(layout.createParallelGroup(Alignment.BASELINE).
                     addComponent(resLabel).
                     addComponent(resCountLabel)).
            addComponent(resPanel,
                         (int) screenSize.getHeight()/8,
                         GroupLayout.DEFAULT_SIZE,
                         Short.MAX_VALUE).
            addGroup(layout.createParallelGroup(Alignment.BASELINE).
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
                     addComponent(invalidCred, 0,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE).
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
            addGroup(layout.createSequentialGroup().
                     addComponent(charsetLabel).
                     addComponent(charsets)).
            addComponent(sqlLabel).
            addComponent(sqlPanel).
            addGroup(layout.createSequentialGroup().
                     addComponent(testBut,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(testSqlLabel,
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
                     addComponent(removeColBut,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(editColsBut,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
            addGroup(layout.createSequentialGroup().
                     addComponent(extractLabel1,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE).
                     addComponent(extractLimField,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(extractLabel2,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(extractBut,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
            addComponent(secondSep).
            addGroup(layout.createSequentialGroup().
                     addComponent(resLabel,
                                  GroupLayout.PREFERRED_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addComponent(resCountLabel, 0,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE).
                     addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                     GroupLayout.DEFAULT_SIZE,
                                     Short.MAX_VALUE)).
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
                        driverLabel, urlLabel, userLabel, charsetLabel);

        layout.linkSize(SwingConstants.HORIZONTAL, userField, charsets);

        layout.linkSize(SwingConstants.VERTICAL,
                        invalidCred, checkConBut, checkConLabel);

        layout.linkSize(SwingConstants.VERTICAL, testBut, testSqlLabel);

        final Image dockIcon = toolkit.
            getImage(this.getClass().getResource("dockico.png"));

        try { // Sets L&F to system one
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(frm);
        } catch (Exception e) {
            e.printStackTrace();
        } // end of catch

        frm.pack();
        frm.setLocationRelativeTo(null);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setVisible(true);

        // Application icons
        frm.setIconImage(dockIcon);
        Native.setDockIcon(dockIcon);

        // Initial focus
        frm.requestFocus();

        if (driverPath == null) {
            driverBut.grabFocus();
        } else if (jdbcUrl == null) {
            urlField.grabFocus();
        } else if (dbUser == null) {
            userField.grabFocus();
        } else {
            passField.grabFocus();
        } // end of else

        // Sets up model bindings
        final BindingOptionMap txtOpts = new BindingOptionMap().
            add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE);

        Binder.bind("text", userField, "user", this.model, txtOpts);
        Binder.bind("text", passField, "password", this.model, txtOpts);
        Binder.bind("text", urlField, "url", this.model, txtOpts);
        Binder.bind("selectedItem", charsets, "charset", this.model, null);

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

	Binder.bind("text", colName, "enabled", addCol, txtLenOpts);

        Binder.bind("selectionModel.selectionEmpty", colTable,
                    "enabled", removeCol,
                    new BindingOptionMap().
                    add(BindingKey.INPUT_TRANSFORMER,
                        NegateBooleanTransformer.getInstance()));

	Binder.bind("connectionConfig", this.model, "visible", invalidUrl,
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
	Binder.bind("connectionConfig", this.model, "enabled[]", checkCon,
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

        // Bindings for SQL test action
	Binder.bind("text", sqlArea, "enabled[]", testSql, txtLenOpts);

        // Bindings for extract action
        final BindingOptionMap intBoolOpts = new BindingOptionMap().
            add(BindingKey.INPUT_TRANSFORMER,
                IntegerToBooleanTransformer.getInstance());

        Binder.bind("text", sqlArea, "enabled[]", extract, txtLenOpts);
        Binder.bind("columnCount", colModel, "enabled[]", extract, intBoolOpts);

        // Bindings for convert action
        Binder.bind("rowCount", resModel, "text", resCountLabel,
                    new BindingOptionMap().
                    add(BindingKey.INPUT_TRANSFORMER,
                        NumberToStringTransformer.getInstance(new MessageFormat("{0,choice,0#no row|1#1 row|1<{0,number,integer} rows}"))));

        Binder.bind("rowCount", resModel, "enabled[]", convert, intBoolOpts);
    } // end of setUp

    /**
     * Returns preference file: <tt>$USERHOME/.acolyte/studio.properties</tt>
     */
    static File preferencesFile() {
        final File userDir = new File(System.getProperty("user.home"));
        final File prefDir = (userDir.canWrite())
            ? new File(userDir, ".acolyte") : null;

        if (prefDir != null && !prefDir.exists()) {
            prefDir.mkdir();
        } // end of if

        return (prefDir != null && prefDir.canRead() && prefDir.canWrite())
            ? new File(prefDir, "studio.properties") : null;

    } // end of preferencesFile

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
     * Listens to key to close a |window|.
     */
    private static final KeyAdapter closeKeyStrokes(final java.awt.Window window) {
        return new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                final int kc = e.getKeyCode();
                if (kc == KeyEvent.VK_ESCAPE ||
                    kc == KeyEvent.VK_ENTER) {
                    window.dispose();
                }
            }
        };
    } // end of closeKeyStrokes

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

        // DB charset
        final Charset charset = this.model.getCharset();

        if (charset == null) {
            this.conf.remove("db.charset");
        } else {
            this.conf.put("db.charset", charset.name());
        } // end of else

        // Persist configuration
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
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();

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

            final JDialog dlg = new JDialog(frm, "Test result");
            final int c = rs.getMetaData().getColumnCount();
            final RowFunction<String> f = new RowFunction<String>() {
                public ArrayList<String> apply(final ResultSet rs) {
                    try {
                        final ArrayList<String> row = new ArrayList<String>();
                        for (int p = 1; p <= c; p++) {
                            row.add(rs.getString(p));
                        }
                        return row;
                    } catch (SQLException e) {
                        throw new RuntimeException("Fails to extract row", e);
                    } // end of catch
                }
            };
            final TableData<String> td = tableData(rs, f);
            final int rc = td.rows.size();
            final AbstractTableModel tm = new AbstractTableModel() {
                    public int getColumnCount() { return c; }
                    public int getRowCount() { return rc; }
                    public String getColumnName(int i) {
                        return td.columns.get(i);
                    }
                    public Class<String> getColumnClass(int i) {
                        return String.class;
                    }
                    public String getValueAt(int x, int y) {
                        return td.rows.get(x).get(y);
                    }
                    public boolean isCellEditable(int x, int y) {
                        return false;
                    }
                };
            final JTable table = new JTable(tm);
            final JScrollPane pane = new JScrollPane(table);

            table.setDefaultRenderer(String.class, new ResultCellRenderer());

            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setContentPane(pane);
            dlg.setMinimumSize(new Dimension(frm.getWidth(), frm.getHeight()/4));
            table.setPreferredScrollableViewportSize(dlg.getMinimumSize());
            dlg.pack();
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);

            final Point loc = dlg.getLocation();
            loc.translate(20, 0);
            dlg.setLocation(loc);

            final KeyAdapter kl = closeKeyStrokes(dlg);
            dlg.addKeyListener(kl);
            pane.addKeyListener(kl);

            pane.grabFocus();
        } catch (Exception ex) {
            JOptionPane.
                showMessageDialog(frm, ex.getMessage(),
                                  "Query error", JOptionPane.ERROR_MESSAGE);

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
        } // end of finally
    } // end of testSql

    /**
     * Returns JDBC connection for configuration in model.
     */
    private Connection getConnection() throws SQLException {
        return JDBC.connect(model.getDriver(), model.getUrl(),
                            model.getUser(), model.getPassword());

    } // end of getConnection

    /**
     * Returns table data from |result| set.
     */
    private <A> TableData<A> tableData(final ResultSet rs,
                                       final RowFunction<A> f)
        throws SQLException {

        final ResultSetMetaData meta = rs.getMetaData();
        final int c = meta.getColumnCount();
        final TableData<A> td = new TableData<A>();

        for (int p = 1; p <= c; p++) {
            final String n = meta.getColumnLabel(p);
            td.columns.add((n == null || "".equals(n)) ? "col" + p : n);
        } // end of for

        // First row
        td.rows.add(f.apply(rs));

        // Other row(s)
        while (rs.next()) {
            td.rows.add(f.apply(rs));
        } // end of if

        return td;
    } // end of tableData

    /**
     * Sets wait icon on |label|.
     */
    private static ImageIcon setWaitIcon(final JLabel label) {
        final ImageIcon ico =
            new ImageIcon(Studio.class.getResource("loader.gif"));

        label.setIcon(ico);
        ico.setImageObserver(label);

        return ico;
    } // end of setWaitIcon

    /**
     * Returns table with column renderers.
     */
    private JTable withColRenderers(final JTable table) {
        // Data renderers
        final DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();

        // BigDecimal
        table.setDefaultRenderer(BigDecimal.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

                final BigDecimal bd = (BigDecimal) value;

                return dtcr.getTableCellRendererComponent(table, bd.toString(), isSelected, hasFocus, row, column);
            }
        });

        // Boolean
        table.setDefaultRenderer(Boolean.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

                final Boolean b = (Boolean) value;

                return dtcr.getTableCellRendererComponent(table, Boolean.toString(b), isSelected, hasFocus, row, column);
            }
        });

        // Date
        table.setDefaultRenderer(java.sql.Date.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

                final java.sql.Date d = (java.sql.Date) value;

                return dtcr.getTableCellRendererComponent(table, String.format("%tF", d), isSelected, hasFocus, row, column);
            }
        });

        // Time
        table.setDefaultRenderer(Time.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

                final Time t = (Time) value;

                return dtcr.getTableCellRendererComponent(table, String.format("%tr", t), isSelected, hasFocus, row, column);
            }
        });

        // Timestamp
        table.setDefaultRenderer(Timestamp.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

                final Time t = (Time) value;

                return dtcr.getTableCellRendererComponent(table, String.format("%tr", t), isSelected, hasFocus, row, column);
            }
        });

        return table;
    } // end of withColRenderers

    /**
     * Chooses a JDBC driver.
     */
    public void chooseDriver(final JFrame frm, final JTextField field) {
        final JFileChooser chooser = new JFileChooser(new File("."));

        for (final FileFilter ff : chooser.getChoosableFileFilters()) {
            chooser.removeChoosableFileFilter(ff); // clean choosable filters
        } // end of for

        chooser.setFileHidingEnabled(false);
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

        field.setForeground(Color.BLACK);
        field.setText(driverPath);

        try {
            model.setDriver(JDBC.loadDriver(driverFile));

            // !! Side-effect
            conf.put("jdbc.driverPath", driverPath);
            updateConfig();
        } catch (Exception e) {
            e.printStackTrace();
        } // end of catch
    } // end of chooseDriver

    /**
     * Creates export dialog, and returns associated document.
     */
    private static <T> void createConvertDialog(final JFrame frm, final Formatting fmt, final UnaryFunction<Document,Callable<T>> init, final Callable<Void> end) {

        // Prepares UI components
        final JLabel dialogLabel =
            new JLabel("<html><b>Formatted data</b></html>");
        dialogLabel.setHorizontalTextPosition(SwingConstants.LEFT);

        final JEditorPane rowArea = new JEditorPane();
        rowArea.setEditable(false);
        dialogLabel.setLabelFor(rowArea);
        final JScrollPane rowPanel =
            new JScrollPane(rowArea,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rowPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        rowArea.setContentType(fmt.mimeType);

        final JDialog dlg = new JDialog(frm, "Convert");
        final Container content = dlg.getContentPane();
        final GroupLayout layout = new GroupLayout(content);

        final Clipboard pb = Toolkit.getDefaultToolkit().getSystemClipboard();
        final AbstractAction pbcopy = new AbstractAction() {
                public void actionPerformed(final ActionEvent evt) {
                    final StringSelection codeSel =
                        new StringSelection(rowArea.getText());

                    pb.setContents(codeSel, null);
                }
            };
        pbcopy.putValue(Action.NAME, "Copy");
        pbcopy.putValue(Action.SHORT_DESCRIPTION, "Copy to paste board");
        final JButton copyBut = new JButton(pbcopy);

        // Lays out UI component
        content.setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

        final GroupLayout.SequentialGroup vgroup =
            layout.createSequentialGroup().
            addComponent(dialogLabel).
            addComponent(rowPanel,
                         GroupLayout.PREFERRED_SIZE,
                         GroupLayout.DEFAULT_SIZE,
                         Short.MAX_VALUE).
            addComponent(copyBut);

        final GroupLayout.ParallelGroup hgroup =
            layout.createParallelGroup(Alignment.LEADING).
            addComponent(dialogLabel).
            addComponent(rowPanel).
            addGroup(layout.createSequentialGroup().
                     addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                     GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).
                     addComponent(copyBut));

        layout.setVerticalGroup(vgroup);
        layout.setHorizontalGroup(hgroup);

        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setMinimumSize(new Dimension(frm.getWidth(), frm.getHeight()/3));

        dlg.pack();
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);

        final KeyAdapter kl = closeKeyStrokes(dlg);
        dlg.addKeyListener(kl);
        content.addKeyListener(kl);
        rowArea.addKeyListener(kl);

        rowArea.grabFocus();

        final ImageIcon waitIco = setWaitIcon(dialogLabel);

        try {
            final Callable<T> f = init.apply(rowArea.getDocument());

            final SwingWorker<Void,Void> w = new SwingWorker<Void,Void>() {
                public Void doInBackground() throws Exception {
                    try {
                        f.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    } finally {
                        try { end.call(); } catch (Exception e) { }
                    }

                    return null;
                }
            };

            w.execute();
        } finally {
            waitIco.setImageObserver(null);
            dialogLabel.setIcon(null);
        } // end of finally
    } // end of createConvertDialog

    /**
     * Displays formatted rows.
     */
    private static void displayRows(final JFrame frm, final Vector<Map.Entry<String,ColumnType>> cols, final Vector<Vector<Object>> data, final Charset charset, final Formatting fmt, final Callable<Void> end) {

        final UnaryFunction<Document,Callable<Void>> f =
            new UnaryFunction<Document,Callable<Void>>() {
            public Callable<Void> apply(final Document doc) {
                final DocumentAppender ap = new DocumentAppender(doc);

                // Column definitions
                final ArrayList<String> cnames = new ArrayList<String>();
                final ArrayList<ColumnType> ctypes =
                new ArrayList<ColumnType>();
                final int c = cols.size();

                ap.append(fmt.imports);
                ap.append("\r\n\r\nRowLists.rowList" + c + "(");

                int i = 0;
                for (final Map.Entry<String,ColumnType> e : cols) {
                    final String name = e.getKey();
                    final ColumnType type = e.getValue();

                    cnames.add(name);
                    ctypes.add(type);

                    if (i++ > 0) {
                        ap.append(", ");
                    } // end of if

                    final String tname = (fmt.typeMap.containsKey(type))
                        ? fmt.typeMap.get(type) : "String";

                    ap.append(String.format(fmt.colDef, tname, name));
                } // end of while

                ap.append(")\r\n");

                // --

                return new Callable<Void>() {
                    public Void call() {
                        RowFormatter.
                            appendRows(new VectorIterator(data), ap, charset,
                                       fmt, ctypes);

                        return null;
                    }
                };
            } // end of apply
        }; // end of f

        createConvertDialog(frm, fmt, f, end);
    } // end of displayRows

    /**
     * Edit column mappings.
     */
    private static void editColumns(final JFrame frm, final Vector<String> colNames, final Vector<ColumnType> colTypes, final UnaryFunction<ImmutablePair<Vector<String>,Vector<ColumnType>>,Void> cf) {

        // Prepares UI components
        final JLabel dialogLabel =
            new JLabel("<html><b>Column mappings as CSV</b></html>");
        dialogLabel.setHorizontalTextPosition(SwingConstants.LEFT);

        final JLabel useLabel = new JLabel("<html>Simplified CSV syntax, with no quoting required.<br />For each line: <tt>type;name</tt> (e.g. <tt>string;Column name</tt>)</html>");
        useLabel.setHorizontalTextPosition(SwingConstants.LEFT);

        final JEditorPane csvArea = new JEditorPane();
        csvArea.setEditable(false);
        dialogLabel.setLabelFor(csvArea);
        final JScrollPane csvPanel =
            new JScrollPane(csvArea,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        csvPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        csvArea.setContentType("text/csv");

        final Document doc = csvArea.getDocument();
        final JDialog dlg = new JDialog(frm, "Edit columns");
        final Container content = dlg.getContentPane();
        final GroupLayout layout = new GroupLayout(content);

        final AbstractAction update = new AbstractAction() {
                public void actionPerformed(final ActionEvent evt) {
                    BufferedReader r = null;
                    final Vector<String> ns = new Vector<String>();
                    final Vector<ColumnType> ts = new Vector<ColumnType>();

                    try {
                        final StringReader sr =
                            new StringReader(csvArea.getText());

                        r = new BufferedReader(sr);

                        String line, n;
                        ColumnType t;
                        for (int i = 1, o = 0, l = 0, x = -1;
                             (line = r.readLine()) != null; i++) {

                            l = line.length();

                            if ((x = line.indexOf(";")) == -1) {
                                JOptionPane.showMessageDialog(dlg, "Invalid CSV line #" + i, "Invalid line", JOptionPane.ERROR_MESSAGE);

                                csvArea.select(o, o+l);
                                return;
                            } // end of if

                            // ---

                            t = ColumnType.typeFor(line.substring(0, x));

                            if (t == null) {
                                JOptionPane.showMessageDialog(dlg, "Invalid column type at line #" + i + ": " + line.substring(0, x), "Invalid type", JOptionPane.ERROR_MESSAGE);

                                csvArea.select(o, o+x);
                                return;
                            } // end of if

                            n = line.substring(x+1);

                            if (n.length() == 0) {
                                JOptionPane.showMessageDialog(dlg, "Invalid column name at line #" + i, "Invalid name", JOptionPane.ERROR_MESSAGE);

                                csvArea.setCaretPosition(o+x+1);
                                return;
                            } // end of if

                            ns.add(n);
                            ts.add(t);

                            o += l + 1;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Fails to update", e);
                    } finally {
                        if (r != null) {
                            try {
                                r.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } // end of catch
                        } // end of if
                    } // end of finally

                    cf.apply(ImmutablePair.of(ns, ts));
                    dlg.dispose();
                }
            };
        update.putValue(Action.NAME, "Update");
        update.putValue(Action.SHORT_DESCRIPTION, "Update column mappings");
        update.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
        update.setEnabled(false);
        final JButton updateBut = new JButton(update);

        Binder.bind("text", csvArea, "enabled", update,
                    new BindingOptionMap().
                    add(BindingKey.INPUT_TRANSFORMER,
                        StringLengthToBooleanTransformer.
                        getTrimmingInstance()).
                    add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE));

        // Lays out UI component
        content.setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

        final GroupLayout.SequentialGroup vgroup =
            layout.createSequentialGroup().
            addComponent(dialogLabel).
            addComponent(useLabel).
            addComponent(csvPanel,
                         GroupLayout.PREFERRED_SIZE,
                         GroupLayout.DEFAULT_SIZE,
                         Short.MAX_VALUE).
            addComponent(updateBut);

        final GroupLayout.ParallelGroup hgroup =
            layout.createParallelGroup(Alignment.LEADING).
            addComponent(dialogLabel).
            addComponent(useLabel).
            addComponent(csvPanel).
            addGroup(layout.createSequentialGroup().
                     addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                     GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).
                     addComponent(updateBut));

        layout.setVerticalGroup(vgroup);
        layout.setHorizontalGroup(hgroup);

        dlg.setModal(true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setMinimumSize(new Dimension(frm.getWidth(), frm.getHeight()/3));

        final KeyAdapter kl = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dlg.dispose();
            }
        };

        dlg.addKeyListener(kl);
        content.addKeyListener(kl);
        csvArea.addKeyListener(kl);

        final KeyAdapter editKeys = new KeyAdapter() {
                public void keyReleased(final KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_U && e.isControlDown())
                        updateBut.doClick();
                }
            };

        content.addKeyListener(editKeys);
        csvArea.addKeyListener(editKeys);

        csvArea.grabFocus();

        // Load as CSV
        final int len = colNames.size();

        StringBuffer line = new StringBuffer();
        for (int i = 0, o = 0; i < len; i++) {
            line.setLength(0);
            line.append(colTypes.elementAt(i)).
                append(';').append(colNames.elementAt(i)).
                append("\n");

            try {
                doc.insertString(o, line.toString(), null);
            } catch (Exception e) {
                throw new RuntimeException("Fails to append CSV line", e);
            }

            o += line.length();
        }

        csvArea.setEditable(true);

        dlg.pack();
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
    } // end of editColumns

    // ---

    /**
     * Starts studio application.
     */
    public static void main(final String[] args) throws Exception {
        final Studio studio = new Studio();

        studio.setUp();
    } // end of main

    // --- Inner classes ---

    /**
     * Returns list of formatted strings from a result set.
     */
    private interface RowFunction<A>
        extends UnaryFunction<ResultSet,ArrayList<A>> { }

    /**
     * Table data.
     */
    private final class TableData<A> {
        final ArrayList<String> columns;
        final ArrayList<ArrayList<A>> rows;

        TableData() {
            this.columns = new ArrayList<String>();
            this.rows = new ArrayList<ArrayList<A>>();
        } // end of <init>
    } // end of class TableData

    /**
     * Extract mapped column from row.
     */
    private final class ExtractFunction implements RowFunction<Object> {
        private final HashMap<String,ColumnType> mapping;

        /**
         * Un-curried function.
         */
        public ExtractFunction(final HashMap<String,ColumnType> map) {
            this.mapping = map;
        } // end of <init>

        /**
         * {@inheritDoc}
         */
        public ArrayList<Object> apply(final ResultSet rs) {
            final ArrayList<Object> list = new ArrayList<Object>();

            try {
                for (final String name : this.mapping.keySet()) {
                    list.add(JDBC.getObject(rs, name, this.mapping.get(name)));
                } // end of for
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } // end of catch

            return list;
        } // end of apply
    } // end of class ExtractFunction

    /**
     * Document appender.
     */
    private static final class DocumentAppender
        implements RowFormatter.Appender {

        private final Document doc;

        // --- Constructors ---

        /**
         * Bulk constructor.
         */
        DocumentAppender(final Document doc) {
            this.doc = doc;
        } // end of <init>

        // ---

        /**
         * {@inheritDoc}
         */
        public void append(String text) {
            try {
                synchronized(this.doc) {
                    this.doc.insertString(this.doc.getLength(), text, null);
                } // end of synchronized
            } catch (Exception e) {
                throw new RuntimeException("Fails to append to doc", e);
            } // end of catch
        } // end of append
    } // end of class DocumentAppender

    /**
     * Iterator if row from vector.
     */
    private static final class VectorIterator
        implements java.util.Iterator<RowFormatter.ResultRow> {

        private Iterator<Vector<Object>> it;

        /**
         * Bulk constructor.
         */
        VectorIterator(final Vector<Vector<Object>> vector) {
            this.it = vector.iterator();
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
            return this.it.hasNext();
        } // end of hasNext

        /**
         * {@inheritDoc}
         */
        public RowFormatter.ResultRow next() {
            return new VectorRow(this.it.next());
        } // end of next
    } // end of class VectorIterator

    /**
     * Row based on vector.
     */
    private static final class VectorRow implements RowFormatter.ResultRow {
        private Vector<Object> v;

        /**
         * Bulk constructor.
         */
        VectorRow(final Vector<Object> vector) {
            this.v = vector;
        } // end of <init>

        // ---

        public String getString(int p) { return (String) v.elementAt(p); }
        public boolean getBoolean(int p) { return (Boolean) v.elementAt(p); }
        public byte getByte(int p) { return (Byte) v.elementAt(p); }
        public short getShort(int p) { return (Short) v.elementAt(p); }
        public java.sql.Date getDate(int p) {
            return (java.sql.Date) v.elementAt(p);
        }
        public double getDouble(int p) { return (Double) v.elementAt(p); }
        public float getFloat(int p) { return (Float) v.elementAt(p); }
        public int getInt(int p) { return (Integer) v.elementAt(p); }
        public long getLong(int p) { return (Long) v.elementAt(p); }
        public Time getTime(int p) { return (Time) v.elementAt(p); }
        public Timestamp getTimestamp(int p) {
            return (Timestamp) v.elementAt(p);
        }
        public BigDecimal getBigDecimal(int p) {
            return (BigDecimal) v.elementAt(p);
        }
        public boolean isNull(int p) { return v.elementAt(p) == null; }
    } // end of class VectorRow

    /**
     * Map entry as table column.
     */
    private final class TableHeaderEntry 
        implements Map.Entry<String,ColumnType> {

        private final Map.Entry<String,ColumnType> wrappee;

        /**
         * Value constructor
         */
        TableHeaderEntry(final Map.Entry<String,ColumnType> v) {
            this.wrappee = v;
        } // end of <init>

        // ---

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof TableHeaderEntry)) {
                return false;
            } // end of if

            final TableHeaderEntry other = (TableHeaderEntry) o;

            return this.wrappee.equals(other.wrappee);
        } // end of equals

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return this.wrappee.hashCode();
        } // end of hashCode

        /**
         * Returns name as string representation.
         */
        public String toString() {
            return this.wrappee.getKey();
        } // end of toString

        /**
         * Throws exception.
         */
        public ColumnType setValue(ColumnType v) {
            throw new UnsupportedOperationException();
        } // end of setValue

        /**
         * {@inheritDoc}
         */
        public String getKey() { 
            return this.wrappee.getKey();
        } // end of getKey

        /**
         * {@inheritDoc}
         */
        public ColumnType getValue() {
            return this.wrappee.getValue();
        } // end of getValue
    } // end of class TableHeaderEntry
} // end of class Studio
