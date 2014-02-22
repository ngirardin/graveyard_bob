package fr.dmconcept.bob.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Projects;

// TODO move to Application instead of Activity
public class ProjectListActivity extends ListActivity {

    public static final String TAG = "bob.activities.ProjctListActivity";

    public final static String EXTRA_PROJECT_ID = "fr.dmconcept.bob.extras.projectId";

    Project[] mProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mProjects = Projects.all();

        setListAdapter(new ArrayAdapter<Project>(this, android.R.layout.simple_list_item_2, android.R.id.text1, mProjects) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                Project project = getItem(position);

                String text = (project.steps.length - 1) + " steps - duration " + (project.duration() / 1000) + " s - servo config: TODO";

                ((TextView) view.findViewById(android.R.id.text1)).setText(project.name);
                ((TextView) view.findViewById(android.R.id.text2)).setText(text);

                return view;
            }

        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Project p = mProjects[position];

        // Start the project details activity
        Intent intent = new Intent(v.getContext(), ProjectActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, p.id);
        startActivity(intent);
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

}
