package fr.dmconcept.bob.models;

/**
 * Created by jo on 21/02/14.
 */
public class ServoConfig {

    public int port;
    public int start;
    public int end;
    public int frequency;


    public ServoConfig(int port, int start, int end, int frequency){

        this.port       = port;
        this.start      = start;
        this.end        = end;
        this.frequency  = frequency;
    }

}
