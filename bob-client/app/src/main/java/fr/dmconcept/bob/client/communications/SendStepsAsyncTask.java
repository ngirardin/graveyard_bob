package fr.dmconcept.bob.client.communications;

import android.os.AsyncTask;
import android.util.Log;
import fr.dmconcept.bob.client.models.serializers.ProjectStepSerializer;
import fr.dmconcept.bob.client.models.serializers.ServoConfigSerializer;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

class SendStepsAsyncTask extends AsyncTask<SendStepInput, Void, SendStepResult> {

    private static final String TAG = "communication.SendSteps";

    private String mHost;

    private int mPort;

    public SendStepsAsyncTask(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

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

            Log.i(TAG, "doInBackground() - Request error: " + e.getMessage());
        }

        return result;

    }

    private String sendRequest(SendStepInput input) throws IOException {

        Log.i(TAG, "sendRequest()");

        String serializedServoConfigs = ServoConfigSerializer.serialize(input.getBoardConfig().getServoConfigs());
        String serializedSteps        = ProjectStepSerializer.serialize(input.getSteps());

        String url = "http://" + mHost + ":" + mPort + "/step?servoconfig=" + serializedServoConfigs + "&steps=" + serializedSteps;

        Log.i(TAG, "sendRequest() -> " + url);

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost   = new HttpPost(url);

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
