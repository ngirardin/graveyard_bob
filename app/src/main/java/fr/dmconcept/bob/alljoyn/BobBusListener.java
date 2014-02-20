package fr.dmconcept.bob.alljoyn;

import android.util.Log;

import org.alljoyn.bus.BusListener;

/**
 * The ChatBusListener is a class that listens to the AllJoyn bus for
 * notifications corresponding to the existence of events happening out on
 * the bus.  We provide one implementation of our listener to the bus
 * attachment during the connect().
 */
public class BobBusListener extends BusListener {

    static final String TAG = "alljoyn.BusListener";

    /**
     * This method is called when AllJoyn discovers a remote attachment
     * that is hosting an chat channel.  We expect that since we only
     * do a findAdvertisedName looking for instances of the chat
     * well-known name prefix we will only find names that we know to
     * be interesting.  When we find a remote application that is
     * hosting a channel, we add its channel name it to the list of
     * available channels selectable by the user.
     *
     * In the class documentation for the BusListener note that it is a
     * requirement for this method to be multithread safe.  This is
     * accomplished by the use of a monitor on the ChatApplication as
     * exemplified by the synchronized attribute of the addFoundChannel
     * method there.
     */
    public void foundAdvertisedName(String name, short transport, String namePrefix) {

        Log.i(TAG, "foundAdvertisedName(" + name + ")");

        /*
        ChatApplication application = (ChatApplication)getApplication();
        application.addFoundChannel(name);
        */

    }

    /**
     * This method is called when AllJoyn decides that a remote bus
     * attachment that is hosting an chat channel is no longer available.
     * When we lose a remote application that is hosting a channel, we
     * remote its name from the list of available channels selectable
     * by the user.
     *
     * In the class documentation for the BusListener note that it is a
     * requirement for this method to be multithread safe.  This is
     * accomplished by the use of a monitor on the ChatApplication as
     * exemplified by the synchronized attribute of the removeFoundChannel
     * method there.
     */
    public void lostAdvertisedName(String name, short transport, String namePrefix) {

        Log.i(TAG, "lostAdvertisedName(" + name + ")");

        /*
        ChatApplication application = (ChatApplication)getApplication();
        application.removeFoundChannel(name);
        */

    }

}

