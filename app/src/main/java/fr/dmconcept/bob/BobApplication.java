package fr.dmconcept.bob;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.ServoConfig;
import fr.dmconcept.bob.models.Step;
import fr.dmconcept.bob.models.dao.BoardConfigDao;
import fr.dmconcept.bob.models.dao.ProjectDao;
import fr.dmconcept.bob.models.helpers.BobSqliteOpenHelper;

import java.util.ArrayList;

public class BobApplication extends Application {

    private static final String TAG = "BobApplication";

    BobSqliteOpenHelper mOpenHelper;
    SQLiteDatabase      mDatabase;

    BoardConfigDao mBoardConfigDao;
    ProjectDao mProjectsDao;

    @Override
    public void onCreate() {

        Log.i(TAG, "onCreate()");

        mOpenHelper = new BobSqliteOpenHelper(this);
        mDatabase   = mOpenHelper.getWritableDatabase();

        mBoardConfigDao = new BoardConfigDao(mDatabase);
        mProjectsDao    = new ProjectDao(mDatabase, mBoardConfigDao);

        if (BuildConfig.DEBUG && mProjectsDao.findAll().size() == 0)
            // First run, create the fixtures
            createFixtures();

        super.onCreate();

    }

    private void createFixtures() {

        Log.i(TAG, "DEBUG MODE - Creating servos config fixtures");

        ArrayList<ServoConfig> servoConfigs1 = new ArrayList<ServoConfig>() {{

            add(new ServoConfig(3, 556, 2472, 50));
            add(new ServoConfig(4, 556, 2472, 50));
        }};

        ArrayList<ServoConfig> servoConfigs2 = new ArrayList<ServoConfig>() {{
            add(new ServoConfig(3, 1200, 1660, 50));
            add(new ServoConfig(4, 1200, 1660, 50));
            add(new ServoConfig(5, 1200, 1660, 50));
        }};

        BoardConfig config1 = new BoardConfig("Servos on port 3 and 4"  , servoConfigs1);
        BoardConfig config2 = new BoardConfig("Servos on port 3, 4 and 5", servoConfigs2);

        config1.setId(mBoardConfigDao.save(config1));
        config2.setId(mBoardConfigDao.save(config2));

        Log.i(TAG, "DEBUG MODE - Creating project...");

        Project project = new Project(-1, "Demo project", config1, new ArrayList<Step>() {{
            add(new Step(2000, new ArrayList<Integer>() {{
                add(  0);
                add(100);
            }}));
            add(new Step(4000, new ArrayList<Integer>() {{
                add(100);
                add(  0);
            }}));
            add(new Step(0, new ArrayList<Integer>() {{
                add( 50);
                add( 50);
            }}));
        }});

        mProjectsDao.create(project);

        Log.i(TAG, "DEBUG MODE - Fixtures creation done");

    }

    public ProjectDao getProjectsDao() {
        return mProjectsDao;
    }

    public BoardConfigDao getBoardConfigDao() {
        return mBoardConfigDao;
    }

    @Override
    public void onTerminate() {

        // Close the database and the DB helper
        mDatabase.close();
        mOpenHelper.close();

        super.onTerminate();

    }


}
