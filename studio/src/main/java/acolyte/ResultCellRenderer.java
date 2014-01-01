package acolyte;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Cell renderer for result.
 *
 * @author Cedric Chantepie
 */
final class ResultCellRenderer implements javax.swing.table.TableCellRenderer {

    /**
     * {@inheritDoc}
     */
    public JLabel getTableCellRendererComponent(final JTable table, 
                                                final Object value, 
                                                final boolean isSelected, 
                                                final boolean hasFocus, 
                                                final int row, 
                                                final int column) {
            
        final String str = (value == null) ? "NULL" : value.toString();
        final JLabel cell = new JLabel(str);
            
        cell.setForeground((value == null) ? Color.LIGHT_GRAY : Color.BLACK);
            
        return cell;
    } // end of getTableCellRendererComponent
} // end of ResultCellRenderer
