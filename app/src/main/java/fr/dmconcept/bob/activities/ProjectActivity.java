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
import android.widget.TableLayout;

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
            createTimeline(rootView);
            createPositionSliders(rootView, savedInstanceState);

            return rootView;
        }

        private void updateTitle() {
            getActivity().setTitle(project.name);
        }

        private void createTimeline(View view) {

            LinearLayout timeline = (LinearLayout) view.findViewById(R.id.timeline);

            // Set the listener on the "new" button
            view
                .findViewById(R.id.newStep)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
                    d.setMessage("new step");
                    d.show();
                    }
                });

            // Create the timeline positions
            for (int i = 0; i < project.steps.length; i++) {

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

            // Select the first position
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

            // Update the position sliders positions
            Step step = project.steps[positionIndex];

        }

        private void createPositionSliders(View view, Bundle savedInstanceState) {

            // Create the positions sliders according to the project servos count
            TableLayout startPositions = ((TableLayout) view.findViewById(R.id.startPositions));
            TableLayout endPositions   = ((TableLayout) view.findViewById(R.id.endPositions  ));

            LayoutInflater inflater = getLayoutInflater(savedInstanceState);

            for (int i = 0; i < project.getServosCount(); i++) {
                inflater.inflate(R.layout.layout_position_sliders, startPositions).setTag(i);
                inflater.inflate(R.layout.layout_position_sliders, endPositions  ).setTag(i);
            }

        }

    }

}
