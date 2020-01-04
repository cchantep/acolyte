package reactivemongo // as a friend project

import reactivemongo.core.protocol.Response

import reactivemongo.core.netty.ChannelFactory

import reactivemongo.api.MongoConnectionOptions

import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONSerializationPack

package object acolyte {
  @inline def channelFactory(
    supervisor: String,
    name: String,
    options: MongoConnectionOptions) =
    new ChannelFactory(supervisor, name, options)

  val parseResponse: Response => Iterator[BSONDocument] =
    Response.parse(BSONSerializationPack)(_)
}
