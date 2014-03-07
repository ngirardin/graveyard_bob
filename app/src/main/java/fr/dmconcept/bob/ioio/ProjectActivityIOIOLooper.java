package fr.dmconcept.bob.ioio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import fr.dmconcept.bob.activities.ProjectActivity;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.ServoConfig;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

import java.util.ArrayList;

public class ProjectActivityIOIOLooper extends BaseIOIOLooper {

    private static final String TAG = "activites.ProjectActivityIOIOLooper";

    // Define the interval at which the servo position is sent to the IOIO board
    private static final int SLICE_LENGTH = 20;

    ProjectActivity mProjectActivity;

    BoardConfig mBoardConfig;

    ArrayList<PwmOutput> mPins = new ArrayList<PwmOutput>();

    public ProjectActivityIOIOLooper(ProjectActivity projectActivity, BoardConfig boardConfig) {

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

        if (!mProjectActivity.ioioQueue.consumed) {

           Log.i(TAG, "loop() playing the new position: " + mProjectActivity.ioioQueue);

            ioio_.beginBatch();

            try {

                if (mProjectActivity.ioioQueue.duration > 0) {
                    playStep();
                } else if (mProjectActivity.ioioQueue.startPosition != null) {
                    playStart();
                    mProjectActivity.ioioQueue.consumed();
                } else if (mProjectActivity.ioioQueue.endPosition != null) {
                    playEnd();
                    mProjectActivity.ioioQueue.consumed();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                ioio_.endBatch();
            }

        }

        Thread.sleep(SLICE_LENGTH);

    }

    //TODO move to queue
    // The current slice when playing a step
    int currentSlice = 0;

    private void playStep() throws ConnectionLostException {

        int duration = mProjectActivity.ioioQueue.duration;

        int totalSlices = duration / SLICE_LENGTH;

        if (currentSlice == 0)
            mProjectActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mProjectActivity, "Playing the sequence...", Toast.LENGTH_SHORT);
                }
            });

        if (currentSlice > totalSlices) {
            Log.i(TAG, "playStep() step play done");

            mProjectActivity.ioioQueue.consumed();
            currentSlice = 0;

            mProjectActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mProjectActivity, "The sequence is done", Toast.LENGTH_SHORT);
                }
            });

            return;
        }

        for (int i = 0; i < mProjectActivity.ioioQueue.endPosition.size(); i++) {

            // Interpolate the position
            //TODO store interpoled data before loop()
            float startPercentage   = mProjectActivity.ioioQueue.startPosition.get(i);
            float endPercentage     = mProjectActivity.ioioQueue.endPosition.get(i);
            float slicePercentage   = (endPercentage - startPercentage) / totalSlices;

            float currentPercentage = startPercentage + (slicePercentage * currentSlice);

            float servoMinPosition = mBoardConfig.getServoConfigs().get(i).start;
            float servoMaxPosition = mBoardConfig.getServoConfigs().get(i).end;

            float position = servoMinPosition + ((servoMaxPosition - servoMinPosition) * currentPercentage / 100);

            Log.i(TAG, "playStep() " +  duration + " ms: " + currentSlice + "/" + totalSlices + " " + slicePercentage + "% " + startPercentage + "% -> " + endPercentage + "% : " + currentPercentage + "%");
            PwmOutput pin = mPins.get(i);

            pin.setPulseWidth(position);
        }

        currentSlice++;

    }

    private void playStart() throws ConnectionLostException {

        for (int i = 0; i < mProjectActivity.ioioQueue.startPosition.size(); i++) {

            float minPosition = mBoardConfig.getServoConfigs().get(i).start;
            float maxPosition = mBoardConfig.getServoConfigs().get(i).end;

            float positionPercent = mProjectActivity.ioioQueue.startPosition.get(i);
            float position        = minPosition + ((maxPosition - minPosition) * positionPercent / 100);

            PwmOutput pin = mPins.get(i);

            pin.setPulseWidth(position);
        }

    }

    private void playEnd() throws ConnectionLostException {

        for (int i = 0; i < mProjectActivity.ioioQueue.endPosition.size(); i++) {

            float minPosition = mBoardConfig.getServoConfigs().get(i).start;
            float maxPosition = mBoardConfig.getServoConfigs().get(i).end;

            float positionPercent = mProjectActivity.ioioQueue.endPosition.get(i);
            float position        = minPosition + ((maxPosition - minPosition) * positionPercent / 100);

            PwmOutput pin = mPins.get(i);

            pin.setPulseWidth(position);
        }

    }

    @Override
    public void disconnected() {
        Log.i(TAG, "disconnected()");
        mProjectActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mProjectActivity, "IOIO board disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void incompatible() {
        Log.i(TAG, "incompatible");
        mProjectActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(mProjectActivity)
                    .setTitle("Incompatible IOIO board")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("This IOIO board is incompatible, you need to update it to at least version 5.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            }
        });
    }

}
