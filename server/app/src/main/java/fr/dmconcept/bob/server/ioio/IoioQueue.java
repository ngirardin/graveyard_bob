package fr.dmconcept.bob.server.ioio;

import fr.dmconcept.bob.server.models.ServoConfig;
import fr.dmconcept.bob.server.models.Step;

import java.util.ArrayList;

public class IoioQueue {

    private ArrayList<ServoConfig> mServoConfigs;
    private ArrayList<Step> mSteps;

    public IoioQueue() {
        // Empty constructor
    }

    public ArrayList<Step> getSteps() {
        return mSteps;
    }

    public Step getStep(int i) {
        return mSteps.get(i);
    }

    public ArrayList<ServoConfig> getServoConfigs() {
        return mServoConfigs;
    }

    public void queueSteps(ArrayList<ServoConfig> servoConfigs, ArrayList<Step> steps) {
        mServoConfigs = servoConfigs;
        mSteps        = new ArrayList<Step>(steps);
    }

    public void clear() {
        mServoConfigs = null;
        mSteps        = null;
    }

    public boolean hasSequence() {
        return mServoConfigs != null && mSteps != null;
    }

    @Override
    public String toString() {
        return "IoioQueue(servoConfigs: " + mServoConfigs.size() + ", steps: " + mSteps.size() + ")";
    }
}
