package fr.dmconcept.bob.server.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import fr.dmconcept.bob.server.BobServer;
import fr.dmconcept.bob.server.R;
import fr.dmconcept.bob.server.ioio.IoioQueue;
import fr.dmconcept.bob.server.models.ServoConfig;
import fr.dmconcept.bob.server.models.Step;
import fr.dmconcept.bob.server.models.serializers.ProjectStepSerializer;
import fr.dmconcept.bob.server.models.serializers.ServoConfigSerializer;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;
import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends ActionBarActivity implements IOIOLooperProvider, BobServer.BobServerListener {

    private static final String TAG = ".activities.MainActivity";

    private IOIOAndroidApplicationHelper mIoioHelper = new IOIOAndroidApplicationHelper(this, this);
    private IoioQueue mIoioQueue = new IoioQueue();

    private boolean mIoioConnected;

    private TextView mTextIPs;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate()");

        new BobServer(this);
        mIoioHelper.create();

        mTextIPs = (TextView) findViewById(R.id.textLog);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Waiting for you to connect the IOIO board");
        mProgressDialog.setCancelable(true);
        //TODO set buttons?
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
               dialog.dismiss();
                finish();
            }
        });

        setIoioConnected(false);

        ArrayList<String> ips = getIPs();

        if (ips.isEmpty())
            showFatalErrorDialog("No network connection", "This app need a wifi or Ethernet network connection.");

        for (String ip: ips)
            mTextIPs.setText(mTextIPs.getText() + "\n" + ip);

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
        mIoioHelper.start();

        if (new GregorianCalendar().getTimeInMillis() > new GregorianCalendar(2014, 3, 13).getTimeInMillis())
            showFatalErrorDialog("Demo expired", "This app is a demo version and has expired since the 10th march 2014.");

    }

    /**
     * Called when the board is attached
     *
     * @param intent the USB_ACCESSORY_ATTACHED intent
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected void onNewIntent(Intent intent) {

        Log.i(TAG, "onNewIntent(" + intent + ")");
        Bundle extras = intent.getExtras();

        if (!intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)) {
            Log.i(TAG, "onNewIntent() - Not an ACTION_USB_ACCESSORY_ATTACHED action");
            return;
        }

        UsbAccessory accessory = (UsbAccessory) extras.get("accessory");

        if (!accessory.getModel().equals("IOIO")) {
            Log.i(TAG, "onNewIntent() - " + accessory + " don't match the IOIO model");
            return;
        }

        mIoioHelper.restart();

        setIoioConnected(true);

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

                Log.i(TAG, "setup()");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setIoioConnected(true);
                    }
                });

                mIoio = ioio;

                // Wait for sequence to setup the queue
                while (true) {

                    if (mIoioQueue.hasSequence()) {

                        // Setup the board pins
                        for (ServoConfig servoConfig: mIoioQueue.getServoConfigs()) {
                            Log.i(TAG,"setup() - Setting up pin " + servoConfig.port + " to " + servoConfig.frequency);
                            mPins.add(mIoio.openPwmOutput(servoConfig.port, servoConfig.frequency));
                        }

                        Log.i(TAG,"setup() - Done");

                        return;
                    }

                    Log.i(TAG, "setup() - Waiting for a sequence...");

                    Thread.sleep(IDLE_DURATION);

                }

            }

            @Override
            public void loop() throws ConnectionLostException, InterruptedException {

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

                Thread.sleep(SLICE_DURATION);

            }

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

                    float startPercentage   = mIoioQueue.getStep(playStepsCurrentStep    ).getPosition(servo);
                    float endPercentage     = mIoioQueue.getStep(playStepsCurrentStep + 1).getPosition(servo);

                    // The interpolated position
                    float slicePercentage   = (endPercentage - startPercentage) / playStepsSlices;
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
                    playStepsSlices       = 0;
                    playStepsCurrentSlice = 0;

                    playStepsCurrentStep++;

                    if (playStepsCurrentStep == mIoioQueue.getSteps().size() - 1) {

                        Log.i(TAG, "playStep() - Playing sequence done");
                        playStepsCurrentStep = 0;
                        mIoioQueue.clear();

                    }

                }

            }

            @Override
            public void disconnected() {

                Log.i(TAG, "disconnected()");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setIoioConnected(false);
                    }
                });

            }

            @Override
            public void incompatible() {

                Log.i(TAG, "incompatible()");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       showFatalErrorDialog("Incompatible IOIO board", "The connected IOIO board must be updated to at least the version 5 firmware");
                    }
                });

            }

        };

    }

    @Override
    public void onCantStart(IOException exception) {

       showFatalErrorDialog("Network error", "Can't start the server: " + exception.getMessage());

    }

    @Override
    public void onPlayRequest(final String serializedServoConfig, final String serializedSteps) {

        Log.i(TAG, "onPlayRequest(" + serializedServoConfig + ", " + serializedSteps + ")");

        if (!mIoioConnected) {

            Log.i(TAG, "onPlayRequest() - Not connected");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast
                        .makeText(MainActivity.this, "Can't play the sequence while the IOIO board is not connected.", Toast.LENGTH_SHORT)
                        .show();
                }
            });

            return;

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ArrayList<ServoConfig> servoConfigs = ServoConfigSerializer.deserialize(serializedServoConfig);
                ArrayList<Step>        steps        = ProjectStepSerializer.deserialize(serializedSteps);

                mIoioQueue.queueSteps(servoConfigs, steps);

                Toast.makeText(MainActivity.this, "Playing the sequence...", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private ArrayList<String> getIPs() {

        ArrayList<String> ips = new ArrayList<String>();

        try {

            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {

                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());

                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {

                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);

                        if (isIPv4)
                            ips.add(sAddr);
                    }
                }
            }

        } catch (SocketException e) {
            showFatalErrorDialog("Not network connection", "This device has no IP address, connect it to the network.");
        }

        return ips;

    }

    private void setIoioConnected(boolean connected) {

        Log.i(TAG, "setIoioConnected(" + connected + ")");

        mIoioConnected = connected;

        if (connected)
            mProgressDialog.dismiss();
        else
            mProgressDialog.show();

    }

    private void showFatalErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            })
            .show();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        mIoioHelper.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        Log.i(TAG, "onDestroy()");

        //Avoid window leaking
        mProgressDialog.dismiss();

        mIoioHelper.destroy();
        super.onDestroy();
    }


}
