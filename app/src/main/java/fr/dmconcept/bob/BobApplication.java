package fr.dmconcept.bob;

import android.app.AlertDialog;
import android.app.Application;
import android.util.Log;

import fr.dmconcept.bob.alljoyn.AllJoynThread;

public class BobApplication extends Application implements Observable {

    /**
     * Load the AllJoyn native lib
     */
    static {
        System.loadLibrary("alljoyn_java");
    }

    static final String TAG = "BobApplication";

    public static String PACKAGE_NAME;


    @Override
    public void onCreate() {

        PACKAGE_NAME = getApplicationContext().getPackageName();

        Log.i(TAG, "onCreate() - PACKAGE_NAME: " + PACKAGE_NAME);
        startBusThread();

    }

    private void startBusThread() {

        // TODO start the thread
        AllJoynThread allJoynRunnable = new AllJoynThread(this);
        Thread allJoynThread = new Thread(allJoynRunnable);

        allJoynThread.start();

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

    public void newRemoteUserMessage(String nickname, String message) {

        new AlertDialog.Builder(this)
                .setMessage("newRemoteUserMessage from " + nickname + ": " + message)
                .show();

    }

}
