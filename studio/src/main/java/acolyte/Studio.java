package acolyte;

import java.util.Properties;
import java.util.Arrays;

import java.io.File;

import java.sql.Driver;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Color;

import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import javax.swing.JPasswordField;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.LayoutStyle;
import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.filechooser.FileFilter;

import javax.swing.table.TableColumn;

import melasse.StringLengthToBooleanTransformer;
import melasse.ValueTransformer;
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
    // --- Properties ---

    /**
     * JDBC driver
     */
    private Driver driver = null;

    /**
     * Runtime configuration
     */
    private final Properties conf = new Properties();

    // ---

    /**
     * Sets up UI.
     */
    public void setUp() {
        final JFrame frm = new JFrame("Acolyte Studio");
        final Dimension screenSize = java.awt.Toolkit.
            getDefaultToolkit().getScreenSize();

        frm.setMinimumSize(new Dimension((int) (screenSize.getWidth()/2.4d),
                                         (int) (screenSize.getHeight()/1.4d)));
        frm.setPreferredSize(frm.getMinimumSize());

        // 
        
        final Container content = frm.getContentPane();
        final GroupLayout layout = new GroupLayout(content);
        final JLabel confLabel = 
            new JLabel("<html><b>Configuration</b></html>");
        final JLabel driverLabel = new JLabel("JDBC driver");
        final JTextField driverField = new JTextField("Path to driver.jar");
        driverField.setEditable(false);
        driverField.setForeground(Color.LIGHT_GRAY);
        de.sciss.syntaxpane.DefaultSyntaxKit.initKit();
        final JLabel urlLabel = new JLabel("JDBC URL");
        final JTextField urlField = new JTextField();
        final JLabel invalidUrl = new JLabel("Driver doesn't accept URL");
        invalidUrl.setForeground(Color.RED);
        final JLabel userLabel = new JLabel("DB user");
        final JTextField userField = new JTextField();
        final JLabel passLabel = new JLabel("Password");
        final JPasswordField passField = new JPasswordField();
        final JLabel sqlLabel = new JLabel("<html><b>SQL</b></html>");
        final JEditorPane sqlArea = new JEditorPane();
        final JScrollPane sqlPanel = 
            new JScrollPane(sqlArea, 
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sqlArea.setContentType("text/sql");
        final JTextField colName = new JTextField();
        final JTable colTable = new JTable();
        final JTable resTable = new JTable();
        final JScrollPane colPanel = new JScrollPane(colTable);
        final JScrollPane resPanel = 
            new JScrollPane(resTable, 
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        final JComboBox colTypes = 
            new JComboBox(Export.colTypes.toArray());
        colTypes.setSelectedItem("string");

        final Runnable driverRun = new Runnable() {
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

                    final String driverPath = 
                        chooser.getSelectedFile().getAbsolutePath();

                    final File driverFile = new File(driverPath);

                    driverField.setForeground(colName.getForeground());
                    driverField.setText(driverPath);

                    try {
                        driver = JDBC.loadDriver(driverFile.toURL());
                        conf.put("jdbc.driverPath", driverPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        final AbstractAction chooseDriver = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    driverRun.run();
                }
            };
        chooseDriver.putValue(Action.NAME, "Choose...");
        chooseDriver.putValue(Action.SHORT_DESCRIPTION, "Choose JDBC driver");
        final JButton driverBut = new JButton(chooseDriver);
        driverField.addMouseListener(new MouseAdapter() {
                public void mouseClicked(final MouseEvent e) {
                    driverRun.run();
                }
            });

        final AbstractAction addCol = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    final String name = colName.getText();
                    final String type = (String) colTypes.getSelectedItem();

                    final TableColumn col = new TableColumn();
                    col.setHeaderValue(name);

                    colTable.addColumn(col);

                    setEnabled(false);
                    colName.setText("");
                    colName.grabFocus();
                }
            };
        addCol.putValue(Action.NAME, "Add column");
        addCol.putValue(Action.SHORT_DESCRIPTION, "Add column to list");
        final JButton colBut = new JButton(addCol);

        final AbstractAction testSql = new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    System.out.println("-> test SQL");
                }
            };
        testSql.putValue(Action.NAME, "Test");
        testSql.putValue(Action.SHORT_DESCRIPTION, "Test SQL request");
        final JButton testBut = new JButton(testSql);

        sqlPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        content.setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

        final JSeparator zeroSep = new JSeparator();
        final JSeparator firstSep = new JSeparator();
        final JSeparator secondSep = new JSeparator();
        final GroupLayout.SequentialGroup vgroup = 
            layout.createSequentialGroup().
            addComponent(confLabel,
                         GroupLayout.PREFERRED_SIZE,
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addGroup(layout.
                     createParallelGroup(GroupLayout.Alignment.BASELINE).
                     addComponent(driverLabel).
                     addComponent(driverField).
                     addComponent(driverBut)).
            addGroup(layout.
                     createParallelGroup(GroupLayout.Alignment.BASELINE).
                     addComponent(urlLabel).
                     addComponent(urlField)).
            addComponent(invalidUrl).
            addGroup(layout.
                     createParallelGroup(GroupLayout.Alignment.BASELINE).
                     addComponent(userLabel).
                     addComponent(userField).
                     addComponent(passLabel).
                     addComponent(passField)).
            addComponent(zeroSep,
                         GroupLayout.PREFERRED_SIZE, 
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addComponent(sqlLabel).
            addComponent(sqlPanel, 
                         (int) screenSize.getHeight()/8,
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
            addGroup(layout.
                     createParallelGroup(GroupLayout.Alignment.BASELINE).
                     addComponent(colName).
                     addComponent(colTypes).
                     addComponent(colBut)).
            addComponent(colPanel,
                         (int) screenSize.getHeight()/16,
                         GroupLayout.DEFAULT_SIZE,
                         (int) screenSize.getHeight()/16).
            addComponent(secondSep,
                         GroupLayout.PREFERRED_SIZE, 
                         GroupLayout.DEFAULT_SIZE,
                         GroupLayout.PREFERRED_SIZE).
            addComponent(resPanel,
                         (int) screenSize.getHeight()/6,
                         GroupLayout.DEFAULT_SIZE,
                         Short.MAX_VALUE);

        final GroupLayout.ParallelGroup hgroup = 
            layout.createParallelGroup(GroupLayout.Alignment.LEADING).
            addComponent(confLabel).
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
                     addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                     GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).
                     addComponent(testBut,
                                  GroupLayout.PREFERRED_SIZE, 
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.PREFERRED_SIZE)).
            addComponent(firstSep).
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
            addComponent(secondSep).
            addComponent(resPanel);
                     
        layout.setVerticalGroup(vgroup);
        layout.setHorizontalGroup(hgroup);

        layout.linkSize(SwingConstants.HORIZONTAL, 
                        driverLabel, urlLabel, userLabel);

        frm.pack();
        frm.setLocationRelativeTo(null);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setVisible(true);

        // Sets up bindings
        colName.addActionListener(addCol);
	Binder.bind("text", colName,
		    "enabled", addCol,
		    new BindingOptionMap().
		    add(BindingKey.INPUT_TRANSFORMER,
			StringLengthToBooleanTransformer.
                        getTrimmingInstance()).
                    add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE));

	Binder.bind("text", sqlArea,
		    "enabled[]", testSql,
		    new BindingOptionMap().
		    add(BindingKey.INPUT_TRANSFORMER,
			StringLengthToBooleanTransformer.
                        getTrimmingInstance()).
                    add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE));

	Binder.bind("text", driverField,
                    "enabled[]", testSql,
		    new BindingOptionMap().
		    add(BindingKey.INPUT_TRANSFORMER,
                        new ValueTransformer<String>() {
                            public Boolean transform(final String t) {
                                if (t == null || t == "Path to driver.jar") {
                                    return false;
                                } // end of if

                                if (t.trim().length() == 0) {
                                    return false;
                                } // end of if
                                
                                // ---

                                // Sets from file chooser - so already validated
                                return true;
                            }
                    }));

	Binder.bind("text", urlField,
                    "enabled[]", testSql,
		    new BindingOptionMap().
                    add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE).
		    add(BindingKey.INPUT_TRANSFORMER,
                        new ValueTransformer<String>() {
                            public Boolean transform(final String t) {
                                try {
                                    return (driver != null && 
                                            driver.acceptsURL(t));

                                } catch (Exception e) {
                                    return false;
                                } // end of catch
                            }
                    }));

	Binder.bind("text", userField,
		    "enabled[]", testSql,
		    new BindingOptionMap().
		    add(BindingKey.INPUT_TRANSFORMER,
			StringLengthToBooleanTransformer.
                        getTrimmingInstance()).
                    add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE));

	Binder.bind("text", passField,
		    "enabled[]", testSql,
		    new BindingOptionMap().
		    add(BindingKey.INPUT_TRANSFORMER,
			StringLengthToBooleanTransformer.
                        getTrimmingInstance()).
                    add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE));

	Binder.bind("text", urlField,
                    "visible", invalidUrl,
		    new BindingOptionMap().
                    add(TextBindingKey.CONTINUOUSLY_UPDATE_VALUE).
		    add(BindingKey.INPUT_TRANSFORMER,
                        new ValueTransformer<String>() {
                            public Boolean transform(final String t) {
                                try {
                                    return (t != null && t.length() > 0 &&
                                            driver != null && 
                                            !driver.acceptsURL(t));

                                } catch (Exception e) { }

                                return false;
                            }
                    }));

    } // end of setUp
} // end of class Studio
