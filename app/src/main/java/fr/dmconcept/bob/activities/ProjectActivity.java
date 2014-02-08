package fr.dmconcept.bob.activities;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.TextView;

import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Projects;

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

            updateProjectName(rootView);
            createTimeline(rootView);

            return rootView;
        }

        private void updateProjectName(View view) {
            ((TextView) view.findViewById(R.id.projectName))
                .setText(project.name);
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

                        // Set the active background on the button
                        LinearLayout timeline  = (LinearLayout) button.getParent();

                        for (int b = 0; b < timeline.getChildCount(); b++)
                            timeline.getChildAt(b).setBackgroundResource(android.R.drawable.btn_default);

                        button.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

                        // Get the button ID
                        int id = (Integer) button.getTag();

                        AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
                        d.setMessage("Button " + id);
                        d.show();

                    }
                });

                timeline.addView(button);
            }

        }

    }

}
