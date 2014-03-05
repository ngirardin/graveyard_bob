package fr.dmconcept.bob.models;

import java.util.ArrayList;

public class Step {

    // The step getDuration in ms
    private int mDuration;

    // Store the servos position in percentage
    private ArrayList<Integer> mPositions;

    /**
     * Create a step with the given getDuration and positions
     *
     * @param duration the step getDuration in ms
     * @param positions the positions values
     */
    public Step(int duration, ArrayList<Integer> positions) {

        assert duration         > 0;
        assert positions.size() > 0;

        this.mDuration  = duration;
        this.mPositions = positions;

    }

    public int getDuration() {
        return mDuration;
    }

    /**
     * @return the positions
     */
    public ArrayList<Integer> getPositions() {
        return mPositions;
    }

    /**
     * Set the position for a servo
     *
     * @param position the position index
     * @param value the position value, between 0 and 100
     */
    public void setPosition(int position, int value) {

        assert position > 0 && position < mPositions.size();
        assert value > -1 && position < 101;

        mPositions.set(position, value);

    }

}


