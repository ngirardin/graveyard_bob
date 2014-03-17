package fr.dmconcept.bob.models.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.util.Log;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.helpers.BobSqliteOpenHelper;
import fr.dmconcept.bob.models.serializers.ProjectStepSerializer;

import java.util.ArrayList;

public class ProjectDao {

    private static final String TAG = "models.dao.ProjectDao";

    private SQLiteDatabase mDatabase;

    private BoardConfigDao mBoardConfigDao;

    public ProjectDao(SQLiteDatabase database, BoardConfigDao boardConfigDao) {

        Log.i(TAG, "ProjectDao()");

        mDatabase = database;

        mBoardConfigDao = boardConfigDao;
    }

    public long create(Project project){

        Log.i(TAG, "create(" + project.getName() + ")");

        ContentValues values = new ContentValues();

        values.put(BobSqliteOpenHelper.PROJECT_COL_NAME        , project.getName()                 );
        values.put(BobSqliteOpenHelper.PROJECT_COL_BOARD_CONFIG, project.getBoardConfig().getId()  );
        values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS       , ProjectStepSerializer.serialize(project.getSteps()));

        return mDatabase.insert(BobSqliteOpenHelper.PROJECT_TABLE, null, values);
    }

    public void saveSteps(Project project) {

        long s = SystemClock.elapsedRealtime();

        ContentValues values = new ContentValues();

        String serializedSteps = ProjectStepSerializer.serialize(project.getSteps());

        values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS, serializedSteps);

        int updated = mDatabase.update(
                BobSqliteOpenHelper.PROJECT_TABLE,
                values,
                BobSqliteOpenHelper.PROJECT_COL_ID + "=?",
                new String[] { String.valueOf(project.getId()) }
        );

        // Check that we updated 1 row
        assert(updated == 1);

        Log.d(TAG, "save(project: " + project.getId()+ ") took " + (SystemClock.elapsedRealtime() - s) + " ms");

    }

    public void savePosition(Project project, int stepIndex, int positionIndex, int newValue) {

        Log.i(TAG, "savePosition(project: " + project.getId() + ", step: " + stepIndex + ", position: " + positionIndex + ", newValue: " + newValue);

        project.getStep(stepIndex).setPosition(positionIndex, newValue);
        saveSteps(project);

    }

    public void saveDuration(Project project, int stepIndex, int newDuration) {

        Log.i(TAG, "saveDuration(project: " + project.getId() + ", step: " + stepIndex + ", newDuration: " + newDuration);

        project.getStep(stepIndex).setDuration(newDuration);
        saveSteps(project);

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

        return new Project(
            cursor.getLong  (0)                        ,           // PROJECT_COL_ID
            cursor.getString(1)                        ,           // PROJECT_COL_NAME
            mBoardConfigDao.findById(cursor.getLong(2)),           // PROJECT_COL_BOARD_CONFIG
            ProjectStepSerializer.deserialize(cursor.getString(3)) // PROJECT_COL_STEPS
        );

    }

}
