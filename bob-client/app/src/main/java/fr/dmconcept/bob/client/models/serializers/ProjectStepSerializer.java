package fr.dmconcept.bob.client.models.serializers;

import android.util.Log;
import fr.dmconcept.bob.client.models.Step;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class ProjectStepSerializer {

    private static final String TAG = "models.serializers.ServoConfigSerializer";

    /**
     * Serialize the project steps values
     *
     * @param projectSteps the project steps
     *
     * @return the servo config as `duration1:servo11,servo12,servo3;1duration2:servo21,servo22,servo23`
     *
     */
    public static String serialize(ArrayList<Step> projectSteps){

        Log.i(TAG, "seralize() - Serializing " + projectSteps.size());

        StringBuilder sb = new StringBuilder();

        Iterator<Step> it = projectSteps.iterator();

        while (it.hasNext()) {

            Step step = it.next();

            sb.append(step.getDuration());
            sb.append(",");

            Iterator<Integer> positionsIterator = step.getPositions().iterator();

            while (positionsIterator.hasNext()) {

                Integer position = positionsIterator.next();
                sb.append(position);

                if (positionsIterator.hasNext())
                    sb.append(",");

            }

            if (it.hasNext())
                sb.append(";");

        }

        String serialized = sb.toString();

        Log.i(TAG, "serialize() - result: " + serialized);

        return serialized;

    }

    /**
     * Deserialize the project steps
     *
     * @param serialized the serialized steps
     *
     * @return the deserialized steps
     *
     */
    public static ArrayList<Step> deserialize(String serialized){

        Log.i(TAG, "deserialize(" + serialized + ")");

        ArrayList<Step> steps = new ArrayList<Step>();

        for (String serializedStep: serialized.split(";")) {

            String[] splitServoConfig = serializedStep.split(",");

            ArrayList<Integer> positions = new ArrayList<Integer>();

            int duration = Integer.parseInt(splitServoConfig[0]);

            for (int i = 1; i < splitServoConfig.length; i++)
                positions.add(Integer.parseInt(splitServoConfig[i]));

            steps.add(new Step(duration, positions));
        }

        Log.i(TAG, "deserialize() - Result: " + steps.size() + " steps");

        return steps;
    }

}
