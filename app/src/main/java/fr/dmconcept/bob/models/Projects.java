package fr.dmconcept.bob.models;

import java.util.ArrayList;

public abstract class Projects {

    //TODO use DB instead
    private static ArrayList<Step> createSteps(String csvSteps) {

        ArrayList<Step> steps = new ArrayList<Step>();

        for(String csvStep: csvSteps.split(";")) {

            ArrayList<Integer> positions = new ArrayList<Integer>();

            int    duration       = Integer.valueOf(csvStep.split(":")[0]);
            String splitPositions = csvStep.split(":")[1];

            for (String position: splitPositions.split(","))
                positions.add(Integer.valueOf(position));

            steps.add(new Step(duration, positions));
        }

        return steps;

    }

    private static final Project[] projects = new Project[] {

        new Project(
            111,
            "First project",
            new BoardConfig("IOIO 3", new ServoConfig[] {
                new ServoConfig(3, 1200, 1500, 50),
                new ServoConfig(4, 1200, 1500, 50),
                new ServoConfig(5, 1200, 1500, 50),
            }),
            createSteps(
                "1000:0,50,100;" +
                "500:30,0,50;"   +
                "2000:60,0,50;"  +
                "0:90,50,100"
            )
        ),

        new Project(
            222,
            "Second project",
            new BoardConfig("IOIO 4", new ServoConfig[] {
                new ServoConfig(3, 1350, 1560, 50),
                new ServoConfig(4, 1350, 1560, 50),
                new ServoConfig(5, 1350, 1560, 50),
                new ServoConfig(6, 1350, 1560, 50),
            }),
            createSteps(
                "1000:0,100,0,0;"    +
                "5000:20,80,100,50;" +
                "2000:40,60,0,100"   +
                "3000:60,40,100,50"  +
                "0:80,20,0,0"
            )
        )

    };

    public static Project[] all() {
        return projects;
    }

    public static Project findById(int id) {

        for (Project p : projects) {

            if (p.getId() == id)
                return p;

        }

        throw new RuntimeException("Project " + id + " not found");
    }

}
