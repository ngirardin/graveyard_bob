package fr.dmconcept.bob.models.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Step;
import fr.dmconcept.bob.models.helpers.BobSqliteOpenHelper;

import java.util.ArrayList;

public class ProjectsDao {

    private static final String TAG = "models.dao.ProjectsDao";

    private static final String[] mAllColumns = {
            BobSqliteOpenHelper.PROJECT_COL_ID,
            BobSqliteOpenHelper.PROJECT_COL_NAME,
            BobSqliteOpenHelper.PROJECT_COL_STEPS,
            BobSqliteOpenHelper.PROJECT_COL_BORD_CONFIG
    };

    private SQLiteDatabase mDatabase;

    private BoardConfigDao mBoardConfigDao;

    public ProjectsDao(SQLiteDatabase database, BoardConfigDao boardConfigDao) {

        Log.i(TAG, "ProjectDao()");

        mDatabase = database;

        mBoardConfigDao = boardConfigDao;
    }

    public long save(Project project){

        ContentValues values = new ContentValues();

        values.put(BobSqliteOpenHelper.PROJECT_COL_NAME       , project.getName()                 );
        values.put(BobSqliteOpenHelper.PROJECT_COL_BORD_CONFIG, project.getBoardConfig().getId()  );
        values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS      , serializeSteps(project.getSteps()));

        return mDatabase.insert(BobSqliteOpenHelper.PROJECT_TABLE, null, values);
    }


    public Project findById(long projectId){

        Log.i(TAG, "findById(" + projectId + ")");

        Cursor cursor = mDatabase.query(BobSqliteOpenHelper.PROJECT_TABLE, mAllColumns, BobSqliteOpenHelper.PROJECT_COL_ID + " = " + projectId, null, null, null, null);
        cursor.moveToFirst();
        Project project = new Project( cursor.getLong(0), cursor.getString(1), mBoardConfigDao.findById(cursor.getLong(2)), unSerializeSteps(cursor.getString(3)));
        cursor.close();
        return project;
    }


    public ArrayList<Project> findAll(){

        Log.i(TAG, "findAll()");

        ArrayList<Project> projects = new ArrayList<Project>();
        Cursor cursor = mDatabase.query(BobSqliteOpenHelper.PROJECT_TABLE, mAllColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Project project = new Project( cursor.getLong(0), cursor.getString(1), mBoardConfigDao.findById(cursor.getLong(2)), unSerializeSteps(cursor.getString(3)));
            projects.add(project);
            cursor.moveToNext();
        }

        cursor.close();
        return projects;
    }


    private String serializeSteps(ArrayList<Step> steps){
        return "";
    }

    private ArrayList<Step> unSerializeSteps(String steps){
        return new ArrayList<Step>();
    }

}
