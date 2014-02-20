package fr.dmconcept.bob;

import android.app.AlertDialog;
import android.app.Application;
import android.util.Log;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.MessageContext;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.alljoyn.DaemonInit;
import org.alljoyn.bus.annotation.BusSignalHandler;

import fr.dmconcept.bob.alljoyn.BobBusListener;
import fr.dmconcept.bob.alljoyn.ChatService;

public class BobApplication extends Application implements Observable, /**XXX **/ Observer /* XXX move to thread */ {

    static final String TAG = "BobApplication";

    public static String PACKAGE_NAME;

    private static final String OBJECT_PATH = "/bobService";

    /**
     * Load the AllJoyn native lib
     */
    static {
        System.loadLibrary("alljoyn_java");
    }

    private BobApplication mBobApplication = null;

    private BusAttachment mBus  = new BusAttachment(BobApplication.PACKAGE_NAME, BusAttachment.RemoteMessage.Receive);

    public static enum BusAttachmentState {
        DISCONNECTED,	/** The bus attachment is not connected to the AllJoyn bus */
        CONNECTED,		/** The  bus attachment is connected to the AllJoyn bus */
        DISCOVERING		/** The bus attachment is discovering remote attachments hosting chat channels */
    }

    private BusAttachmentState mBusAttachmentState = BusAttachmentState.DISCONNECTED;

    private BobBusListener mBusListener = new BobBusListener();

    private ChatService mChatService = new ChatService();



    @Override
    public void onCreate() {

        PACKAGE_NAME = getApplicationContext().getPackageName();

        Log.i(TAG, "onCreate() - PACKAGE_NAME: " + PACKAGE_NAME);

        startBusThread();
        //XXX
//        mBoboApplication = (BobApplication) getApplication();
//        mBobApplication.addObserver(this);
        //XXX

        addObserver(this);

    }

    private void startBusThread() {
        doConnect();
    }

    // TODO move to own thead
    private void doConnect() {
        Log.i(TAG, "doConnect()");

        DaemonInit.PrepareDaemon(getApplicationContext());
        assert(mBusAttachmentState == BusAttachmentState.DISCONNECTED);

        mBus.useOSLogging(true);
        mBus.setDebugLevel("ALLJOYN_JAVA", 7);

        mBus.registerBusListener(mBusListener);

        /*
         * To make a service available to other AllJoyn peers, first
         * register a BusObject with the BusAttachment at a specific
         * object path.  Our service is implemented by the ChatService
         * BusObject found at the "/chatService" object path.
         */
        Status status = mBus.registerBusObject(mChatService, OBJECT_PATH);

        if (Status.OK != status) {
            throw new RuntimeException("Unable to register the chat bus object");
//            mChatApplication.alljoynError(ChatApplication.Module.HOST, "Unable to register the chat bus object: (" + status + ")");
//            return;
        }

        status = mBus.connect();
        if (status != Status.OK) {

            throw new RuntimeException("Unbale to connect to the bus: " + status);
//            mChatApplication.alljoynError(ChatApplication.Module.GENERAL, "Unable to connect to the bus: (" + status + ")");
//            return;
        }

        /*
        status = mBus.registerSignalHandlers(this);
        if (status != Status.OK) {
            throw new RuntimeException("Unable to register signal handlers: " + status);
//            mChatApplication.alljoynError(ChatApplication.Module.GENERAL, "Unable to register signal handlers: (" + status + ")");
//            return;
        }
        */

        mBusAttachmentState = BusAttachmentState.CONNECTED;
        Log.i(TAG, "doConnect() - Connected");

    }


    /**
     * The session identifier of the "use" session that the application
     * uses to talk to remote instances.  Set to -1 if not connectecd.
     */
    int mUseSessionId = -1;

    /**
     * The session identifier of the "host" session that the application
     * provides for remote devices.  Set to -1 if not connected.
     */
    int mHostSessionId = -1;

    /**
     * A flag indicating that the application has joined a chat channel that
     * it is hosting.  See the long comment in doJoinSession() for a
     * description of this rather non-intuitively complicated case.
     */
    boolean mJoinedToSelf = false;

    /**
     * The signal handler for messages received from the AllJoyn bus.
     *
     * Since the messages sent on a chat channel will be sent using a bus
     * signal, we need to provide a signal handler to receive those signals.
     * This is it.  Note that the name of the signal handler has the first
     * letter capitalized to conform with the DBus convention for signal
     * handler names.
     */
    @BusSignalHandler(iface = "fr.dmconcept.bob.alljoyn", signal = "Chat")
    public void Chat(String string) {

        /*
    	 * See the long comment in doJoinSession() for more explanation of
    	 * why this is needed.
    	 *
    	 * The only time we allow a signal from the hosted session ID to pass
    	 * through is if we are in mJoinedToSelf state.  If the source of the
    	 * signal is us, we also filter out the signal since we are going to
    	 * locally echo the signal.

     	 */
        String uniqueName = mBus.getUniqueName();
        MessageContext ctx = mBus.getMessageContext();
        Log.i(TAG, "Chat(): use sessionId is " + mUseSessionId);
        Log.i(TAG, "Chat(): message sessionId is " + ctx.sessionId);

        /*
         * Always drop our own signals which may be echoed back from the system.
         */
        if (ctx.sender.equals(uniqueName)) {
            Log.i(TAG, "Chat(): dropped our own signal received on session " + ctx.sessionId);
            return;
        }

        /*
         * Drop signals on the hosted session unless we are joined-to-self.
         */
        if (mJoinedToSelf == false && ctx.sessionId == mHostSessionId) {
            Log.i(TAG, "Chat(): dropped signal received on hosted session " + ctx.sessionId + " when not joined-to-self");
            return;
        }

        /*
         * To keep the application simple, we didn't force users to choose a
         * nickname.  We want to identify the message source somehow, so we
         * just use the unique name of the sender's bus attachment.
         */
        String nickname = ctx.sender;
        nickname = nickname.substring(nickname.length()-10, nickname.length());

        Log.i(TAG, "Chat(): signal " + string + " received from nickname " + nickname);

        mBobApplication.newRemoteUserMessage(nickname, string);
    }


    /**
     * This object is really the model of a model-view-controller architecture.
     * The observer/observed design pattern is used to notify view-controller
     * objects when the model has changed.  The observed object is this object,
     * the model.  Observers correspond to the view-controllers which in this
     * case are the Android Activities (corresponding to the use tab and the
     * hsot tab) and the Android Service that does all of the AllJoyn work.
     * When an observer wants to register for change notifications, it calls
     * here.
     */
    public synchronized void addObserver(Observer obs) {
        Log.i(TAG, "addObserver(" + obs + ")");
        /*
        if (mObservers.indexOf(obs) < 0) {
            mObservers.add(obs);
        }
        */
    }

    /**
     * When an observer wants to unregister to stop receiving change
     * notifications, it calls here.
     */
    public synchronized void deleteObserver(Observer obs) {
        Log.i(TAG, "deleteObserver(" + obs + ")");
        /*
        mObservers.remove(obs);
        */
    }

    // TODO move to thread and remove Observer
    public synchronized void update(Observable o, Object arg) {
        Log.i(TAG, "update(" + arg + ")");
        String qualifier = (String)arg;
    }

    private void newRemoteUserMessage(String nickname, String message) {

        new AlertDialog.Builder(this)
            .setMessage("newRemoteUserMessage from " + nickname + ": " + message)
            .show();

    }

}
