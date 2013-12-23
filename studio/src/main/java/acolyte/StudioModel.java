package acolyte;

import java.beans.PropertyChangeListener;

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

    // --- Constructors ---

    /**
     * No-arg constructor.
     */
    public StudioModel() {
        this.pcs = new PropertyChangeSupport(this);
    } // end of <init>

    // --- Properties accessors ---

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
