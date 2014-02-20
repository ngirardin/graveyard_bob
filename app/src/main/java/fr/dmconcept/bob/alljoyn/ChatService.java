package fr.dmconcept.bob.alljoyn;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;

/**
 * Our chat messages are going to be Bus Signals multicast out onto an
 * associated session.  In order to send signals, we need to define an
 * AllJoyn bus object that will allow us to instantiate a signal emmiter.
 */
public class ChatService implements ChatInterface, BusObject {
    /**
     * Intentionally empty implementation of Chat method.  Since this
     * method is only used as a signal emitter, it will never be called
     * directly.
     */
    public void Chat(String str) throws BusException {
    }

}

