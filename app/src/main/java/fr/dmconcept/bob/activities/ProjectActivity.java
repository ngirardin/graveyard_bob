package fr.dmconcept.bob.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import fr.dmconcept.bob.BobApplication;
import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Step;
import fr.dmconcept.bob.models.dao.ProjectDao;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.ArrayList;

public class ProjectActivity extends IOIOActivity {

    private static final String TAG = "activities.ProjectListActivity";

    // The intent extra key for the project id
    public final static String EXTRA_PROJECT_ID = "fr.dmconcept.bob.extras.projectId";

    // The default step duration for new steps in ms
    private static final int DEFAULT_STEP_DURATION = 2000;

    // The minimum step duration in ms
    private static final int MIN_STEP_DURATION = 100;

    // The project DAO
    private ProjectDao mProjectDao;

    // The current project
    private Project mProject;

    // The active step index
    private int mStepIndex;

    // The positions consumed by the IOIO Looper
    protected ArrayList<Integer> mIoioPositions;

    // The timeline
    private LinearLayout mTimeline;

    // The getDuration EditText
    private EditText mDurationEditText;

    // The start and end positions layout
    private LinearLayout mPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate()");

        setContentView(R.layout.activity_project);

        // Get the project from the DB according to the intent extra id
        long projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        mProjectDao = ((BobApplication) getApplication()).getProjectsDao();
        mProject    = mProjectDao.findById(projectId);

        mTimeline         = (LinearLayout) findViewById(R.id.timeline        );
        mDurationEditText = (EditText    ) findViewById(R.id.editTextDuration);
        mPositions        = (LinearLayout) findViewById(R.id.positions       );

        // Register the views event listeners
        registerViewListeners();

        // Set the activity title as the project name
        setTitle(mProject.getName());

        // Create the positions
        createPositions();

