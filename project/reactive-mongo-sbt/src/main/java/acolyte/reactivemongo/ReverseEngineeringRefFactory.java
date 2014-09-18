package acolyte.reactivemongo;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;

/**
 * Reference factory for reverse engineering of Mongo actors.
 */
public interface ReverseEngineeringRefFactory {

    /**
     * Creates an ActorRef 'before' another.
     *
     * @param system Actor system
     * @param then Actor to which message should be normally forwarded
     */
    public ActorRef before(ActorSystem system, ActorRef then);

} // end of class ReverseEngineeringRefFactory
