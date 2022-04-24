package reactivemongo.api // as a friend project

import reactivemongo.core.protocol.ProtocolMetadata

package object acolyte {
  object AcolyteDB {
    import reactivemongo.api.DB

    /**
     * Creates a DB managed by Acolyte without standard DB resolution,
     * in order to reduce the initialization time.
     */
    @inline def apply(
      connection: MongoConnection,
      name: String,
      metadata: ProtocolMetadata = ProtocolMetadata.Default,
      setName: Option[String] = None,
      isMongos: Boolean = false): DB = {
      val state = ConnectionState(
        metadata = metadata,
        setName = setName,
        isMongos = isMongos)

      new DB(
        name = name,
        connection = connection,
        connectionState = state,
        failoverStrategy = connection.options.failoverStrategy,
        session = None)
    }
  }
}
