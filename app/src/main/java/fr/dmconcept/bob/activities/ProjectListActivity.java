package fr.dmconcept.bob.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import fr.dmconcept.bob.R;
import fr.dmconcept.bob.adapters.ProjectAdapter;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Projects;

public class ProjectListActivity extends ActionBarActivity {

    public final static String EXTRA_PROJECT_ID = "fr.dmconcept.bob.extras.projectId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment())
                .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project_list, menu);
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
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_project_list, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {

            Project[] projects = Projects.all();

            // Populate the list with the projects
            ListView projectList = (ListView) view.findViewById(R.id.projectList);
            projectList.setAdapter(new ProjectAdapter(view.getContext(), projects));

            projectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    ProjectAdapter adapter = (ProjectAdapter) parent.getAdapter();
                    Project project = adapter.getItem(position);

                    // Start the project details activity
                    Intent intent = new Intent(view.getContext(), ProjectActivity.class);
                    intent.putExtra(EXTRA_PROJECT_ID, project.id);
                    startActivity(intent);

                }
            });

            super.onViewCreated(view, savedInstanceState);

        }

    }

}
