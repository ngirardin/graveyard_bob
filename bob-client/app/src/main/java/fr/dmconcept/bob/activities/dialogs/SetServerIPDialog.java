package fr.dmconcept.bob.activities.dialogs;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import fr.dmconcept.bob.BobApplication;
import fr.dmconcept.bob.R;
import fr.dmconcept.bob.activities.ProjectListActivity;

import java.util.regex.Pattern;

public class SetServerIPDialog extends ActionBarActivity {

    private static final String TAG = "activities.dialogs.SetServerIPDialog";

    private static final Pattern IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_projectlist_ip);

        findViewById(R.id.buttonConnect)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TextView textIP = (TextView) findViewById(R.id.editTextIP);

                    String ip = textIP.getText().toString();

                    if (ip.length() > 0 && !IP_REGEXP.matcher(ip).matches()) {
                        textIP.setError("Invalid IP address");
                        return;
                    }

                    ((BobApplication) getApplication()).setServerIP(ip);

                    Intent intent = new Intent(SetServerIPDialog.this, ProjectListActivity.class);
                    startActivity(intent);

                }
            });

    }


}
