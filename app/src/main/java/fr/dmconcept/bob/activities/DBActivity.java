package fr.dmconcept.bob.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import fr.dmconcept.bob.R;

public class DBActivity extends Activity {

    public static final String TAG = "activities.DBActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        findViewById(R.id.buttonCave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonCaveClicked();
            }
        });

    }

    private void buttonCaveClicked() {

        new AlertDialog.Builder(this)
            .setMessage("CAAAAAVE")
            .show();

        Log.i(TAG, "buttonCaveClicked() - Pouce dans le CUL");

    }

}
