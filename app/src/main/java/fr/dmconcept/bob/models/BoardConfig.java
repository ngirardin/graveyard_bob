package fr.dmconcept.bob.models;

/**
 * Created by jo on 21/02/14.
 */
public abstract class BoardConfig {

    private int id;
    private String name;
    private ServoConfig[] servoConfigs;


    public BoardConfig(int id, String name){

        this(id, name, null);
    }


    public BoardConfig(int id, String name, ServoConfig[] servoConfigs){

        this.id   = id;
        this.name = name;

        if(servoConfigs.length > 0){
            for(ServoConfig s:servoConfigs){
                addServoConfig(s);
            }
        }
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public ServoConfig[] getAllServoConfigs(){

        // db query code
        return null;
    }

    public ServoConfig getServoConfigByName(String servoConfigName){

        // db query code
        return null;
    }

    public void addServoConfig(ServoConfig servoConfig){

        // db query code
    }

    public void removeServoConfig(String servoConfigName){

        // db query code
    }

}
