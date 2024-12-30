package acolyte.jdbc;

import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * Row list result.
 *
 * @author Cedric Chantepie
 */
public interface Result<SELF extends Result> {
    
    /**
     * Returns result with given |warning|.
     *
     * @param warning the SQL warning
     * @return new result with given warning
     */
    public SELF withWarning(SQLWarning warning);

    /**
     * Returns result with warning for given |reason|.
     *
     * @param reason the warning reason
     * @return new result with specified warning
     */
    public SELF withWarning(String reason);

    /**
     * Returns associated warning.
     * @return the SQL warning, or null
     */
    public SQLWarning getWarning();

    /**
     * Returns result with given SQL exception
     * that will be thrown by each method declaring it.
     *
     * @param message the SQL exception
     * @return new result with specified exception
     */
    public SELF withException(SQLException message);

    /**
     * Returns result with message for an SQL exception
     * that will be thrown by each method declaring it.
     *
     * @param message the SQL exception message
     * @return new result with specified exception message
     */
    public SELF withException(String message);

    /**
     * Returns associated exception.
     * @return the SQL exception, or null
     */
    public SQLException getException();

} // end class Result
