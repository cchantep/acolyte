package acolyte;

import java.sql.SQLWarning;

/**
 * Update result.
 *
 * @author Cedric Chantepie
 */
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

    // --- Constructors ---

    /**
     * Bulk constructor.
     */
    private UpdateResult(final int count, final SQLWarning warning) {
        this.count = count;
        this.warning = warning;
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
     * Returns update count.
     */
    public int getUpdateCount() {
        return this.count;
    } // end of getUpdateCount

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
