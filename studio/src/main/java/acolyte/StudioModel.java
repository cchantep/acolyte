package acolyte;

import java.beans.PropertyChangeListener;

import java.sql.Driver;

import java.nio.charset.Charset;

import melasse.PropertyChangeSupport;

/**
 * Property change support.
 *
 * @author Cedric Chantepie
 */
public final class StudioModel {
    // --- Properties ---

    /**
     * Change support
     */
    private final PropertyChangeSupport pcs;

    /**
     * Connection validated
     * [false]
     */
    private boolean connectionValidated = false;

    /**
     * Is processing?
     * [false]
     */
    private boolean processing = false;

    /**
     * JDBC driver
     */
    private Driver driver = null;

    /**
     * JDBC url
     */
    private String url = null;

    /**
     * DB user name
     */
    private String user = null;

    /**
     * Password for DB user
     */
    private String password = null;

    /**
     * DB character set
     */
    private Charset charset = null;

    /**
     * Connection config time
     */
    private long connectionConfig = -1l;

    // --- Constructors ---

    /**
     * No-arg constructor.
     */
    public StudioModel() {
        this.pcs = new PropertyChangeSupport<StudioModel>(this);

        pcs.registerDependency("driver", new String[] { "connectionConfig" });
        pcs.registerDependency("url", new String[] { "connectionConfig" });
        pcs.registerDependency("user", new String[] { "connectionConfig" });
        pcs.registerDependency("charset", new String[] { "connectionConfig" });

        pcs.registerDependency("connectionConfig", 
                               new String[] { "connectionValidated" });

    } // end of <init>

    // --- Properties accessors ---

    /**
     * Returns time when configuration of connection was updated.
     */
    public long getConnectionConfig() {
        return this.connectionConfig;
    } // end of getConnectionConfig

    /**
     * Sets DB |charset|.
     * 
     * @param charset DB charset
     * @see #getCharset
     */
    public void setCharset(final Charset charset) {
        final Charset old = this.charset;

        this.charset = charset;
        this.connectionConfig = System.currentTimeMillis();
        this.connectionValidated = false;

        this.pcs.firePropertyChange("charset", old, this.charset);
    } // end of setCharset

    /**
     * Returns DB charset.
     *
     * @see #setCharset
     */
    public Charset getCharset() {
        return this.charset;
    } // end of getCharset

    /**
     * Sets name of DB |user|.
     *
     * @param user User name
     * @see #getUser
     */
    public void setUser(final String user) {
        final String old = this.user;

        this.user = user;
        this.connectionConfig = System.currentTimeMillis();
        this.connectionValidated = false;

        this.pcs.firePropertyChange("user", old, this.user);
    } // end of setUser

    /**
     * Returns name of DB user.
     * @see #setUser
     */
    public String getUser() {
        return this.user;
    } // end of getUser

    /**
     * Sets |password| for DB user.
     *
     * @param password DB user password
     * @see #getPassword
     */
    public void setPassword(final String password) {
        this.password = password;
    } // end of setPassword

    /**
     * Returns password of DB user.
     * @see #setPassword
     */
    public String getPassword() {
        return this.password;
    } // end of getPassword

    /**
     * Sets JDBC |url|.
     *
     * @param url JDBC URL
     * @see #getUrl
     */
    public void setUrl(final String url) {
        final String old = this.url;

        this.url = url;
        this.connectionConfig = System.currentTimeMillis();
        this.connectionValidated = false;

        this.pcs.firePropertyChange("url", old, this.url);
    } // end of setUrl

    /**
     * Returns JDBC URL.
     * @see #setUrl
     */
    public String getUrl() {
        return this.url;
    } // end of getUrl

    /**
     * Sets JDBC |driver|.
     *
     * @param driver JDBC driver
     * @see #getDriver
     */
    public void setDriver(final Driver driver) {
        final Driver old = this.driver;

        this.driver = driver;
        this.connectionConfig = System.currentTimeMillis();
        this.connectionValidated = false;
        
        this.pcs.firePropertyChange("driver", old, this.driver);
    } // end of setDriver

    /**
     * Returns JDBC driver.
     * @see #setDriver
     */
    public Driver getDriver() {
        return this.driver;
    } // end of getDriver

    /**
     * Sets whether connection is |validated|.
     *
     * @param validated Connection validated?
     * @see #isConnectionValidated
     */
    public void setConnectionValidated(final boolean validated) {
        final boolean old = this.connectionValidated;
        this.connectionValidated = validated;

        this.pcs.firePropertyChange("connectionValidated", 
                                    old, this.connectionValidated);

    } // end of setConnectionValidated

    /**
     * Returns whether connection is validated.
     * @see #setConnectionValidated
     */
    public boolean isConnectionValidated() {
        return this.connectionValidated;
    } // end of isConnectionValidated

    /**
     * Sets whether is |processing|.
     *
     * @param processing Is processing?
     * @see #isProcessing
     */
    public void setProcessing(final boolean processing) {
        final boolean old = this.processing;
        this.processing = processing;

        this.pcs.firePropertyChange("processing", old, this.processing);
    } // end of setProcessing
        
    /**
     * Returns whether processing?
     * @see #setProcessing
     */
    public boolean isProcessing() {
        return this.processing;
    } // end of isProcessing

    // ---

    /**
     * Adds property change |listener|.
     *
     * @param listener New property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	this.pcs.addPropertyChangeListener(listener);
    } // end of addPropertyChangeListener

    /**
     * Removes property change |listener|.
     *
     * @param listener Listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	this.pcs.removePropertyChangeListener(listener);
    } // end of removePropertyChangeListener
} // end of class StudioModel
