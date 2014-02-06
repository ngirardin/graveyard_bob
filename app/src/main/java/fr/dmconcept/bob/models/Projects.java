package fr.dmconcept.bob.models;

import java.util.Arrays;

public class Projects {

    private static final Project[] projects = new Project[] {
        new Project("First project" ),
        new Project("Second project"),
        new Project("Third project" ),
        new Project("Fourth project")
    };

    public static Project[] all() {
        return projects;
    }

    public static Project findById(String id) {

        for (int i = 0; i < projects.length; i++) {

            Project p = projects[i];

            if (p.id.equals(id))
                return p;

        }

        throw new RuntimeException("Project " + id + " not found");
    }

}
