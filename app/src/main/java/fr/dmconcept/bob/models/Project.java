package fr.dmconcept.bob.models;

import java.util.UUID;

public class Project {

    public long id;
    public String name ;
    public String steps;
    public long boardConfigsId;


    public Project(Long id, String name, long boardConfig, String steps) {
        this.id           = id;
        this.name         = name;
        this.steps        = steps;
        this.boardConfigsId = boardConfig;
    }

    /**
     * @return the computed project duration (in ms)
     */
    public int duration() {

        int duration = 0;

        for (Step step : steps) duration += step.duration;

        return duration;

    }

    public int getServosCount() {
        if (steps.length == 0)
            return 0;

        return steps[0].getServosCount();
    }

}
