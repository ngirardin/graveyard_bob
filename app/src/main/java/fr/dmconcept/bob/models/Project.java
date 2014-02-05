package fr.dmconcept.bob.models;

import java.util.UUID;

public class Project {

    public String id  ;
    public String name;

    public Project(String name) {
        this.id   = UUID.randomUUID().toString();
        this.name = name;
    }
}
