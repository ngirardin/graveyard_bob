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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import fr.dmconcept.bob.server.BobServer;
import fr.dmconcept.bob.server.R;
import fr.dmconcept.bob.server.ioio.ProjectLock;
import fr.dmconcept.bob.server.models.Project;
import fr.dmconcept.bob.server.models.ServoConfig;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Sequencer;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainIOIOActivity extends IOIOActivity {

    private static final String TAG = "BobServer ------------------- ";

    private TextView textViewIP;
    private TextView textViewLog;

    private ArrayList<String> ips;
    private BobServer mBobServer;

    private final ProjectLock mProject = new ProjectLock();

    public static final int PERIOD  = 20000 /* microseconds */; /* 50hz = 0.02s = 20ms = 20.000us */
    public static final int MIN     = 1000 * 2 /* periods */  ; /* 1000us * 0.5us periods         */
    public static final int MAX     = 2000 * 2 /* periods */  ; /* 2000us * 0.5us periods         */
    public static final int PERCENT = new Float((MAX - MIN) / 100f).intValue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_ioio_activity);

        textViewIP  = (TextView) findViewById(R.id.textViewIP );
        textViewLog = (TextView) findViewById(R.id.textViewLog);

        info("onCreate()");

        findViewById(R.id.buttonQuit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get the device IP addresses
        ips = getIPs();

        // Display an error message and exit the app if the device has no IP address
        if (ips.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(MainIOIOActivity.this)
                            .setTitle("No network connection")
                            .setMessage("The app needs a Wifi or Ethernet network connection")
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

        mBobServer = new BobServer(new BobServer.BobServerListener() {

            @Override
            public void onServerStarted() {

                // Display the IP addresses
                textViewIP.setText("");

                for (String ip: ips)
                    textViewIP.setText(textViewIP.getText() + ip + "\n");

            }

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

        //TODO better newIntent handling
        createIOIOLooper();

    }

    protected IOIOLooper createIOIOLooper() {
        return new SequencerLooper();
    }

    class SequencerLooper extends BaseIOIOLooper {

        private Sequencer sequencer_;

        final Sequencer.Clock CLK = Sequencer.Clock.CLK_2M; /* 0.5us periods */
        final int INITIAL = 3000; /* 1500 us * (1 / 0.5microseconds) */
        final int SLICE =  PERIOD / 16; /* Slice duration in 16microseconds period */

        private ArrayList<Sequencer.ChannelCuePwmPosition> channelCues = new ArrayList<>();

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

            ArrayList<Sequencer.ChannelConfigPwmPosition> channelConfigs = new ArrayList<>();

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

                sequencer_.stop();

                synchronized (mProject) {
                    info("SequencerLooper.push() Sequence done, waiting for a new project");
                    mProject.wait();

                    info("SequencerLooper.push() Got a new project: " + mProject.getDuration() + "ms, " + mProject.getSliceCount() + " slices");
                    currentSlice = 0;

                    info("SequencerLooper.push() Wait for sequencer to stop...");
                    sequencer_.waitEventType(Sequencer.Event.Type.STOPPED);

                    info("SequencerLooper.push() Prefilling the sequencer...");
                    while (sequencer_.available() > 0) {
                        push();
                    }

                    info("SequencerLooper.push() Starting the sequencer");
                    sequencer_.start();
                }
            }

        }
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

    private ArrayList<String> getIPs() {

        ArrayList<String> ips = new ArrayList<>();

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
            // Do notihgn
        }

        return ips;

    }

}
