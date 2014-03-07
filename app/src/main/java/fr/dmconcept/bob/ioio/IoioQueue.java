package fr.dmconcept.bob.ioio;

import fr.dmconcept.bob.models.Step;

import java.util.ArrayList;

public class IoioQueue {

    boolean consumed = true;
    int duration;
    ArrayList<Integer> startPosition;
    ArrayList<Integer> endPosition  ;

    public IoioQueue() {

    }

    public void playStart(Step step) {
        consumed = false;
        duration      = 0;
        startPosition = step.getPositions();
        endPosition   = null;
    }

    public void playEnd(Step step) {
        consumed = false;
        duration      = 0;
        startPosition = null;
        endPosition   = step.getPositions();
    }

    public void playStep(Step startStep, Step endStep) {
        consumed = false;
        duration      = startStep.getDuration();
        startPosition = startStep.getPositions();
        endPosition   = endStep.getPositions();
    }

    public void consumed() {
        consumed = true;
        duration      = 0;
        startPosition = null;
        endPosition   = null;
    }

    @Override
    public String toString() {
        return "IoioQueue(consumed: " + consumed + ", duration: " + duration + ", startPosition: " + startPosition + ", endPosition: " + endPosition + ")";
    }
}
