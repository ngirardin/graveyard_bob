package fr.dmconcept.bob.communications;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Step;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 *
 * This class handles all the communication with the Bob Server
 *
 */
public class BobCommunication {

    private static final String TAG = "communications.BobCommunication";

    private static final int PORT = 8000;

    private static final String mHost = "192.168.0.36";

    Context mContext;

    public BobCommunication(Context context) {
        this.mContext = context;
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

            SendStepResult result = new SendStepsAsyncTask(mHost, PORT).execute(input).get();

            if (result.isError())
                throw new NetworkErrorException();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private boolean isNetworkAvailable() {

        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected())
            return false;

        boolean isEthernet = networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET;
        boolean isWifi     = networkInfo.getType() == ConnectivityManager.TYPE_WIFI    ;

        return isEthernet || isWifi;

    }

}
