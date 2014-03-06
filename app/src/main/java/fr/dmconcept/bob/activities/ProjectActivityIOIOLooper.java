package fr.dmconcept.bob.activities;

import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.ServoConfig;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

import java.util.ArrayList;

public class ProjectActivityIOIOLooper extends BaseIOIOLooper {

    private static final String TAG = "activites.ProjectActivityIOIOLooper";

    ProjectActivity mProjectActivity;

    BoardConfig mBoardConfig;

    ArrayList<PwmOutput> mPins = new ArrayList<PwmOutput>();

    ProjectActivityIOIOLooper(ProjectActivity projectActivity, BoardConfig boardConfig) {

        super();

        mProjectActivity = projectActivity;
        mBoardConfig     = boardConfig;

    }

    @Override
    protected void setup() throws ConnectionLostException, InterruptedException {

        Log.i(TAG, "setup()");

        for (ServoConfig servoConfig: mBoardConfig.getServoConfigs()) {

            int port      = servoConfig.port;
            int frequency = servoConfig.frequency;

            Log.i(TAG, "setup() opnning pin " + port + " at " + frequency + "hz");

            PwmOutput pin = ioio_.openPwmOutput(port, frequency);
            mPins.add(pin);

        }

        mProjectActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast
                    .makeText(mProjectActivity, "IOIO board connected with config " + mBoardConfig.getName(), Toast.LENGTH_SHORT)
                    .show();
            }
        });
    }

    @Override
    public void loop() throws ConnectionLostException, InterruptedException {

        if (mProjectActivity.mIoioPositions != null) {

           Log.i(TAG, "loop() playing the new position: " + mProjectActivity.mIoioPositions);

            ioio_.beginBatch();

            try {

                for (int i = 0; i < mProjectActivity.mIoioPositions.size(); i++) {

                    float positionStart   = mBoardConfig.getServoConfigs().get(i).start;
                    float positionEnd     = mBoardConfig.getServoConfigs().get(i).end;
                    float positionPercent = mProjectActivity.mIoioPositions.get(i);
                    float position        = positionStart + ((positionEnd - positionStart) * positionPercent / 100);

                    PwmOutput pin = mPins.get(i);

                    pin.setPulseWidth(position);

                }
            } catch (Exception e) {
                Toast.makeText(mProjectActivity, "Error setting the servo position", Toast.LENGTH_SHORT).show();
            } finally {
                ioio_.endBatch();
            }

            mProjectActivity.mIoioPositions = null;

        }

        Thread.sleep(500);

    }

    @Override
    public void disconnected() {
        Log.i(TAG, "disconnected()");
        new AlertDialog.Builder(mProjectActivity).setMessage("Disconnected").show();
    }

    @Override
    public void incompatible() {
        Log.i(TAG, "incompatible");
        new AlertDialog.Builder(mProjectActivity).setMessage("Incompatible").show();
    }

}
