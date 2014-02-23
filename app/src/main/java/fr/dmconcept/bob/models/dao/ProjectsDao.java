package fr.dmconcept.bob.models.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.Step;
import fr.dmconcept.bob.models.helpers.ProjectSQLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;

public class ProjectsDao {

    private SQLiteDatabase mDatabase;
    private ProjectSQLiteHelper mDBHelper;
    private String[] mAllColumns = { ProjectSQLiteHelper.COLUMN_ID, ProjectSQLiteHelper.COLUMN_NAME, ProjectSQLiteHelper.COLUMN_STEPS, ProjectSQLiteHelper.COLUMN_BORD_CONFIG};
    private BoardConfigDao mBoardConfigDao;


    public ProjectsDao(Context context){
        mDBHelper = new ProjectSQLiteHelper(context);
        mBoardConfigDao = new BoardConfigDao(context);
    }

    public void open() throws SQLException{
        mDatabase = mDBHelper.getWritableDatabase();
    }

    public void close(){
        mDBHelper.close();
    }


    public long save(Project project){

        ContentValues values = new ContentValues();

        values.put(ProjectSQLiteHelper.COLUMN_NAME, project.getName());
        values.put(ProjectSQLiteHelper.COLUMN_BORD_CONFIG, project.getBoardConfig().getId());
        values.put(ProjectSQLiteHelper.COLUMN_STEPS, serializeSteps(project.getSteps()));

        return mDatabase.insert(ProjectSQLiteHelper.TABLE_PROJECT, null, values);
    }


    public Project findById(long projectId){

        Cursor cursor = mDatabase.query(ProjectSQLiteHelper.TABLE_PROJECT, mAllColumns, ProjectSQLiteHelper.COLUMN_ID + " = " + projectId, null, null, null, null);
        cursor.moveToFirst();
        Project project = new Project( cursor.getLong(0), cursor.getString(1), mBoardConfigDao.findById(cursor.getLong(2)), unSerializeSteps(cursor.getString(3)));
        cursor.close();
        return project;
    }


    public ArrayList<Project> findAll(){

        ArrayList<Project> projects = new ArrayList<Project>();
        Cursor cursor = mDatabase.query(ProjectSQLiteHelper.TABLE_PROJECT, mAllColumns, null, null, null, null, null);
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
