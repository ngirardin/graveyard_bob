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
import fr.dmconcept.bob.server.BobLooperProvider;
import fr.dmconcept.bob.server.BobServer;
import fr.dmconcept.bob.server.R;
import fr.dmconcept.bob.server.models.Project;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;
import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends ActionBarActivity implements BobServer.BobServerListener {

    private static final String TAG = "BobServer-----------------------------";

    private BobLooperProvider mBobLooperProvider;
    private IOIOAndroidApplicationHelper mIoioHelper;

    private boolean mIoioConnected;

    private TextView mTextIPs;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "MainActivity.onCreate() thread=" + Thread.currentThread().getId());

        new BobServer(this);

        mTextIPs = (TextView) findViewById(R.id.textLog);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Connecting to IOIO board...");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
           dialog.dismiss();
            finish();
            }
        });
        setIoioConnected(false);

        mBobLooperProvider = new BobLooperProvider();
        mIoioHelper        = new IOIOAndroidApplicationHelper(this, mBobLooperProvider);
        mIoioHelper.create();
        mIoioHelper.start();

        ArrayList<String> ips = getIPs();

        if (ips.isEmpty())
            showFatalErrorDialog("No network connection", "This app need a wifi or Ethernet network connection.");

        for (String ip: ips)
            mTextIPs.setText(mTextIPs.getText() + "\n" + ip);

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
    public void onCantStart(IOException exception) {

       showFatalErrorDialog("Network error", "Can't start the server: " + exception.getMessage());

    }

    @Override
    public void onPlayRequest(final Project project) {

        Log.i(TAG, "MainActivity.onPlayRequest(" + project.id + ") thread=" + Thread.currentThread().getId());

        if (!mIoioConnected) {

            Log.i(TAG, "MainActivity.onPlayRequest() - Not connected");

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
                Toast.makeText(MainActivity.this, "Playing project " + project.name + "...", Toast.LENGTH_SHORT).show();
            }
        });

        // Reset the IOIO so we can setup the ports again
        Log.i(TAG, "MainActivity.onPlayRequest() Restarting the board");
        mIoioHelper.restart();

        mBobLooperProvider.play(project);

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
