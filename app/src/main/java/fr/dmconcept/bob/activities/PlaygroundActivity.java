package fr.dmconcept.bob.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import fr.dmconcept.bob.R;

public class PlaygroundActivity extends Activity {

    public static final String TAG = "activities.PlaygroundActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);

        findViewById(R.id.buttonCave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               buttonCaveClicked();
            }
        });
    }

    private void buttonCaveClicked() {

        Log.i(TAG, "buttonCaveClicked()");

        new AlertDialog.Builder(this)
                .setTitle("This is a playground area")
                .setMessage("PLEASE BE CAVE")
                .setPositiveButton("Okkkkay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

}
