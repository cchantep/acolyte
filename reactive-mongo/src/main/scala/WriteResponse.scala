package acolyte
package reactivemongo

import _root_.reactivemongo.io.netty.channel.ChannelId

/** Write response factory. */
object WriteResponse {
  /** Creates a response for given `body`. */
  def apply[T](body: ⇒ T)(implicit mkResponse: WriteResponseMaker[T]): PreparedResponse = new PreparedResponse {
    def apply(chanId: ChannelId) = mkResponse(chanId, body)
  }

  /**
   * Named factory for error response.
   *
   * @param message Error message
   */
  def failed(message: String) = apply(message)

  /**
   * Named factory for error response.
   *
   * @param message Error message
   * @param code Error code
   */
  def failed(message: String, code: Int) = apply(message → code)

  /**
   * Factory for successful response.
   *
   * @param count The number of documents affected by last command, 0 if none
   * @param updatedExisting Some existing document has been updated
   */
  def successful(count: Int = 0, updatedExisting: Boolean = false) =
    apply(count → updatedExisting)

  /**
   * Undefined response, returned by handler no supporting
   * a specific write operation that may be handled by others.
   */
  lazy val undefined = apply(None)
}
