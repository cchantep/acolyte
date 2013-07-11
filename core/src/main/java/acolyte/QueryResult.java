package acolyte;

/**
 * Query result.
 *
 * @author Cedric Chantepie
 */
public interface QueryResult extends Result<QueryResult> {

    /**
     * Returns underlying row list.
     */
    public RowList<?> getRowList();

} // end of interface QueryResult
