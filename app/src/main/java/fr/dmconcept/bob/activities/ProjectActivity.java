package fr.dmconcept.bob.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;

import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Projects;
import fr.dmconcept.bob.models.Step;

public class ProjectActivity extends Activity {

    // The current project
    private Project mProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project);

        // Get the project ID from the intent
        String projectId = getIntent().getStringExtra(ProjectListActivity.EXTRA_PROJECT_ID);

        this.mProject = Projects.findById(projectId);

        // Set the project name as the activity title
        setTitle(mProject.name);

        createPositionSliders();
        createTimeline();

    }

    /**
     * Create the right quantity of position sliders according to the project servo count
     *
     */
    private void createPositionSliders() {

        // Create the positions sliders according to the project servos count
        LinearLayout startPositions = ((LinearLayout) findViewById(R.id.startPositions));
        LinearLayout endPositions   = ((LinearLayout) findViewById(R.id.endPositions  ));

        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < mProject.getServosCount(); i++) {
            createPositionSlider(inflater, startPositions, i);
            createPositionSlider(inflater, endPositions  , i);
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

    private void createTimeline() {

        LinearLayout timeline = (LinearLayout) findViewById(R.id.timeline);

        // Create the timeline positions
        for (int i = 0; i < mProject.steps.length - 1; i++) {

            Button button = new Button(this);
            button.setText(String.valueOf(i + 1));
            button.setTag(i);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View button) {
                    selectPosition((Button) button);
                }
            });

            timeline.addView(button);
        }

        selectPosition((Button) timeline.getChildAt(0));

    }

    private void selectPosition(Button button) {

        // Get the clicked button position index
        int positionIndex = (Integer) button.getTag();

        LinearLayout timeline = (LinearLayout) button.getParent();

        // Set the default background on all position buttons
        for (int b = 0; b < timeline.getChildCount(); b++)
            timeline.getChildAt(b).setBackgroundResource(android.R.drawable.btn_default);

        // Set the selected position button as active
        button.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

        // Update the position sliders
        updatePositionsSliders(positionIndex);

    }

    private void updatePositionsSliders(int positionIndex) {

        TableLayout startPositions = (TableLayout) findViewById(R.id.startPositions);
        TableLayout endPositions   = (TableLayout) findViewById(R.id.endPositions  );

        Step startStep = mProject.steps[positionIndex    ];
        Step endStep   = mProject.steps[positionIndex + 1];

        for (int i = 0; i < startStep.getServosCount(); i++)
            ((SeekBar) startPositions.findViewWithTag(i)).setProgress(startStep.getServo(i));

        for (int i = 0; i < endStep.getServosCount(); i++)
            ((SeekBar) endPositions.findViewWithTag(i)).setProgress(endStep.getServo(i));

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
