package fr.dmconcept.bob.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import fr.dmconcept.bob.R;
import fr.dmconcept.bob.alljoyn.AllJoynService;
import fr.dmconcept.bob.alljoyn.ChatApplication;
import fr.dmconcept.bob.alljoyn.Observable;
import fr.dmconcept.bob.alljoyn.Observer;

public class AlljoynActivity extends Activity implements Observer {

    public static final String TAG = "activities.AlljoynActivity";

    ChatApplication mChatApplication = null;

    private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
    private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 1;
    private static final int HANDLE_ALLJOYN_ERROR_EVENT = 2;

    private static final String mChannelName = "Bob";

    private Button mStartButton;
    private Button mStopButton;

    private TextView mChannelStatus;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_APPLICATION_QUIT_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
                    finish();
                }
                break;
                case HANDLE_CHANNEL_STATE_CHANGED_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_CHANNEL_STATE_CHANGED_EVENT");
                    updateChannelState();
                }
                break;
                case HANDLE_ALLJOYN_ERROR_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
                    alljoynError();
                }
                break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alljoyn);

        mStartButton = (Button)findViewById(R.id.mStartButton);
        mStartButton.setEnabled(false);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mChatApplication.hostStartChannel();
            }
        });

        mStopButton = (Button)findViewById(R.id.mStopButton);
        mStopButton.setEnabled(false);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mChatApplication.hostStopChannel();
            }
        });

        findViewById(R.id.buttonListChannels).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> channels = mChatApplication.getFoundChannels();

                new AlertDialog.Builder(v.getContext())
                    .setMessage("Found " + channels + " channels")
                    .show();

            }
        });

        mChannelStatus = (TextView) findViewById(R.id.mChannelStatus);

        mChatApplication = (ChatApplication) getApplication();

        /*
         * Call down into the model to get its current state.  Since the model
         * outlives its Activities, this may actually be a lot of state and not
         * just empty.
         */
        updateChannelState();

        /*
         * Now that we're all ready to go, we are ready to accept notifications
         * from other components.
         */
        mChatApplication.addObserver(this);
    }

    public synchronized void update(Observable o, Object arg) {
        Log.i(TAG, "update(" + arg + ")");
        String qualifier = (String)arg;

        if (qualifier.equals(ChatApplication.APPLICATION_QUIT_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
            mHandler.sendMessage(message);
        }

        if (qualifier.equals(ChatApplication.HOST_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }

        if (qualifier.equals(ChatApplication.ALLJOYN_ERROR_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
            mHandler.sendMessage(message);
        }
    }

    private void updateChannelState() {

        AllJoynService.HostChannelState channelState = mChatApplication.hostGetChannelState();
        String name = mChatApplication.hostGetChannelName();

        /*
        boolean haveName = true;
        if (name == null) {
            haveName = false;
            name = "Not set";
        }
        mChannelName.setText(name);
        */

        switch (channelState) {
            case IDLE:
                mChannelStatus.setText("Idle");
                break;
            case NAMED:
                mChannelStatus.setText("Named");
                break;
            case BOUND:
                mChannelStatus.setText("Bound");
                break;
            case ADVERTISED:
                mChannelStatus.setText("Advertised");
                break;
            case CONNECTED:
                mChannelStatus.setText("Connected");
                break;
            default:
                mChannelStatus.setText("Unknown");
                break;
        }

        if (channelState == AllJoynService.HostChannelState.IDLE) {
//            mSetNameButton.setEnabled(true);
//            if (haveName) {
                mStartButton.setEnabled(true);
//            } else {
//                mStartButton.setEnabled(false);
//            }
            mStopButton.setEnabled(false);
        } else {
//            mSetNameButton.setEnabled(false);
            mStartButton.setEnabled(false);
            mStopButton.setEnabled(true);
        }

    }

    private void alljoynError(){
        new AlertDialog.Builder(this)
                .setMessage("alljoynError")
                .show();
    }

}
