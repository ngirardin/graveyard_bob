package fr.dmconcept.bob.client.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Pattern;

import fr.dmconcept.bob.client.BobApplication;
import fr.dmconcept.bob.client.R;

public class ServerIPSelectionActivity extends ActionBarActivity {

    private static final String TAG = "activities.activities.ServerIPSelectionActivity";

    private static final Pattern IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    SharedPreferences preferences;

    private TextView mEditTextIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_projectlist_ip);

        mEditTextIP = (TextView) findViewById(R.id.editTextIP);

        // Update the IP edit text with the value read from the preferences, if any
        if (preferences.contains(BobApplication.PREFERENCES_SERVER_IP)) {

            String ip = preferences.getString(BobApplication.PREFERENCES_SERVER_IP, "");
            mEditTextIP.setText(ip);

            Log.i(TAG, "onCreate() - IP stored in preference: " + ip);

        } else {
            Log.i(TAG, "onCreate() - No IP stored in preferences");
        }

        findViewById(R.id.buttonConnect)

            .setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    String ip = mEditTextIP.getText().toString();

                    // Check that the IP is not empty and a valid ip
                    if (ip.length() > 0 && !IP_REGEXP.matcher(ip).matches()) {
                        mEditTextIP.setError("Invalid IP address");
                        Log.i(TAG, "onClickListener - Invalid IP " + ip);
                        return;
                    }

                    // Store the IP address in the preferences
                    preferences.edit()
                            .putString(BobApplication.PREFERENCES_SERVER_IP, ip)
                            .apply();

                    Log.i(TAG, "clickListener - IP updated to " + ip);

                    // Start the project list activity
                    Intent intent = new Intent(ServerIPSelectionActivity.this, ProjectListActivity.class);
                    startActivity(intent);

                }
            });

    }

}
