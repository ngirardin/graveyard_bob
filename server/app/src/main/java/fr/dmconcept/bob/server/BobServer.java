package fr.dmconcept.bob.server;

import android.util.Log;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class BobServer extends NanoHTTPD {

    public interface BobServerListener {

        void onCantStart(IOException exception);

        void onPlayRequest(String seralizedServoConfig, String serializedSteps);
    }

    private static final String TAG = "BobServer";

    private static int PORT = 8000;

    private static final String ROUTE_POSITION = "/step";

    private static final String RESPONSE_OK = "BOB";
    private static final String RESPONSE_NOT_FOUND = "Not found";

    BobServerListener mListener;

    public BobServer(BobServerListener playRequestListener) {
        super(PORT);
        this.mListener = playRequestListener;

        try {
            start();
        } catch (IOException e) {
            mListener.onCantStart(e);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {

        Log.i(TAG, "serve() - " + session.getMethod() + " " + session.getUri());

        if (session.getMethod() == Method.POST && session.getUri().equals(ROUTE_POSITION)) {

            //TODO do deserialization here
            String servoConfigParam = session.getParms().get("servoconfig");
            String stepsParam       = session.getParms().get("steps");

            mListener.onPlayRequest(servoConfigParam, stepsParam);

            return new Response(Response.Status.OK, MIME_PLAINTEXT, RESPONSE_OK);
        } else
            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, RESPONSE_NOT_FOUND);

    }

}
