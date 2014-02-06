package fr.dmconcept.bob.models;

import java.util.ArrayList;
import java.util.Arrays;

public class Projects {

    private static final Project[] projects = new Project[] {

        new Project("111", "First project", new Step[] {
            new Step(1000, new int[] { 0, 50, 100} ),
            new Step(500 , new int[] {30,  0,  50} ),
            new Step(2000, new int[] {60,  0,  50} ),
            new Step(5000, new int[] {90, 50, 100} )
        }),


        new Project("222", "Second project", new Step[] {
            new Step(1000, new int[] { 0, 100,   0} ),
            new Step(5000, new int[] {20,  80, 100} ),
            new Step(2000, new int[] {40,  60,   0} ),
            new Step(3000, new int[] {60,  40, 100} ),
            new Step(5000, new int[] {80,  20,   0} )
        })

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
