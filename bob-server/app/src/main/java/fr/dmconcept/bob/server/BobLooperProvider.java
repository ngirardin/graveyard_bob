package fr.dmconcept.bob.server;

import android.util.Log;
import fr.dmconcept.bob.server.models.Project;
import fr.dmconcept.bob.server.models.ServoConfig;
import fr.dmconcept.bob.server.models.Step;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.Sequencer;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;

import java.util.ArrayList;

public class BobLooperProvider implements IOIOLooperProvider {

    private static final String TAG = "BobServer.BobLooperProvider -------------- ";

    Project mProject;

    public synchronized void play(Project project) {

        Log.i(TAG, "play(" + project.id + ")");
        Log.i(TAG, "Board config: " + project.boardConfig.name);

        for (ServoConfig s: project.boardConfig.servoConfigs)
            Log.i(TAG, "  " + s.port + ": " + s.start +" ms to " + s.end + " ms");

        Log.i(TAG, "Steps:");
        for (Step s: project.steps)
            Log.i(TAG, "  " + s.duration + " ms - " + s.positions);

        this.mProject = project;
        notifyAll();
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {

        Log.i(TAG, "createIOIOLooper(" + connectionType + ", " + extra + ")");

        return new IOIOLooper() {

            // The interval to loop when waiting for the queue
            final int IDLE_DURATION = 500;

            // The interval at which the servo position is sent to the IOIO board
            final int SLICE_DURATION = 20;

            IOIO mIoio;

            ArrayList<PwmOutput> mPins = new ArrayList<PwmOutput>();

            //TODO use class
            int playStepsCurrentStep;
            int playStepsSlices;
            int playStepsCurrentSlice;

            @Override
            public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {

                mIoio = ioio;

                // Wait for sequence to setup the queue
                while (mProject == null) {

                    Log.i(TAG, "setup() mProject=" + mProject + " thread=" + Thread.currentThread().getId());

                    Sequencer.ChannelConfig[] channelConfig = new Sequencer.ChannelConfig[] {
                        new Sequencer.ChannelConfigPwmPosition(Sequencer.Clock.CLK_250K, 20, 1500, new DigitalOutput.Spec(3)),
                        new Sequencer.ChannelConfigPwmPosition(Sequencer.Clock.CLK_250K, 20, 1500, new DigitalOutput.Spec(4)),
                        new Sequencer.ChannelConfigPwmPosition(Sequencer.Clock.CLK_250K, 20, 1500, new DigitalOutput.Spec(5))
                    };

                    Sequencer sequencer = ioio.openSequencer(channelConfig);

                    sequencer.manualStart(new Sequencer.ChannelCue[] {
                        new Sequencer.ChannelCuePwmPosition()
                    });

                    sequencer.close();


                    /*
                    if (mIoioQueue.hasSequence()) {

                        // Setup the board pins
                        for (ServoConfig servoConfig : mIoioQueue.getServoConfigs()) {
                            Log.i(TAG, "setup() - Setting up pin " + servoConfig.port + " to " + servoConfig.FREQUENCY);
                            mPins.add(mIoio.openPwmOutput(servoConfig.port, servoConfig.FREQUENCY));
                        }

                        Log.i(TAG, "setup() - Done");

                        return;
                    }
                    */

                    Thread.sleep(IDLE_DURATION);

                }

            }

            @Override
            public void loop() throws ConnectionLostException, InterruptedException {

                Log.i(TAG, "loop() project=" + mProject.id + " thread=" + Thread.currentThread().getId());

                /*
                //TODO check that the servo config is the same
                if (!mIoioQueue.hasSequence()) {
                    Log.i(TAG, "loop() - Waiting for a sequence...");
                    Thread.sleep(IDLE_DURATION);
                    return;
                }

                if (mIoioQueue.getSteps().size() == 1)
                    playPosition();
                else
                    playSteps();
                */

                /*
                Thread.sleep(SLICE_DURATION);
                */
                Thread.sleep(IDLE_DURATION);

            }

            /*
            private void playPosition() throws ConnectionLostException {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Playing the postion...", Toast.LENGTH_SHORT).show();
                    }
                });

                // Send the servo positions
                for (int i = 0; i < mIoioQueue.getServoConfigs().size(); i++) {

                    // Get the servo config for the position
                    ServoConfig servoConfig = mIoioQueue.getServoConfigs().get(i);

                    Step step = mIoioQueue.getSteps().get(0);
                    float position = step.getPositions().get(i);
                    float pulseWidth = servoConfig.start + ((servoConfig.end - servoConfig.start) * position / 100f);

                    Log.i(TAG, "playPosition() - Setting servo " + i + " to " + position + "% = " + pulseWidth + " micro s");

                    // Add the pulse duration for the servo to the batch
                    mPins.get(i).setPulseWidth(pulseWidth);
                }

                mIoioQueue.clear();

            }

            private void playSteps() throws ConnectionLostException {

                if (playStepsCurrentSlice == 0) {

                    Log.i(TAG, "playSteps() - Current step changed to " + playStepsCurrentStep);

                    // First slice of the step, compute the slice count
                    int stepDuration = mIoioQueue.getStep(playStepsCurrentStep).getDuration();
                    playStepsSlices = stepDuration / SLICE_DURATION;

                    Log.i(TAG, "playSteps() - Step duration: " + stepDuration + " ms = " + playStepsSlices + " * " + SLICE_DURATION + " ms");
                }

                mIoio.beginBatch();

                for (int servo = 0; servo < mIoioQueue.getServoConfigs().size(); servo++) {

                    float servoMinPosition = mIoioQueue.getServoConfigs().get(servo).start;
                    float servoMaxPosition = mIoioQueue.getServoConfigs().get(servo).end;

                    float startPercentage = mIoioQueue.getStep(playStepsCurrentStep).getPosition(servo);
                    float endPercentage = mIoioQueue.getStep(playStepsCurrentStep + 1).getPosition(servo);

                    // The interpolated position
                    float slicePercentage = (endPercentage - startPercentage) / playStepsSlices;
                    float currentPercentage = startPercentage + (slicePercentage * playStepsCurrentSlice);

                    float position = servoMinPosition + ((servoMaxPosition - servoMinPosition) * currentPercentage / 100);

//                    Log.i(TAG, "playStep() slice: " + playStepsCurrentSlice + " = " + currentPercentage + "/1 = " + position +  " ms");

                    // Batch the current position
                    mPins.get(servo).setPulseWidth(position);
                }

                mIoio.endBatch();

                playStepsCurrentSlice++;

                if (playStepsCurrentSlice == playStepsSlices) {

                    // End of the step
                    Log.i(TAG, "playSteps() - Step " + playStepsCurrentStep + " done");

                    // Reset the slices count and the current slice index
                    playStepsSlices = 0;
                    playStepsCurrentSlice = 0;

                    playStepsCurrentStep++;

                    if (playStepsCurrentStep == mIoioQueue.getSteps().size() - 1) {

                        Log.i(TAG, "playStep() - Playing sequence done");
                        playStepsCurrentStep = 0;
                        mIoioQueue.clear();

                    }

                }

            }
            */

            @Override
            public void disconnected() {

                Log.i(TAG, "disconnected()");

                /*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setIoioConnected(false);
                    }
                });
                */

            }

            @Override
            public void incompatible() {

                Log.i(TAG, "incompatible()");

                /*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFatalErrorDialog("Incompatible IOIO board", "The connected IOIO board must be updated to at least the version 5 firmware");
                    }
                });
                */

            }

        };

    }

}
