package acolyte.reactivemongo;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Reference factory with underlying actor system provided.
 */
public interface ActorRefFactory {

    /**
     * Returns actor reference according given properties.
     *
     * @param system Actor system
     * @param props Actor properties
     */
    public ActorRef actorOf(ActorSystem system, Props props);

    /**
     * Returns actor reference according given properties and name.
     *
     * @param system Actor system
     * @param props Actor properties
     * @param name Actor name
     */
    public ActorRef actorOf(ActorSystem system, Props props, String name);

} // end of class ActorRefFactory
