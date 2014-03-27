package fr.dmconcept.bob.client;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import fr.dmconcept.bob.client.models.BoardConfig;
import fr.dmconcept.bob.client.models.Project;
import fr.dmconcept.bob.client.models.ServoConfig;
import fr.dmconcept.bob.client.models.Step;
import fr.dmconcept.bob.client.models.dao.BoardConfigDao;
import fr.dmconcept.bob.client.models.dao.ProjectDao;
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper;

public class BobApplication extends Application {

    private static final String TAG = "BobApplication";

    public static final String PREFERENCES_SERVER_IP = "sever_ip";

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
            add(new ServoConfig(3, 558, 2472, 50));
            add(new ServoConfig(4, 558, 2472, 50));
        }};

        ArrayList<ServoConfig> servoConfigs2 = new ArrayList<ServoConfig>() {{
            add(new ServoConfig(3, 558, 2472, 50));
            add(new ServoConfig(4, 558, 2472, 50));
            add(new ServoConfig(5, 558, 2472, 50));
            add(new ServoConfig(6, 558, 2472, 50));
            add(new ServoConfig(7, 558, 2472, 50));
            add(new ServoConfig(8, 558, 2472, 50));
            add(new ServoConfig(9, 558, 2472, 50));
        }};

        BoardConfig config1 = new BoardConfig("Servos on pins 3 and 4"  , servoConfigs1);
        BoardConfig config2 = new BoardConfig("Servos on pins 3 to 9", servoConfigs2);

        config1.setId(mBoardConfigDao.save(config1));
        config2.setId(mBoardConfigDao.save(config2));

        Log.i(TAG, "DEBUG MODE - Creating project...");

        // Project 1
        mProjectsDao.create(
            new Project(-1, "Simple demo project", config1, new ArrayList<Step>() {{
                add(new Step(4000, new ArrayList<Integer>() {{ add( 1); add(50); }}));
                add(new Step(2000, new ArrayList<Integer>() {{ add(99); add(50); }}));
                add(new Step(4000, new ArrayList<Integer>() {{ add(50); add(50); }}));
                add(new Step(0   , new ArrayList<Integer>() {{ add( 1); add(50); }}));
            }})
        );

        // Project 2
        mProjectsDao.create(
            new Project(-1, "Bob demo project", config2, new ArrayList<Step>() {{
                add(new Step(5000, new ArrayList<Integer>() {{
                    add(  0); add(20); add(20); add(10); add(20); add(80); add( 100);
                }}));
                add(new Step(6000, new ArrayList<Integer>() {{
                    add( 25); add(40); add(40); add(20); add(40); add(60); add( 50);
                }}));
                add(new Step(5000, new ArrayList<Integer>() {{
                    add( 75); add(60); add(60); add(30); add(60); add(40); add( 25);
                }}));
                add(new Step(10000, new ArrayList<Integer>() {{
                    add(100); add(80); add(80); add(40); add(80); add(20); add(  0);
                }}));
            }})
        );

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