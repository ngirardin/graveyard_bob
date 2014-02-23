package fr.dmconcept.bob.models;

import java.util.ArrayList;

public class Project {

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

        int servos = boardConfig.getServoCount();

        // Start step
        mSteps.add(new Step(servos));

        // End step with a 0 ms getDuration
        mSteps.add(new Step(servos, 0));

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
    public void addStep() {
        mSteps.add(new Step(mBoardConfig.getServoCount()));
    }

}
