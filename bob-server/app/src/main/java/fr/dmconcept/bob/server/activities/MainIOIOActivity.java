package fr.dmconcept.bob.server.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import fr.dmconcept.bob.server.BobServer;
import fr.dmconcept.bob.server.R;
import fr.dmconcept.bob.server.ioio.ProjectLock;
import fr.dmconcept.bob.server.models.Project;
import fr.dmconcept.bob.server.models.ServoConfig;
import fr.dmconcept.bob.server.models.Step;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.Sequencer;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class MainIOIOActivity extends IOIOActivity {

    private static final String TAG = "BobServer ------------------- ";

    private TextView textViewLog;

    private BobServer mBobServer;

    private ProjectLock mProject = new ProjectLock();

    public static final int PERIOD  = 20000 /* microseconds */; /* 50hz = 0.02s = 20ms = 20.000us */
    public static final int MIN     = 1000 * 2 /* periods */  ; /* 1000us * 0.5us periods         */
    public static final int MAX     = 2000 * 2 /* periods */  ; /* 2000us * 0.5us periods         */
    public static final int PERCENT = new Float((MAX - MIN) / 100f).intValue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_ioio_activity);
        textViewLog = (TextView) findViewById(R.id.textViewLog);

        info("onCreate()");

        findViewById(R.id.buttonQuit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBobServer = new BobServer(new BobServer.BobServerListener() {

            @Override
            public void onCantStart(IOException exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainIOIOActivity.this)
                                .setTitle("Application already running")
                                .setMessage("You can only run one instance of the application")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                });
            }

            @Override
            public void onPlayRequest(Project project) {

//                info("onPlayRequest");

                synchronized (mProject) {

					if (mProject.getProject() != null && !mProject.getProject().boardConfig.id.equals(project.boardConfig.id)) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainIOIOActivity.this)
                                        .setTitle("Reconnect the board")
                                        .setMessage("The project played has a different board configuration, you need to restart the app")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .show();
                            }
                        });

                        return;
                    }

                    mProject.setProject(project);
                    info("Project " + mProject.getProject().name + " duration=" + mProject.getDuration() + " ms, slices=" + mProject.getSliceCount());
                    mProject.notifyAll();
                }
            }
        });

    }

    private void info(final String message) {

        final String t = Thread.currentThread().getName();

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Thread thread: Thread.getAllStackTraces().keySet()) {
            sb.append(thread.getName());
            sb.append(",");
        }
        sb.append("]");

        Log.i(TAG, "-> " + sb.toString());
        Log.i(TAG, t + " - " + message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewLog.setText(textViewLog.getText() + message + "\n");
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        info("newIntent");

        Log.i(TAG, "MainActivity.onNewIntent(" + intent + ")");
        Bundle extras = intent.getExtras();

        if (!intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)) {
            Log.i(TAG, "MainActivity.onNewIntent() - Not an ACTION_USB_ACCESSORY_ATTACHED action");
            return;
        }

        UsbAccessory accessory = (UsbAccessory) extras.get("accessory");

        if (!accessory.getModel().equals("IOIO")) {
            Log.i(TAG, "MainActivity.onNewIntent() - " + accessory + " don't match the IOIO model");
            return;
        }

    }

    class SequencerLooper extends BaseIOIOLooper {

        private Sequencer sequencer_;

        final Sequencer.Clock CLK = Sequencer.Clock.CLK_2M; /* 0.5us periods */
        final int INITIAL = 3000; /* 1500 us * (1 / 0.5microseconds) */
        final int SLICE =  PERIOD / 16; /* Slice duration in 16microseconds period */

        private ArrayList<Sequencer.ChannelCuePwmPosition> channelCues = new ArrayList();

        @Override
        protected void setup() throws ConnectionLostException, InterruptedException {

            synchronized(mProject) {
                try {
                    info("SequencerLooper.setup() Waiting for a project");
                    mProject.wait();
                } catch (Exception e) {
                    info("Waiting on the project failed");
                }
            }

            info("SequencerLooper.setup() Setting config " + mProject.getProject().boardConfig.name);

            ArrayList<Sequencer.ChannelConfigPwmPosition> channelConfigs = new ArrayList();

            for (ServoConfig sc: mProject.getProject().boardConfig.servoConfigs) {

                // Create the channel configs
                channelConfigs.add(
                    new Sequencer.ChannelConfigPwmPosition(CLK, PERIOD, INITIAL, new DigitalOutput.Spec(sc.port))
                );

                // Create the cues
                channelCues.add(new Sequencer.ChannelCuePwmPosition());

            }

            /*
            final Sequencer.ChannelConfigBinary      led1Config   = new Sequencer.ChannelConfigBinary(true, true, new DigitalOutput.Spec(11));
            */

            sequencer_ = ioio_.openSequencer(channelConfigs.toArray(new Sequencer.ChannelConfig[channelConfigs.size()]));

            // Pre-fill.
            sequencer_.waitEventType(Sequencer.Event.Type.STOPPED);
            while (sequencer_.available() > 0) {
                push();
            }

            info("SequencerLooper.start()");

            sequencer_.start();
        }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            push();
        }

        private long lastLog = 0;
        private int currentSlice = 0;


        private void push() throws ConnectionLostException, InterruptedException {

            ArrayList<Integer> slices = mProject.getSlices().get(currentSlice);

            for (int i = 0; i < channelCues.size(); i++)
                channelCues.get(i).pulseWidth = slices.get(i);

            sequencer_.push(channelCues.toArray(new Sequencer.ChannelCuePwmPosition[channelCues.size()]), SLICE /* 20ms */); // Unit value is 16microseconds, 62500 = 1 s

            /*
            led1Cue.value = new Random().nextBoolean();
            */

            long now = SystemClock.elapsedRealtime();
            if (now - lastLog > 500) {
                lastLog = now;
                Log.i(TAG, "#" + currentSlice + " " + slices);
            }

            currentSlice++;

            if (currentSlice == mProject.getSliceCount()) {

                info("********** LAST SLICE");

                sequencer_.stop();

                synchronized (mProject) {
                    info("SequencerLooper.push() Waiting for a new project");
                    mProject.wait();

                    info("SequencerLooper.push() Got a new project: " + mProject.getDuration() + "ms, " + mProject.getSliceCount() + " slices");
                    currentSlice = 0;

                    info("SequencerLooper.push() Wait for sequencer to stop...");
                    sequencer_.waitEventType(Sequencer.Event.Type.STOPPED);

                    info("SequencerLooper.push() Prefilling sequencer...");
                    while (sequencer_.available() > 0) {
                        push();
                    }

                    info("SequencerLooper.push() Starting sequencer");
                    sequencer_.start();
                }
            }

        }
    }

    class RandomLooper extends BaseIOIOLooper {

        PwmOutput pin3;
        PwmOutput pin4;
        PwmOutput pin5;
        PwmOutput pin6;
        PwmOutput pin7;
        PwmOutput pin10;
        DigitalOutput pin11;

        @Override
        protected void setup() throws ConnectionLostException, InterruptedException {
            pin3 = ioio_.openPwmOutput(3, 50);
            pin4 = ioio_.openPwmOutput(4, 50);
            pin5 = ioio_.openPwmOutput(5, 50);
            pin6 = ioio_.openPwmOutput(6, 50);
            pin7 = ioio_.openPwmOutput(7, 50);
            pin10 = ioio_.openPwmOutput(10, 50);
            pin11 = ioio_.openDigitalOutput(11);
        }

        private int getRandom() {
            return new Random().nextInt(2000 - 1000) + 1000;
        }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException {

            final int servo1 = getRandom();
            final int servo2 = getRandom();
            final int servo3 = getRandom();
            final int servo4 = getRandom();
            final int servo5 = getRandom();
            final int servo6 = getRandom();
            final boolean led1 = new Random().nextBoolean();

            pin3.setPulseWidth(servo1);
            pin4.setPulseWidth(servo2);
            pin5.setPulseWidth(servo3);
            pin6.setPulseWidth(servo4);
            pin7.setPulseWidth(servo5);
            pin10.setPulseWidth(servo6);
            pin11.write(led1);

            Thread.sleep(1000);
        }

        @Override
        public void disconnected() {
            info("disconnected");
        }

        @Override
        public void incompatible() {
            info("incompatible");
        }
    }

    protected IOIOLooper createIOIOLooper() {

        return new SequencerLooper();

    }

    @Override
    protected void onStart() {
        super.onStart();
        info("onStart");
    }

    @Override
    protected void onStop() {

        info("onStop");
        mBobServer.stop();

        super.onStop();
    }

}
