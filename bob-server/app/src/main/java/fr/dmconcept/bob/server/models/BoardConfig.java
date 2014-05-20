package fr.dmconcept.bob.server.models;

import java.util.List;

public class BoardConfig {

    public String id;

    public String name;

    public List<ServoConfig> servoConfigs;

    public BoardConfig(String id, String name, List<ServoConfig> servoConfigs) {
        this.id           = id;
        this.name         = name;
        this.servoConfigs = servoConfigs;
    }

}
