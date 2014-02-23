package fr.dmconcept.bob.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import fr.dmconcept.bob.R;
import fr.dmconcept.bob.dao.ProjectsDataSource;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Step;

public class ProjectActivity extends Activity {

    private static final String TAG = "activities.ProjectListActivity";

    // The current project
    private Project mProject;

    // The timeline
    private LinearLayout mTimeline;

    // The duration EditText
    private EditText mDurationEditText;

    // The start and end positions linear layout
    private LinearLayout mStartPositions;
    private LinearLayout mEndPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate()");

        setContentView(R.layout.activity_project);

        mTimeline         = (LinearLayout) findViewById(R.id.timeline        );
        mDurationEditText = (EditText    ) findViewById(R.id.editTextDuration);
        mStartPositions   = (LinearLayout) findViewById(R.id.startPositions  );
        mEndPositions     = (LinearLayout) findViewById(R.id.endPositions    );

        // Get the project ID from the intent
        String projectId = getIntent().getStringExtra(ProjectListActivity.EXTRA_PROJECT_ID);

        mProject = ProjectsDataSource.findById(projectId);

        // Set the project name as the activity title
        setTitle(mProject.name);

        fillTimeline();
        createPositionSliders();

        // Select the first step as active
        updateActiveStep(0);

    }

    private void fillTimeline() {

        float projectDuration = mProject.duration();

        // Add the buttons to the timeline
        for (int i = 0; i < mProject.steps.length - 1; i++) {

            Step step = mProject.steps[i];

            float durationRatio = (float) step.duration / projectDuration;

            ToggleButton button = new ToggleButton(this);

            // Set the step id as the tag
            button.setTag(i);

            // Dynamically set the weight on the button according to the duration
            button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, durationRatio
            ));

            button.setTextOff(String.valueOf(i + 1));
            button.setTextOn(button.getTextOff());

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View button) {
                    updateActiveStep((Button) button);
                }
            });

            // Add the button before the "new step" button
            mTimeline.addView(button, mTimeline.getChildCount() - 1);

        }

    }

    private void updateActiveStep(Button button) {

        int stepIndex = (Integer) button.getTag();
        updateActiveStep(stepIndex);

    }

    private void updateActiveStep(int stepIndex) {

        // Set the default background on all position buttons
        for (int i = 0; i < mProject.steps.length - 1; i++) {

            ToggleButton button = (ToggleButton) mTimeline.findViewWithTag(i);
            assert button != null;

            if (i == stepIndex)
                // Set the current button as selected
                button.setChecked(true);
            else
                // Restore the default background
                button.setChecked(false);
        }

        Step step = mProject.steps[stepIndex];

        // Update the duration editText
        String durationString = String.valueOf(step.duration);
        mDurationEditText.setText(durationString);

        //Set the cursor at the end of the edit text
        mDurationEditText.setSelection(durationString.length());

        // Update the position sliders
        updatePositionSliders(stepIndex);

    }

    /**
     * Create the position sliders according to the project servo count
     *
     */
    private void createPositionSliders() {

        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < mProject.getServosCount(); i++) {
            createPositionSlider(inflater, mStartPositions, i);
            createPositionSlider(inflater, mEndPositions, i);
        }

    }

    /**
     * Create a position slider
     *
     * @param inflater the layout inflater
     * @param positionLayout the position slider table linearLayout
     * @param i the servo index
     */
    private void createPositionSlider(LayoutInflater inflater, LinearLayout positionLayout, int i) {

        // Inflate the position slider widget
        LinearLayout positionSliderRow = (LinearLayout) inflater.inflate(R.layout.layout_position_sliders, null);

        // Update the servo name text
        assert positionSliderRow != null;
        ((TextView) positionSliderRow.findViewById(R.id.text)).setText("Servo " + (i + 1));

        // Set a tag to the slider for easier retrieval
        positionSliderRow.findViewById(R.id.slider).setTag(i);

        // Append the position slider to the parent view
        positionLayout.addView(positionSliderRow);

    }


    private void updatePositionSliders(int positionIndex) {

        Step startStep = mProject.steps[positionIndex    ];
        Step endStep   = mProject.steps[positionIndex + 1];

        // Update the start position seek bar
        for (int i = 0; i < startStep.getServosCount(); i++) {
            View position = mStartPositions.findViewWithTag(i);
            assert position != null;
            ((SeekBar) position).setProgress(startStep.getServo(i));
        }

        // Update the end position seek bar
        for (int i = 0; i < endStep.getServosCount(); i++) {
            View position = mEndPositions.findViewWithTag(i);
            assert position != null;
            ((SeekBar) position).setProgress(endStep.getServo(i));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return (id ==  R.id.action_settings) || super.onOptionsItemSelected(item);
    }

}
