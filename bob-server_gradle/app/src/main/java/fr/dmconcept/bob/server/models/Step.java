package fr.dmconcept.bob.server.models;

import java.util.ArrayList;

public class Step {

    public int duration;

    public ArrayList<Integer> positions;

    public Step(int duration, ArrayList<Integer> positions) {

        assert duration         > 0;
        assert positions.size() > 0;

        this.duration  = duration;
        this.positions = positions;

    }

}


