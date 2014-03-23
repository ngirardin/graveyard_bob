package fr.dmconcept.bob.client.communications;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import fr.dmconcept.bob.client.models.serializers.ProjectStepSerializer;
import fr.dmconcept.bob.client.models.serializers.ServoConfigSerializer;

class SendStepsAsyncTask extends AsyncTask<SendStepInput, Void, SendStepResult> {

    private static final String TAG = "communication.SendSteps";

    private static final String RESPONSE_OK = "BOB";

    private String mHost;

    private int mPort;

    public SendStepsAsyncTask(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    @Override
    protected SendStepResult doInBackground(SendStepInput... params) {

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

    private void sendRequest(SendStepInput input) throws IOException {

        Log.i(TAG, "sendRequest()");

        String serializedServoConfigs = ServoConfigSerializer.serialize(input.getBoardConfig().getServoConfigs());
        String serializedSteps        = ProjectStepSerializer.serialize(input.getSteps());

        String url = "http://" + mHost + ":" + mPort + "/step?servoconfig=" + serializedServoConfigs + "&steps=" + serializedSteps;

        Log.i(TAG, "sendRequest() -> " + url);

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost     = new HttpPost(url);

        HttpResponse response = httpClient.execute(httpPost);

        int status = response.getStatusLine().getStatusCode();

        if (status != HttpStatus.SC_OK) {
            Log.d(TAG, "sendRequest() <- HTTP " + status);
            throw new RuntimeException("HTTP " + status);
        }

        String body = readIt(response.getEntity().getContent());

        Log.d(TAG, "sendRequest() <- HTTP " + status + " - " + body);

        if (!body.equals(RESPONSE_OK))
            throw new RuntimeException("Invalid response: " + body);

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
