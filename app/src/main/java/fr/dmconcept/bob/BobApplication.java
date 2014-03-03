package fr.dmconcept.bob;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.ServoConfig;
import fr.dmconcept.bob.models.dao.BoardConfigDao;
import fr.dmconcept.bob.models.dao.ProjectsDao;
import fr.dmconcept.bob.models.helpers.BobSqliteOpenHelper;

import java.util.ArrayList;

public class BobApplication extends Application {

    private static final String TAG = "BobApplication";

    BobSqliteOpenHelper mOpenHelper;
    SQLiteDatabase      mDatabase;

    BoardConfigDao mBoardConfigDao;
    ProjectsDao    mProjectsDao;

    @Override
    public void onCreate() {

        Log.i(TAG, "onCreate()");

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "DEBUG MODE - Delete databse");
            deleteDatabase("project.db");
        }

        mOpenHelper = new BobSqliteOpenHelper(this);
        mDatabase   = mOpenHelper.getWritableDatabase();

        mBoardConfigDao = new BoardConfigDao(mDatabase);
        mProjectsDao    = new ProjectsDao   (mDatabase, mBoardConfigDao);

        if (BuildConfig.DEBUG)
            createFixtures();

        super.onCreate();

    }

    private void createFixtures() {

        Log.i(TAG, "DEBUG MODE - Creating servos config fixtures");

        ArrayList<ServoConfig> servoConfigs1 = new ArrayList<ServoConfig>() {{
            add(new ServoConfig(3, 1200, 1660, 50));
        }};

        ArrayList<ServoConfig> servoConfigs2 = new ArrayList<ServoConfig>() {{
            add(new ServoConfig(3, 1200, 1660, 50));
            add(new ServoConfig(4, 1200, 1660, 50));
            add(new ServoConfig(5, 1200, 1660, 50));
        }};

        BoardConfig config1 = new BoardConfig("Demo port 3"         , servoConfigs1);
        BoardConfig config2 = new BoardConfig("Demo port 3, 4 and 5", servoConfigs2);

        config1.setId(mBoardConfigDao.save(config1));
        config2.setId(mBoardConfigDao.save(config2));

        Log.i(TAG, "DEBUG MODE - Creating project...");

        mProjectsDao.save(new Project("Demo project", config1));

        Log.i(TAG, "DEBUG MODE - Fixtures creation done");

    }

    public ProjectsDao getProjectsDao() {
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
