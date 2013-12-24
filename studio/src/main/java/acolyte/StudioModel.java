package acolyte;

import java.beans.PropertyChangeListener;

import java.sql.Driver;

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
     * Connection ref/ID
     */
    private long connectionRef = -1l;

    // --- Constructors ---

    /**
     * No-arg constructor.
     */
    public StudioModel() {
        this.pcs = new PropertyChangeSupport(this);

        pcs.registerDependency("driver", new String[] { "connectionRef" });
        pcs.registerDependency("url", new String[] { "connectionRef" });
    } // end of <init>

    // --- Properties accessors ---

    /**
     * Returns connection reference/ID.
     */
    public long getConnectionRef() {
        return this.connectionRef;
    } // end of getConnectionRef

    /**
     * Sets JDBC |url|.
     *
     * @param url JDBC URL
     * @see #getUrl
     */
    public void setUrl(final String url) {
        final String old = this.url;

        this.url = url;
        this.connectionRef = System.currentTimeMillis();

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
        this.connectionRef = System.currentTimeMillis();
        
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
     * @param validate Connection validated?
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
