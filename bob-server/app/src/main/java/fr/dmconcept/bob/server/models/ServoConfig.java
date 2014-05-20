package fr.dmconcept.bob.server.models;

import java.util.Arrays;

/**
 * Created by jo on 21/02/14.
 */
public class ServoConfig {

    public static int FREQUENCY = 50; // Hz

    private static final int[] PERIPHERAL_PORTS = {
         1,  2,  3,  4,  5,  6,  7,
        10, 11, 12, 13, 14,
        18, 19, 20, 21, 22, 23, 24, 25, 26
    };

    public int port;
    public int start;
    public int end;

    public ServoConfig(int port, int start, int end) {

        // Check that the port is a peripheral port
        assert Arrays.binarySearch(PERIPHERAL_PORTS, port) > 0;
        assert start > 1000 && start < 3000;
        assert end   > 1000 && end   < 3000;
        assert start < end;

        this.port       = port;
        this.start      = start;
        this.end        = end;
    }

}
