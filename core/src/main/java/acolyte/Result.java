package acolyte;

import java.sql.SQLWarning;

/**
 * Row list result.
 *
 * @author Cedric Chantepie
 */
public interface Result {
    
    /**
     * Returns underlying row list.
     */
    public RowList<?> getRowList();

    /**
     * Returns result with given |warning|.
     */
    public Result withWarning(SQLWarning warning);
    
} // end class Result
