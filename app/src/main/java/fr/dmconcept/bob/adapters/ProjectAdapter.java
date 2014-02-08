package fr.dmconcept.bob.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import fr.dmconcept.bob.models.Project;

public class ProjectAdapter extends ArrayAdapter<Project> {

    Map<Project, String> ids = new HashMap<Project, String>();

    public ProjectAdapter(Context context, Project[] projects) {

        super(context, android.R.layout.simple_list_item_1, projects);

        for (int i  = 0; i < projects.length; i++) {
            Project p = projects[i];
            ids.put(p, p.id);
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Let the adapter create the view
        View view = super.getView(position, convertView, parent);

        Project project = getItem(position);

        String line1 = project.name;
        String line2 = project.steps.length + " steps for a duration of " + project.duration() + " ms";

        ((TextView) view.findViewById(android.R.id.text1)).setText(line1 + " / " + line2);

        return view;
    }
}
