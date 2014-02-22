package fr.dmconcept.bob.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Projects;
import fr.dmconcept.bob.models.Step;

public class ProjectActivity extends Activity {

    public static final String TAG = "activities.ProjectListActivity";

    // The current project
    private Project mProject;

    // The timeline
    LinearLayout mTimeline;

    // The duration EditText
    EditText mDurationEditText;

    // The start and end positions linear layout
    LinearLayout mStartPositions;
    LinearLayout mEndPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project);

        mTimeline         = (LinearLayout) findViewById(R.id.timeline        );
        mDurationEditText = (EditText    ) findViewById(R.id.editTextDuration);
        mStartPositions   = (LinearLayout) findViewById(R.id.startPositions  );
        mEndPositions     = (LinearLayout) findViewById(R.id.endPositions    );

        // Get the project ID from the intent
        String projectId = getIntent().getStringExtra(ProjectListActivity.EXTRA_PROJECT_ID);

        mProject = Projects.findById(projectId);

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

            Button button = new Button(this);

            // Dynamically set the weight on the button according to the duration
            button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, durationRatio
            ));

            // Set the step id as the tag
            button.setTag(i);

            button.setText(String.valueOf(i + 1));

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View button) {
                    updateActiveStep((Button) button);
                }
            });

            mTimeline.addView(button);
        }

    }

    private void updateActiveStep(Button button) {

        int stepIndex = (Integer) button.getTag();
        updateActiveStep(stepIndex);

    }

    private void updateActiveStep(int stepIndex) {

        // Set the default background on all position buttons
        for (int i = 0; i < mProject.steps.length - 1; i++) {

            View button = mTimeline.getChildAt(i);

            if (i == stepIndex)
                // Set the current button as selected
                button.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            else
                // Restore the default background
                button.setBackgroundResource(android.R.drawable.btn_default);
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
        ((TextView) positionSliderRow.findViewById(R.id.text)).setText("Servo " + (i + 1));

        // Set a tag to the slider for easier retrieval
        positionSliderRow.findViewById(R.id.slider).setTag(i);

        // Append the position slider to the parent view
        positionLayout.addView(positionSliderRow);

    }


    private void updatePositionSliders(int positionIndex) {

        Step startStep = mProject.steps[positionIndex    ];
        Step endStep   = mProject.steps[positionIndex + 1];

        for (int i = 0; i < startStep.getServosCount(); i++)
            ((SeekBar) mStartPositions.findViewWithTag(i)).setProgress(startStep.getServo(i));

        for (int i = 0; i < endStep.getServosCount(); i++)
            ((SeekBar) mEndPositions.findViewWithTag(i)).setProgress(endStep.getServo(i));

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
