package fr.dmconcept.bob;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Step;
import fr.dmconcept.bob.models.serializers.ProjectStepSerializer;
import fr.dmconcept.bob.models.serializers.ServoConfigSerializer;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 *
 * This class handles all the communication with the Bob Server
 *
 */
public class BobCommunication {

    private static final String TAG = "BobCommunication";

    private static final int PORT = 8000;

    private static final String mHost = "192.168.0.36";

    Context mContext;

    public BobCommunication(Context context) {
        this.mContext = context;
    }

    // TODO extract to class
    private class SendStepInput {

        BoardConfig     mBoardConfig;
        ArrayList<Step> mSteps;

        public SendStepInput(BoardConfig boardConfig, ArrayList<Step> steps) {
            mBoardConfig = boardConfig;
            mSteps       = steps;
        }

        public BoardConfig getBoardConfig() {
            return mBoardConfig;
        }

        public ArrayList<Step> getSteps() {
            return mSteps;
        }

    }

    //TODO extract to class
    private class SendStepResult {

        String mError ;

        public SendStepResult() {
        }

        public void setError(String message) {
           mError = message;
        }

        public boolean isError(){
            return mError != null;
        }

    }

    //TODO extracts to class
    private class SendSteps extends AsyncTask<SendStepInput, Void, SendStepResult> {

        private static final String TAG = "communication.SendSteps";

        @Override
        protected SendStepResult doInBackground(SendStepInput... params) {

            Log.i(TAG, "doInBackground()");

            SendStepResult result = new SendStepResult();

            try {

                Log.i(TAG, "doInBackground() - Sending request...");
                sendRequest(params[0]);
                Log.i(TAG, "doInBackground() - Request sent successfully");

            } catch (IOException e) {

                result.setError(e.getMessage());

                Log.e(TAG, "doInBackground() - Request error, stack trace below");
                e.printStackTrace();
            }

            return result;

        }

        private String sendRequest(SendStepInput input) throws IOException {

            Log.i(TAG, "sendRequest()");

            String serializedServoConfigs = ServoConfigSerializer.serialize(input.getBoardConfig().getServoConfigs());
            String serializedSteps        = ProjectStepSerializer.serialize(input.getSteps());

            String url = "http://" + mHost + ":" + PORT + "/step?servoconfig=" + serializedServoConfigs + "&steps=" + serializedSteps;

            Log.i(TAG, "sendRequest() -> " + url);

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost   httpPost   = new HttpPost(url);

            HttpResponse response = httpClient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();

            Log.d(TAG, "sendRequest() <- HTTP " + status);

            if (status != HttpStatus.SC_OK)
                throw new RuntimeException("HTTP " + status);

            return readIt(response.getEntity().getContent());

        }

        // Reads an InputStream and converts it to a String.
        private String readIt(InputStream stream) throws IOException {
            Reader reader;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[1024];
            reader.read(buffer);
            return new String(buffer);
        }

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

            SendStepResult result = new SendSteps().execute(input).get();

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
