package reactivemongo // as a friend project

import reactivemongo.api.MongoConnectionOptions
import reactivemongo.core.nodeset.ChannelFactory

package object acolyte {
  @inline def channelFactory(
    supervisor: String,
    name: String,
    options: MongoConnectionOptions) =
    new ChannelFactory(supervisor, name, options)
}
