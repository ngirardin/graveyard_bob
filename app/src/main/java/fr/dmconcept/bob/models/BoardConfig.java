package fr.dmconcept.bob.models;

import java.util.ArrayList;

public class BoardConfig {

    private Long mId;
    private String mName;
    private ArrayList<ServoConfig> mServoConfigs;


    public BoardConfig(String name, ArrayList<ServoConfig> servoConfig){
        this(-1, name, servoConfig);
    }


    public BoardConfig(long id, String name, ArrayList<ServoConfig> servoConfigs){

        // Check that we got at least 1 servo config
        assert servoConfigs.size() > 0;

        this.mId           = id;
        this.mName         = name;
        this.mServoConfigs = servoConfigs;
    }

    public long getId(){
        return this.mId;
    }

    public String getName(){
        return this.mName;
    }

    public int getServoCount() {
        return this.mServoConfigs.size();
    }

    public ArrayList<ServoConfig> getServoConfigs(){
        return this.mServoConfigs;
    }

}
