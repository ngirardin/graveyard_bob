package fr.dmconcept.bob.client.communications;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import fr.dmconcept.bob.client.BobApplication;
import fr.dmconcept.bob.client.models.BoardConfig;
import fr.dmconcept.bob.client.models.Project;
import fr.dmconcept.bob.client.models.Step;

/**
 *
 * This class handles all the communication with the Bob Server
 *
 */
public class BobCommunication {

    private static final String TAG = "communications.BobCommunication";

    private static final int PORT = 8000;

    Context mApplication;

    public BobCommunication(Context application) {
        this.mApplication = application;
    }

    public void sendStep(BoardConfig boardConfig, Step step) throws NetworkErrorException, NoInterfaceException {

        Log.i(TAG, "sendStep(boardconfig, step)");

        ArrayList<Step> steps = new ArrayList<Step>();
        steps.add(step);

        launchSendSteps(new SendStepInput(boardConfig, steps));

    }

    public void sendSteps(BoardConfig boardConfig, Step startStep, Step endStep) throws NetworkErrorException, NoInterfaceException {

        Log.i(TAG, "sendSteps(boardconfig, startStep, endStep)");

        ArrayList<Step> steps = new ArrayList<Step>();
        steps.add(startStep);
        steps.add(endStep  );

        launchSendSteps(new SendStepInput(boardConfig, steps));

    }

    public void sendSteps(Project project) throws NetworkErrorException, NoInterfaceException {

        Log.i(TAG, "sendSteps(project)");

        launchSendSteps(new SendStepInput(project.getBoardConfig(), project.getSteps()));

    }

    private void launchSendSteps(SendStepInput input) throws NetworkErrorException, NoInterfaceException {

        Log.i(TAG, "launchSendSteps()");

        if (!isNetworkAvailable())
            throw new NoInterfaceException();

        try {

            // Read the server IP from the preferences
            String serverIP = mApplication.getSharedPreferences(mApplication.getPackageName(), BobApplication.MODE_PRIVATE).getString(BobApplication.PREFERENCES_SERVER_IP, "");

            if (serverIP.isEmpty())
               throw new RuntimeException("Empty server IP address");

            SendStepResult result = new SendStepsAsyncTask(serverIP, PORT).execute(input).get();

            if (result.isError())
                throw new NetworkErrorException();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private boolean isNetworkAvailable() {

        ConnectivityManager connMgr = (ConnectivityManager) mApplication.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected())
            return false;

        boolean isEthernet = networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET;
        boolean isWifi     = networkInfo.getType() == ConnectivityManager.TYPE_WIFI    ;

        return isEthernet || isWifi;

    }

}
