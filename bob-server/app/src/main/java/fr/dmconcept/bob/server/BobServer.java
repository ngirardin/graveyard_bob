package fr.dmconcept.bob.server;

import android.util.Log;
import fi.iki.elonen.NanoHTTPD;
import fr.dmconcept.bob.server.models.BoardConfig;
import fr.dmconcept.bob.server.models.Project;
import fr.dmconcept.bob.server.models.ServoConfig;
import fr.dmconcept.bob.server.models.Step;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BobServer extends NanoHTTPD {

    public interface BobServerListener {

        void onServerStarted();

        void onCantStart(IOException exception);

        void onPlayRequest(Project project);

    }

    private static final String TAG = "BobServer";

    private static int PORT = 8000;

    private static final String ROOT_URL      = "/play";
    private static final String PROJECT_PARAM = "project";

    private static final String RESPONSE_OK = "BOB";
    private static final String RESPONSE_NOT_FOUND = "Not found";

    BobServerListener mListener;

    public BobServer(BobServerListener playRequestListener) {
        super(PORT);
        this.mListener = playRequestListener;

        try {
            start();
            mListener.onServerStarted();
        } catch (IOException e) {
            mListener.onCantStart(e);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {

//        Log.i(TAG, "serve() - " + session.getMethod() + " " + session.getUri() + " " + session.getParms());

        if (session.getMethod() == Method.GET && session.getUri().equals(ROOT_URL)) {

            String json = session.getParms().get(PROJECT_PARAM);

            try {

                Project project = deserializeProject(json);
                mListener.onPlayRequest(project);
                return new Response(Response.Status.OK, MIME_PLAINTEXT, RESPONSE_OK);

            } catch (JSONException e) {

                Log.e("BobServer", "Can't deserialize the JSON: " + json, e);
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Bad request");

            }

        } else {
            Log.e(TAG, "BobServer.serve() Invalid request: " + session.getMethod() + " " + session.getUri() + " " + session.getParms());
            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, RESPONSE_NOT_FOUND);
        }

    }

    private Project deserializeProject(String project) throws JSONException {

        JSONObject root = new JSONObject(project);

        return new Project(
            root.getString("id"  ),
            root.getString("name"),
            deserializeBoardConfig(root.getJSONObject("boardConfig")),
            deserializeSteps(root.getJSONArray("steps"))
        );

    }

    private BoardConfig deserializeBoardConfig(JSONObject json) throws JSONException {

        return new BoardConfig(
            json.getString("id"  ),
            json.getString("name"),
            deserializeServoConfigs(json.getJSONArray("servoConfigs"))
        );

    }

    private List<ServoConfig> deserializeServoConfigs(JSONArray json) throws JSONException {

        ArrayList<ServoConfig> servoConfigs = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {

            JSONObject jsonServoConfig = json.getJSONObject(i);

            JSONArray  timings = jsonServoConfig.getJSONArray("timings");

            servoConfigs.add(
                new ServoConfig(
                    jsonServoConfig.getInt("port"),
                    timings.getInt(0),
                    timings.getInt(1)
                )
            );
        }

        return servoConfigs;

    }

    private List<Step> deserializeSteps(JSONArray json) throws JSONException {

        ArrayList<Step> steps = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {

            JSONObject jsonStep = json.getJSONObject(i);

            steps.add(
                new Step(
                    jsonStep.getInt("duration"),
                    deserializePositions(jsonStep.getJSONArray("positions"))
                )
            );

        }

        return steps;

    }

    private ArrayList<Integer> deserializePositions(JSONArray json) throws JSONException {

        ArrayList<Integer> positions = new ArrayList<>();

        for (int i = 0; i < json.length(); i++)
            positions.add(json.getInt(i));

        return positions;

    }

}
