package acolyte;

import java.sql.SQLWarning;

/**
 * Update result.
 *
 * @author Cedric Chantepie
 * @deprecated Use {@link acolyte.jdbc.UpdateResult}
 */
@Deprecated
public final class UpdateResult implements Result<UpdateResult> {
    // --- Shared ---

    /**
     * No result instance
     */
    public static final UpdateResult Nothing = new UpdateResult();

    /**
     * Result for 1 updated row
     */
    public static final UpdateResult One = new UpdateResult(1);

    // --- Properties ---

    public final int count;
    public final SQLWarning warning;
    public final RowList<?> generatedKeys;

    // --- Constructors ---

    /**
     * Bulk constructor.
     */
    private UpdateResult(final int count, 
                         final RowList<?> generatedKeys,
                         final SQLWarning warning) {

        this.count = count;
        this.generatedKeys = generatedKeys;
        this.warning = warning;
    } // end of <init>

    /**
     * With-warning constructor.
     */
    private UpdateResult(final int count, final SQLWarning warning) {
        this(count, null, warning);
    } // end of <init>

    /**
     * No result constructor.
     */
    private UpdateResult() { 
        this(0, null);
    } // end of <init>

    /**
     * Count constructor.
     */
    public UpdateResult(final int count) {
         this(count, null);
    } // end of <init>

    // ---

    /**
     * Returns either null if there is no row resulting from update, 
     * or associated row list.
     */
    public RowList<?> getGeneratedKeys() {
        return this.generatedKeys;
    } // end of getGeneratedKeys

    /**
     * Returns update count.
     */
    public int getUpdateCount() {
        return this.count;
    } // end of getUpdateCount

    /**
     * Returns result with updated row |keys|.
     * @param keys Generated keys
     */
    public UpdateResult withGeneratedKeys(final RowList<?> keys) {
        return new UpdateResult(this.count, keys, this.warning);
    } // end of withGeneratedKeys

    /**
     * {@inheritDoc}
     */
    public UpdateResult withWarning(final SQLWarning warning) {
        return new UpdateResult(this.count, warning);
    } // end of withWarning

    /**
     * {@inheritDoc}
     */
    public UpdateResult withWarning(final String reason) {
        return withWarning(new SQLWarning(reason));
    } // end of withWarning
    
    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarning() {
        return this.warning;
    } // end of getWarning
} // end of interface UpdateResult
