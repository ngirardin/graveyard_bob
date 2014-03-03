package fr.dmconcept.bob.models.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.helpers.BobSqliteOpenHelper;
import fr.dmconcept.bob.models.serializers.ProjectStepSerializer;

import java.util.ArrayList;

public class ProjectsDao {

    private static final String TAG = "models.dao.ProjectsDao";

    private SQLiteDatabase mDatabase;

    private BoardConfigDao mBoardConfigDao;

    public ProjectsDao(SQLiteDatabase database, BoardConfigDao boardConfigDao) {

        Log.i(TAG, "ProjectDao()");

        mDatabase = database;

        mBoardConfigDao = boardConfigDao;
    }

    public long save(Project project){

        Log.i(TAG, "save(" + project.getName() + ")");

        ContentValues values = new ContentValues();

        values.put(BobSqliteOpenHelper.PROJECT_COL_NAME        , project.getName()                 );
        values.put(BobSqliteOpenHelper.PROJECT_COL_BOARD_CONFIG, project.getBoardConfig().getId()  );
        values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS       , ProjectStepSerializer.serialize(project.getSteps()));

        return mDatabase.insert(BobSqliteOpenHelper.PROJECT_TABLE, null, values);
    }


    public Project findById(long projectId){

        Log.i(TAG, "findById(" + projectId + ")");

        Cursor cursor = mDatabase.query(BobSqliteOpenHelper.PROJECT_TABLE, BobSqliteOpenHelper.PROJECT_ALL, BobSqliteOpenHelper.PROJECT_COL_ID + " = " + projectId, null, null, null, null);
        cursor.moveToFirst();

        Project project = fromCursor(cursor);

        cursor.close();
        return project;
    }


    public ArrayList<Project> findAll(){

        Log.i(TAG, "findAll()");

        ArrayList<Project> projects = new ArrayList<Project>();
        Cursor cursor = mDatabase.query(BobSqliteOpenHelper.PROJECT_TABLE, BobSqliteOpenHelper.PROJECT_ALL, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            projects.add(fromCursor(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return projects;
    }

    private Project fromCursor(Cursor cursor) {

        Project project = new Project(
            cursor.getLong  (0)                        ,           // PROJECT_COL_ID
            cursor.getString(1)                        ,           // PROJECT_COL_NAME
            mBoardConfigDao.findById(cursor.getLong(2)),           // PROJECT_COL_BOARD_CONFIG
            ProjectStepSerializer.deserialize(cursor.getString(3)) // PROJECT_COL_STEPS
        );

        return project;
    }

}
