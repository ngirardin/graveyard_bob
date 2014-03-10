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
import fr.dmconcept.bob.BobApplication;
import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.Project;

import java.util.ArrayList;

public class ProjectListActivity extends ListActivity {

    public static final String TAG = "activities.ProjctListActivity";

    ArrayList<Project> mProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Get the project list from the DB
        mProjects = ((BobApplication) getApplication()).getProjectsDao().findAll();

        setListAdapter(new ArrayAdapter<Project>(this, android.R.layout.simple_list_item_2, android.R.id.text1, mProjects) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                Project project = getItem(position);

                assert view != null;
                ((TextView) view.findViewById(android.R.id.text1)).setText(project.getName());
                ((TextView) view.findViewById(android.R.id.text2)).setText(project.getBoardConfig().getName());

                return view;
            }

        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Project p = mProjects.get(position);

        // Start the project details activity
        Intent intent = new Intent(v.getContext(), ProjectActivity.class);
        intent.putExtra(ProjectActivity.EXTRA_PROJECT_ID, p.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_boardConfig:
                menuBoardConfigClicked();
                return true;

            case R.id.action_newProject:
                menuNewProjectClicked();
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

    /**
     * Show the new project activity
     */
    private void menuNewProjectClicked() {
        startActivity(new Intent(this, NewProjectActivity.class));
    }

}
