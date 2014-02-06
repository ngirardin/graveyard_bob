package fr.dmconcept.bob.models;

public class Step {

    // The step duration in ms
    public int duration;

    // Store the servos position, exprimed in percentage
    private int[] servos;

    public Step(int duration, int[] servos) {
        this.duration = duration;
        this.servos   = servos  ;
    }

    public int getServo(int i) {
        assert i < servos.length;
        return servos[i];
    }

    public void setServo(int servo, int position) {
        assert servo < servos.length;
        assert position >= 0  ;
        assert position <= 100;
    }

}


