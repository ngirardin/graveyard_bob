package fr.dmconcept.bob.client.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import fr.dmconcept.bob.client.BobApplication;
import fr.dmconcept.bob.client.R;
import fr.dmconcept.bob.client.models.BoardConfig;
import fr.dmconcept.bob.client.models.ServoConfig;
import scala.collection.immutable.Vector;
import scala.runtime.AbstractFunction1;

public class BoardConfigActivity extends Activity {

    private static final String TAG = "activities.BoardConfigActivity";

    private Vector<BoardConfig> mBoardConfigs;

    private RadioGroup mBoardConfigRadioGroup;

    private LinearLayout mBoardConfigDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_boardconfig);

        mBoardConfigRadioGroup = (RadioGroup  ) findViewById(R.id.boardConfigRadioGroup);
        mBoardConfigDetails    = (LinearLayout) findViewById(R.id.boardConfigDetails);

        mBoardConfigs = ((BobApplication) getApplication()).boardConfigDao().findAll();

        createBoardConfigRadios();

    }

    /**
     * Create the board config radios
     *
     */
    private void createBoardConfigRadios() {

        for (int i = 0; i < mBoardConfigs.size(); i++) {

            BoardConfig boardConfig = mBoardConfigs.apply(i);

            RadioButton radio = new RadioButton(this);
            radio.setId(i);
            radio.setText(boardConfig.name());
            radio.setOnClickListener(v -> showBoardConfig(mBoardConfigRadioGroup.getCheckedRadioButtonId()));

            mBoardConfigRadioGroup.addView(radio);
        }

        // Check the first board config radio
        mBoardConfigRadioGroup.check(0);

        showBoardConfig(0);

    }

    /**
     * Show the board config details for the given board config
     *
     * @param i the board config index
     */
    private void showBoardConfig(int i) {

        Log.i(TAG, "showBoardConfig(" + i + ")");

        BoardConfig boardConfig = mBoardConfigs.apply(i);

        // Clear the board config layout
        mBoardConfigDetails.removeAllViews();

        // Create the header
        // TODO don't recreate at each change
        LinearLayout headerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_boardconfig_details, mBoardConfigDetails);
        ((TextView) headerLayout.findViewById(R.id.textPort )).setText("Port #"      );
        ((TextView) headerLayout.findViewById(R.id.textServo)).setText("Servo #");
        ((TextView) headerLayout.findViewById(R.id.textStart)).setText("Start timing");
        ((TextView) headerLayout.findViewById(R.id.textEnd  )).setText("End timing"  );

        boardConfig.servoConfigs().foreach(new AbstractFunction1<ServoConfig, String>() {

            @Override
            public String apply(ServoConfig servoConfig) {

                // Inflate the board config details layout
                LinearLayout detailsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_boardconfig_details, null);

                ((TextView) detailsLayout.findViewById(R.id.textPort)).setText(String.valueOf(servoConfig.port()));
                ((TextView) detailsLayout.findViewById(R.id.textServo)).setText(String.valueOf(servoConfig.timings()._1()));
                ((TextView) detailsLayout.findViewById(R.id.textStart)).setText(String.valueOf(servoConfig.timings()._2()));

                mBoardConfigDetails.addView(detailsLayout);

                return "TODO";
            }

        });

    }

}
