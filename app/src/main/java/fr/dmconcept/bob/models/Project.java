package fr.dmconcept.bob.models;

import java.util.UUID;

public class Project {

    public String id   ;
    public String name ;
    public Step[] steps;
    public BoardConfig boardConfigs;

    public Project(String name, BoardConfig boardConfig) {
        this(
            UUID.randomUUID().toString(),
            name,
            boardConfig,
            new Step[2]
        );
    }

    public Project(String id, String name, BoardConfig boardConfig, Step[] steps) {
        this.id           = id   ;
        this.name         = name ;
        this.steps        = steps;
        this.boardConfigs = boardConfig;
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
