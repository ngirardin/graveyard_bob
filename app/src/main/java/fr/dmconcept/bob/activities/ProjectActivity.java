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

    // The getDuration EditText
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

        // Register the views event listeners
        registerEvents();

        // Get the project ID from the intent
        int projectId = getIntent().getIntExtra(ProjectListActivity.EXTRA_PROJECT_ID, -1);

        mProject = ProjectsDataSource.findById(projectId);

        // Set the project name as the activity title
        setTitle(mProject.getName());

        fillTimeline();
        createPositionSliders();

        // Select the first step as active
        updateActiveStep(0);

    }

    private void registerEvents() {

        // Click on the "New step" button
        findViewById(R.id.buttonNewStep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create the new step
                mProject.addStep();

                // Update the timeline
                fillTimeline();

                // Select the last period (the last step is the end step)
                updateActiveStep(mProject.getSteps().size() - 2);

            }
        });

    }

    private void fillTimeline() {

        float projectDuration = mProject.getDuration();

        mTimeline.removeAllViews();

        // Add the buttons to the timeline
        for (int i = 0; i < mProject.getSteps().size() - 1; i++) {

            Step step = mProject.getStep(i);

            float durationRatio = (float) step.getDuration() / projectDuration;

            ToggleButton button = new ToggleButton(this);

            // Set the step id as the tag
            button.setTag(i);

            // Dynamically set the weight on the button according to the getDuration
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
            mTimeline.addView(button);

        }

    }

    private void updateActiveStep(Button button) {

        int stepIndex = (Integer) button.getTag();
        updateActiveStep(stepIndex);

    }

    private void updateActiveStep(int stepIndex) {

        Log.i(TAG, "updateActiveStep(" + stepIndex +")");

        int  stepCount = mProject.getSteps().size();
        Step step      = mProject.getStep(stepIndex);

        // Set the default background on all position buttons
        for (int i = 0; i < stepCount - 1; i++) {

            ToggleButton button = (ToggleButton) mTimeline.findViewWithTag(i);
            assert button != null;

            if (i == stepIndex)
                // Set the current button as selected
                button.setChecked(true);
            else
                // Restore the default background
                button.setChecked(false);
        }

        // Update the getDuration editText
        String durationString = String.valueOf(step.getDuration());
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

        for (int i = 0; i < mProject.getBoardConfig().getServoCount(); i++) {
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

        int servoCount = mProject.getBoardConfig().getServoCount();

        Step startStep = mProject.getStep(positionIndex    );
        Step endStep   = mProject.getStep(positionIndex + 1);

        // Update the start position seek bar
        for (int i = 0; i < servoCount; i++) {
            View position = mStartPositions.findViewWithTag(i);
            assert position != null;
            ((SeekBar) position).setProgress(startStep.getPosition(i));
        }

        // Update the end position seek bar
        for (int i = 0; i < servoCount; i++) {
            View position = mEndPositions.findViewWithTag(i);
            assert position != null;
            ((SeekBar) position).setProgress(endStep.getPosition(i));
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

        switch (item.getItemId()) {

            case R.id.action_settings:
                return true;

            case R.id.action_boardConfig:
                menuBoardConfigClicked();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Show the board config activity
     */
    private void menuBoardConfigClicked() {


    }

}
