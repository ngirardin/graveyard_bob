package fr.dmconcept.bob.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import fr.dmconcept.bob.BobApplication;
import fr.dmconcept.bob.BuildConfig;
import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.Project;

import java.util.ArrayList;

// TODO move to Application instead of Activity
public class ProjectListActivity extends ListActivity {

    public static final String TAG = "bob.activities.ProjctListActivity";

    public final static String EXTRA_PROJECT_ID = "fr.dmconcept.bob.extras.projectId";

    ArrayList<Project> mProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        checkExpirationDate();

        // Get the project list from the DB
        mProjects = ((BobApplication) getApplication()).getProjectsDao().findAll();

        setListAdapter(new ArrayAdapter<Project>(this, android.R.layout.simple_list_item_2, android.R.id.text1, mProjects) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                Project project = getItem(position);

                int    stepCount   = project.getSteps().size() - 1;
                int    duration    = project.getDuration() / 1000;
                String config      = project.getBoardConfig().getName();

                String line1 = project.getName();
                String line2 = stepCount + " steps - " + duration + " seconds - servo config: " + config;

                assert view != null;
                ((TextView) view.findViewById(android.R.id.text1)).setText(line1);
                ((TextView) view.findViewById(android.R.id.text2)).setText(line2);

                return view;
            }

        });

    }

    private void checkExpirationDate(){
        if (!BuildConfig.DEBUG && System.currentTimeMillis() > 1394200800) { // expires on 07/03/2014 0:00:00
            new AlertDialog.Builder(this)
                    .setMessage("Version de démo expirée")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ProjectListActivity.this.finish();
                        }
                    })
                    .create()
                    .show();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Project p = mProjects.get(position);

        // Start the project details activity
        Intent intent = new Intent(v.getContext(), ProjectActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, p.getId());
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

        switch (item.getItemId()) {

            case R.id.action_newProject:
                menuNewProjectClicked();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void menuNewProjectClicked() {

        // Start the new project details activity
        startActivity(new Intent(this, NewProjectActivity.class));
    }

}
