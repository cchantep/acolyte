package reactivemongo.acolyte

import reactivemongo.actors.actor.{ ActorRef, ActorSystem }

package object bindings {
  type MongoConnection = reactivemongo.api.MongoConnection

  object MongoConnection {

    @inline def apply(
        supervisor: String,
        name: String,
        actorSystem: ActorSystem,
        mongosystem: ActorRef,
        options: reactivemongo.api.MongoConnectionOptions
      ) =
      new reactivemongo.api.MongoConnection(
        supervisor,
        name,
        actorSystem,
        mongosystem,
        options
      )
  }
}
