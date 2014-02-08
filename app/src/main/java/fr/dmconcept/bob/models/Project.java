package fr.dmconcept.bob.models;

import java.util.ArrayList;
import java.util.UUID;

public class Project {

    public String id   ;
    public String name ;
    public Step[] steps;

    public Project(String name) {
        this(
            UUID.randomUUID().toString(),
            name,
            new Step[]{}
        );
    }

    public Project(String id, String name, Step[] steps) {
        this.id    = id   ;
        this.name  = name ;
        this.steps = steps;
    }

    /**
     * @return the computed project duration (in ms)
     */
    public int duration() {

        int duration = 0;

        for (int i = 0; i < steps.length; i++)
            duration += steps[i].duration;

        return duration;

    }

    public int servosCount() {
        if (steps.length == 0)
            return 0;

        return steps[0].getServosCount();
    }

}
