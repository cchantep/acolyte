package acolyte;

import java.util.Vector;

/**
 * Table model with not-editable cells.
 */
final class NotEditableTableModel extends melasse.swing.TableModel {
    /** 
     * Bulk constructor.
     */
    protected NotEditableTableModel(final Vector data, 
                                    final Vector columnNames) {

        super(data, columnNames);
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    } // end of isCellEditable

    /**
     * {@inheritDoc}
     */
    public void setValue(int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException();
    } // end of setValue
} // end of class NotEditableTableModel
