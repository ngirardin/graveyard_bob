package fr.dmconcept.bob.server.ioio;

import android.os.SystemClock;
import android.util.Log;
import fr.dmconcept.bob.server.activities.MainIOIOActivity;
import fr.dmconcept.bob.server.models.Project;
import fr.dmconcept.bob.server.models.Step;

import java.util.ArrayList;

public class ProjectLock {

    protected Project project;
    protected int duration;
    protected int sliceCount;
    protected ArrayList<ArrayList<Integer>> slices = new ArrayList();

    public ProjectLock() {

    }

    public Project getProject() {
        return project;
    }

    public int getDuration() {
        return duration;
    }

    public int getSliceCount() {
        return sliceCount;
    }

    public ArrayList<ArrayList<Integer>> getSlices() {
        return slices;
    }

    public void setProject(Project p) {
        this.project    = p;
        this.duration   = projectDuration();
        this.sliceCount = this.duration / (MainIOIOActivity.PERIOD / 1000);
        this.slices     = sliceSteps();
    }

    private int projectDuration() {

        int duration = 0;
        for (Step step: project.steps)
            duration += step.duration;

        return duration;
    }

    private ArrayList<ArrayList<Integer>> sliceSteps() {

        ArrayList<ArrayList<Integer>> s = new ArrayList();

        for (int i = 0; i < project.steps.size() - 1; i++) {

            Step step     = project.steps.get(i    );
            Step nextStep = project.steps.get(i + 1);

            int stepSliceCount = step.duration / (MainIOIOActivity.PERIOD / 1000);

            Log.i("~~~~~~~~~~~~~~~~", "Step duration=" + step.duration + " ms, slices=" + stepSliceCount);

            for (int sliceIndex = 0; sliceIndex < stepSliceCount; sliceIndex++) {

                ArrayList<Integer> interpolatedPositions = new ArrayList();

                for (int positionIndex = 0; positionIndex < step.positions.size(); positionIndex++) {

                    int initialPosition = step.positions.get(positionIndex);
                    int endPosition     = nextStep.positions.get(positionIndex);

                    // Interpolate the position
                    float deltaPosition = endPosition - initialPosition;
                    float deltaPerSlice = deltaPosition / stepSliceCount;

//                    if (sliceIndex == 0)
//                        Log.i("~~~~~~~~~~~~~~~~", "S" + positionIndex + " [" + initialPosition + " -> " + endPosition + "] delta: " + deltaPosition + ", deltaPerSlice: " + deltaPerSlice);

                    Float interpolatedPercentage = initialPosition + deltaPerSlice * sliceIndex;
                    Float interpolatedPosition = MainIOIOActivity.MIN + interpolatedPercentage * MainIOIOActivity.PERCENT;

                    interpolatedPositions.add(interpolatedPosition.intValue());
                }

                if (sliceIndex % 10 == 0)
                    Log.i("~~~~~~~~~~~~~~~~", "slice " + sliceIndex + ": " + interpolatedPositions);

                s.add(interpolatedPositions);
            }

            Log.i("~~~~~~~~~~~~~~~~", "");


        }

        if (s.size() != sliceCount)
            throw new RuntimeException("Expecting " + sliceCount + " slices but got " + s.size());

        return s;

    }

}


