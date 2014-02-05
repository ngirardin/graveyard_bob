package fr.dmconcept.bob.models;

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

}
