package fr.dmconcept.bob.server.models;

import java.util.List;

public class Project {

    public String id;

    public String name;

    public BoardConfig boardConfig;

    public List<Step> steps;

    public Project() {
        // Empty constructor for waiting on the project
    }

    public Project(String id, String name, BoardConfig boardConfig, List<Step> steps) {
        this.id          = id;
        this.name        = name;
        this.boardConfig = boardConfig;
        this.steps       = steps;
    }

}
