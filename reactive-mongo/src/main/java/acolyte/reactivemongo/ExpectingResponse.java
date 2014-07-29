package acolyte.reactivemongo;

import scala.concurrent.Promise;
import scala.concurrent.Future;

import reactivemongo.core.protocol.Response;

/**
 * Wrapper for ReactiveMongo message, expecting a response.
 */
final class ExpectingResponse 
    implements reactivemongo.core.actors.ExpectingResponse {

    // --- Properties ---

    /**
     * Underlying promise
     */
    private final Promise promise;

    // ---

    /**
     * Wrapper constructor. 
     */
    protected ExpectingResponse(reactivemongo.core.actors.
                                ExpectingResponse underlying) {

        this.promise = underlying.promise();
    } // end of <init>

    // ---

    /**
     * Returns current promise.
     */
    public Promise<Response> promise() { return this.promise; }

    /**
     * Returns promise future.
     */
    public Future<Response> future() { return this.promise.future(); }

    /**
     * Unsupported operation.
     */
    public void reactivemongo$core$actors$ExpectingResponse$_setter_$promise_$eq(Promise p) { throw new UnsupportedOperationException(); }

    /**
     * Unsupported operation.
     */
    public void reactivemongo$core$actors$ExpectingResponse$_setter_$future_$eq(Future f) { throw new UnsupportedOperationException(); }
}
