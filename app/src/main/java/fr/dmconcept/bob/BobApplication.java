package fr.dmconcept.bob;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.dmconcept.bob.models.dao.BoardConfigDao;
import fr.dmconcept.bob.models.dao.ProjectsDao;
import fr.dmconcept.bob.models.helpers.BobSqliteOpenHelper;

public class BobApplication extends Application {

    private static final String TAG = "BobApplication";

    BobSqliteOpenHelper mOpenHelper;
    SQLiteDatabase      mDatabase;

    BoardConfigDao mBoardConfigDao;
    ProjectsDao    mProjectsDao;

    @Override
    public void onCreate() {

        // DEBUG
        Log.i(TAG, "onCreate()");
        deleteDatabase("project.db");
        // DEBUG

        mOpenHelper = new BobSqliteOpenHelper(this);
        mDatabase   = mOpenHelper.getWritableDatabase();

        mBoardConfigDao = new BoardConfigDao(mDatabase);
        mProjectsDao    = new ProjectsDao   (mDatabase, mBoardConfigDao);

        super.onCreate();

    }

    @Override
    public void onTerminate() {

        // Close the database and the DB helper
        mDatabase.close();
        mOpenHelper.close();

        super.onTerminate();

    }

    public ProjectsDao getProjectsDao() {
        return mProjectsDao;
    }

    public BoardConfigDao getBoardConfigDao() {
        return mBoardConfigDao;
    }

}
