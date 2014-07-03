package acolyte;

import java.sql.SQLWarning;

/**
 * Row list result.
 *
 * @author Cedric Chantepie
 * @deprecated Use {@link acolyte.jdbc.Result}
 */
@Deprecated
public interface Result<SELF extends Result> {
    
    /**
     * Returns result with given |warning|.
     */
    public SELF withWarning(SQLWarning warning);

    /**
     * Returns result with warning for given |reason|.
     */
    public SELF withWarning(String reason);

    /**
     * Returns associated warning.
     */
    public SQLWarning getWarning();

} // end class Result
