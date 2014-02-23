package fr.dmconcept.bob.models;

public class BoardConfig {

    private int mId;
    private String mName;
    private ServoConfig[] mServoConfigs;


    public BoardConfig(String name, ServoConfig[] servoConfig){

        this(-1, name, servoConfig);
    }


    public BoardConfig(int id, String name, ServoConfig[] servoConfigs){

        this.mId           = id;
        this.mName         = name;
        this.mServoConfigs = servoConfigs;

    }

    public int getId(){
        return this.mId;
    }

    public String getName(){
        return this.mName;
    }

    public int getServoCount() {
        return mServoConfigs.length;
    }

    public static ServoConfig[] getAllServoConfigs(){

        // db query code
        return null;
    }

    public static ServoConfig getServoConfigByName(String servoConfigName){

        // db query code
        return null;
    }

    public static void addServoConfig(ServoConfig servoConfig){

        // db query code
    }

    public static void removeServoConfig(String servoConfigName){

        // db query code
    }

}
