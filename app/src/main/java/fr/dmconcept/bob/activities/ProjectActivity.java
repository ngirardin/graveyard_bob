package fr.dmconcept.bob.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Projects;
import fr.dmconcept.bob.models.Step;

public class ProjectActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project);

        // Get the intent extra
        Bundle extras = getIntent().getExtras();

        ProjectFragment projectFragment = new ProjectFragment();
        projectFragment.setArguments(extras);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, projectFragment)
                .commit();
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The project fragment
     */
    public static class ProjectFragment extends Fragment {

        // The current project
        private Project project;

        public ProjectFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            // Create the fragment
            View rootView = inflater.inflate(R.layout.fragment_project, container, false);

            // Get the project ID from the intent
            String projectId = getArguments().getString(ProjectListActivity.EXTRA_PROJECT_ID);
            this.project = Projects.findById(projectId);

            updateTitle();
            createPositionSliders(rootView, savedInstanceState);
            createTimeline(rootView);

            return rootView;
        }

        private void updateTitle() {
            getActivity().setTitle(project.name);
        }

        private void createTimeline(View view) {

            LinearLayout timeline = (LinearLayout) view.findViewById(R.id.timeline);

            // Create the timeline positions
            for (int i = 0; i < project.steps.length - 1; i++) {
                Button button = new Button(view.getContext());
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
            updatePositionsSliders(positionIndex, button.getRootView());

        }

        private void createPositionSliders(View view, Bundle savedInstanceState) {

            // Create the positions sliders according to the project servos count
            TableLayout startPositions = ((TableLayout) view.findViewById(R.id.startPositions));
            TableLayout endPositions   = ((TableLayout) view.findViewById(R.id.endPositions  ));

            LayoutInflater inflater = getLayoutInflater(savedInstanceState);

            for (int servo = 0; servo < project.getServosCount(); servo++) {
                createPositionSlider(inflater, startPositions, servo);
                createPositionSlider(inflater, endPositions  , servo);
            }

        }

        private void createPositionSlider(LayoutInflater inflater, TableLayout parent, int servoIndex) {

            // Inflate the position slider widget
            TableRow positionSliderRow = (TableRow) inflater.inflate(R.layout.layout_position_sliders, null);

            // Update the servo name text
            ((TextView) positionSliderRow.findViewById(R.id.text)).setText("Servo " + (servoIndex + 1));

            // Set a tag to the slider for easier retrieval
            positionSliderRow.findViewById(R.id.slider).setTag(servoIndex);

            // Append the position slider to the parent view
            parent.addView(positionSliderRow);

        }

        private void updatePositionsSliders(int positionIndex, View root) {

            TableLayout startPositions = (TableLayout) root.findViewById(R.id.startPositions);
            TableLayout endPositions   = (TableLayout) root.findViewById(R.id.endPositions  );

            Step startStep = project.steps[positionIndex    ];
            Step endStep   = project.steps[positionIndex + 1];

            for (int i = 0; i < startStep.getServosCount(); i++)
                    ((SeekBar) startPositions.findViewWithTag(i)).setProgress(startStep.getServo(i));

            for (int i = 0; i < endStep.getServosCount(); i++)
                    ((SeekBar) endPositions.findViewWithTag(i)).setProgress(endStep.getServo(i));

        }

    }

}
