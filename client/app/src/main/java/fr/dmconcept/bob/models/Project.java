package fr.dmconcept.bob.models;

import android.util.Log;

import java.util.ArrayList;

public class Project {

    private static final String TAG = "models.Project";

    // The project id
    private Long mId;

    // The project name
    private String mName;

    // The project steps
    private ArrayList<Step> mSteps;

    // The project board config
    private BoardConfig  mBoardConfig;

    /**
     * Create a new project with a start and end step
     *
     * @param name the project mName
     * @param boardConfig the board config
     */
    public Project(String name, BoardConfig boardConfig) {

        this(-1, name, boardConfig, new ArrayList<Step>());

        addStep(2000);
        addStep(0);
    }

    /**
     * Create a new project
     *
     * @param id the project mId
     * @param name the project mName
     * @param boardConfig the project board configuration
     * @param steps the project mSteps (must contain at leat a start and end step)
     */
    public Project(long id, String name, BoardConfig boardConfig, ArrayList<Step> steps) {

        assert steps.size() >= 2;

        // Check that the all the steps contains the same servo
        // count as defined in the project's board config
        for(Step s: steps)
            //TODO ain't working nah?
            assert s.getPositions().size() == boardConfig.getServoCount();

        this.mId = id   ;
        this.mName = name ;
        this.mBoardConfig = boardConfig;
        this.mSteps = steps;
    }

    /**
     * @return the project id
     */
    public long getId() {
        return mId;
    }

    /**
     * @return the project name
     */
    public String getName() {
        return mName;
    }

    /**
     * @return the steps
     */
    public ArrayList<Step> getSteps() {
        return mSteps;
    }

    /**
     * Return a step
     * @param i the step index
     * @return the step
     */
    public Step getStep(int i) {
        return mSteps.get(i);
    }

    /**
     * @return the project board config
     */
    public BoardConfig getBoardConfig() {
        return mBoardConfig;
    }

    /**
     * @return the project duration in ms
     */
    public int getDuration() {

        int duration = 0;

        for (Step step : mSteps)
            duration += step.getDuration();

        return duration;

    }

    /**
     * Create a new step at the end of the project
     */
    public void addStep(int duration) {

        Log.i(TAG, "addStep(" + duration + ")");

        ArrayList<Integer> positions = new ArrayList<Integer>();

        for(ServoConfig ignored : mBoardConfig.getServoConfigs())
            positions.add(50);

        // Set the length on the last step
        mSteps.get(mSteps.size() - 1).setDuration(duration);

        // Add the new step
        mSteps.add(new Step(0, positions));

    }

    public void removeStep(int i) {

        Log.i(TAG, "removeStep(" + i + ")");

        mSteps.remove(i);

        // If the last step is removed, set the last step duration to 0
        if (i == mSteps.size()) {
            Log.i(TAG, "removeStep() - Removing last step setting the new last step duration to 0");
            mSteps.get(mSteps.size() -1).setDuration(0);
        }

    }

}