        // Fill the timeline with the steps
        updateTimeline();

    }

    private void registerViewListeners() {

        // Click on the "New step" button
        findViewById(R.id.buttonNewStep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create the new step
                mProject.addStep(DEFAULT_STEP_DURATION);

                // Select the last period (the last step is the end step)
                mStepIndex = mProject.getSteps().size() - 2;

                // Update the timeline
                updateTimeline();

            }
        });

        // Duration changed
        ((EditText) findViewById(R.id.editTextDuration)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                int newDuration = Integer.valueOf(v.getText().toString());

                if (newDuration == MIN_STEP_DURATION) {
                    v.setError("The step can't last less than " + MIN_STEP_DURATION + " ms");
                } else {
                    // Save the new duration
                    mProjectDao.saveDuration(mProject, mStepIndex, newDuration);

                    // Redraw the timeline to reflect the new step duration
                    updateTimeline();
                }

                return true;

            }

        });

        // Click on the start or end buttons
        findViewById(R.id.buttonStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIOIOPosition(0);
            }
        });

        findViewById(R.id.buttonEnd).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setIOIOPosition(1);
            }
        });

    }

    /**
     * Delete all the timeline steps and create them with a width
     * matching their relative length
     */
    private void updateTimeline() {

        Log.i(TAG, "updateTimeline()");

        float projectDuration = mProject.getDuration();

        mTimeline.removeAllViews();

        // Add the buttons to the timeline
        for (int i = 0; i < mProject.getSteps().size() - 1; i++) {

            Step step = mProject.getStep(i);

            float durationRatio = (float) step.getDuration() / projectDuration;

            ToggleButton button = new ToggleButton(this);

            // Dynamically set the weight on the button according to the getDuration
            button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, durationRatio
            ));

            // Set the step id as the tag
            button.setTag(i);

            button.setTextOff(String.valueOf(i + 1));
            button.setTextOn(button.getTextOff());

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View button) {
                    mStepIndex = Integer.valueOf(button.getTag().toString());
                    updateActiveStep();
                }
            });

            // Add the button before the "new step" button
            mTimeline.addView(button);

        }

        updateActiveStep();
    }


    /**
     * Enable the step button and update the position to the step value
     */
    private void updateActiveStep() {

        Log.i(TAG, "updateActiveStep()");

        int  stepCount = mProject.getSteps().size();
        Step step      = mProject.getStep(mStepIndex);

        // Set the default background on all position buttons
        for (int i = 0; i < stepCount - 1; i++) {

            ToggleButton button = (ToggleButton) mTimeline.getChildAt(i);

            // Set the current button as selected
            button.setChecked((i == mStepIndex));
        }

        // Update the duration editText and position the cursor at the end
        String durationString = String.valueOf(step.getDuration());
        mDurationEditText.setText(durationString);
        mDurationEditText.setSelection(durationString.length());

        // Update the position sliders
        updatePositions();

    }


    private class PositionListener {

        int stepIndex;
        int positionIndex;

        PositionListener(int step, int position) {
            stepIndex     = step;
            positionIndex = position;
        }

        void savePosition(int newValue) {
            mProjectDao.savePosition(mProject, stepIndex, positionIndex, newValue);
        }

    }

    private class PositionTextEditorActionListener extends PositionListener implements TextView.OnEditorActionListener {

        SeekBar mSeekbar;

        PositionTextEditorActionListener(SeekBar seekbar, int step, int position) {
            super(step, position);
            mSeekbar = seekbar;
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            int newValue = Integer.parseInt(v.getText().toString());

            if (newValue > 100) {
                v.setError("Position must be between 0 and 100");
            } else {
                mSeekbar.setProgress(newValue);
                savePosition(newValue);
            }

            return true;
        }

    }

    private class PositionSeekbarChangeListener extends PositionListener implements SeekBar.OnSeekBarChangeListener {

        EditText editText;

        PositionSeekbarChangeListener(EditText editText, int step, int position) {
            super(step, position);
            this.editText = editText;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (!fromUser)
                return;

            // Update the percentage text
            editText.setText(String.valueOf(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            savePosition(seekBar.getProgress());
        }
    }

    /**
     * Create the positions
     */
    private void createPositions() {

        for (int i = 0; i < mProject.getBoardConfig().getServoCount(); i++) {

            // Inflate the position layout
            LinearLayout positionLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_project_positions, null);

            EditText editPercentageLeft  = (EditText) positionLayout.findViewById(R.id.editPercentageLeft);
            SeekBar  seekbarLeft         = (SeekBar ) positionLayout.findViewById(R.id.seekbarLeft );
            SeekBar  seekbarRight        = (SeekBar ) positionLayout.findViewById(R.id.seekbarRight);
            EditText editPercentageRight = (EditText) positionLayout.findViewById(R.id.editPercentageRight);

            // Set the listeners on the widgets
            editPercentageLeft .setOnEditorActionListener(new PositionTextEditorActionListener(seekbarLeft, mStepIndex, i));
            seekbarLeft        .setOnSeekBarChangeListener(new PositionSeekbarChangeListener   (editPercentageLeft , mStepIndex, i));
            seekbarRight       .setOnSeekBarChangeListener(new PositionSeekbarChangeListener   (editPercentageRight, mStepIndex + 1, i));
            editPercentageRight.setOnEditorActionListener (new PositionTextEditorActionListener(seekbarRight       , mStepIndex + 1, i));

            // Set the position index text
            ((TextView) positionLayout.findViewById(R.id.textPositionIndex)).setText("Servo " + (i + 1));

            // Append the position slider to the parent view
            mPositions.addView(positionLayout);
        }

    }


    private void updatePositions() {

        ArrayList<Integer> startPositions = mProject.getStep(mStepIndex    ).getPositions();
        ArrayList<Integer> endPositions   = mProject.getStep(mStepIndex + 1).getPositions();

        for (int i = 0; i < mProject.getBoardConfig().getServoCount() ; i++) {

            Integer startPosition = startPositions.get(i);
            Integer endPosition   = endPositions  .get(i);

            LinearLayout positionLayout = (LinearLayout) mPositions.getChildAt(i);

            ((EditText) positionLayout.findViewById(R.id.editPercentageLeft )).setText    (startPosition.toString());
            ((SeekBar ) positionLayout.findViewById(R.id.seekbarLeft        )).setProgress(startPosition           );

            ((SeekBar ) positionLayout.findViewById(R.id.seekbarRight       )).setProgress(endPosition             );
            ((EditText) positionLayout.findViewById(R.id.editPercentageRight)).setText    (endPosition.toString()  );

        }

    }

    ProjectActivityIOIOLooper mIoioLooper;

    @Override
    protected IOIOLooper createIOIOLooper() {
        mIoioLooper =  new ProjectActivityIOIOLooper(this, mProject.getBoardConfig());
        return mIoioLooper;
    }

    private void setIOIOPosition(int stepOffset) {
        mIoioPositions = new ArrayList<Integer>(mProject.getStep(mStepIndex + stepOffset).getPositions());
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

        Intent intent = new Intent(this, BoardConfigActivity.class);
        startActivity(intent);

    }

}
