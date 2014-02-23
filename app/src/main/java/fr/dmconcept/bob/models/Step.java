package fr.dmconcept.bob.models;

import java.util.ArrayList;

public class Step {

    // Default position getDuration is 5 seconds
    private static final int DEFAULT_DURATION = 5000;

    // Default position for the new positions is 50%
    private static final int DEFAULT_POSITION = 50;

    // The step getDuration in ms
    private int mDuration;

    // Store the servos position in percentage
    private ArrayList<Integer> mPositions;

    /**
     * Create a step of the default getDuration with each position at the default value
     *
     * @param servos the servos count
     */
    public Step(int servos) {
        this(servos, DEFAULT_DURATION);
    }

    /**
     * Create a step of the given getDuration with each position at the default value
     *
     * @param servos the servos count
     * @param duration the getDuration in ms
     */
    public Step(int servos, int duration) {

        this(duration, new ArrayList<Integer>());

        for (int i = 0; i < servos; i++)
            mPositions.add(DEFAULT_POSITION);

    }

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
     * @param servo the servo index
     *
     * @return the servo position between 0 and 100
     */
    public int getPosition(int servo) {

        assert servo < mPositions.size();

        return mPositions.get(servo);

    }

    /**
     * Set the position for a servo
     *
     * @param servo the servo index
     * @param position the position between 0 and 100
     */
    public void setPosition(int servo, int position) {

        assert servo > 0;
        assert servo < mPositions.size();

        assert position > -1;
        assert position < 101;

        mPositions.set(servo, position);

    }

}


