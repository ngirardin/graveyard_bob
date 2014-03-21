package fr.dmconcept.bob.client.models.serializers;

import fr.dmconcept.bob.client.models.ServoConfig;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class ServoConfigSerializer {

    private static final String TAG = "models.serializers.ServoConfigSerializer";

    /**
     * Serialize the servo configs values
     *
     * @param servoConfigs the servo configs to serialize
     *
     * @return the servo config as `port1,start1,end1,frequency1;portN,startN,endN,frequencyN`
     *
     */
    public static String serialize(ArrayList<ServoConfig> servoConfigs){

        StringBuilder sb = new StringBuilder();

        Iterator<ServoConfig> it = servoConfigs.iterator();

        while(it.hasNext()) {

            ServoConfig servoConfig = it.next();

            sb.append(servoConfig.port);
            sb.append(",");
            sb.append(servoConfig.start);
            sb.append(",");
            sb.append(servoConfig.end);
            sb.append(",");
            sb.append(servoConfig.frequency);

            if (it.hasNext())
                sb.append(";");

        }

        return sb.toString();

    }

    /**
     * Deserialize the servo config
     *
     * @param serialized the serialized servo configs
     *
     * @return the deserialized servo configs
     *
     */
    public static ArrayList<ServoConfig> deserialize(String serialized){

        ArrayList<ServoConfig> servoConfigs = new ArrayList<ServoConfig>();

        for (String serializedServo: serialized.split(";")) {

            String[] splitServoConfig = serializedServo.split(",");

            int port      = Integer.parseInt(splitServoConfig[0]);
            int start     = Integer.parseInt(splitServoConfig[1]);
            int end       = Integer.parseInt(splitServoConfig[2]);
            int frequency = Integer.parseInt(splitServoConfig[3]);

            servoConfigs.add(new ServoConfig(port, start, end, frequency));
        }

        return servoConfigs;
    }

}
