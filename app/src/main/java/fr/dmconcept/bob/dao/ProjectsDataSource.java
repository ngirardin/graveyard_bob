package fr.dmconcept.bob.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.SQLiteHelper;
import fr.dmconcept.bob.models.Step;

public class ProjectsDataSource {

    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    private String[] allColumns = { SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_NAME, SQLiteHelper.COLUMN_STEPS, SQLiteHelper.COLUMN_BORD_CONFIG};


    public ProjectsDataSource(Context context){
        dbHelper = new SQLiteHelper(context);
    }

    public void open() throws SQLException{
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public long createProject(String name, Step[] steps){
        return this.createProject(name, 0, steps);
    }

    public long createProject(String name, int boardConfigId){
        return this.createProject(name, boardConfigId, null);
    }

    public long createProject(String name, int boardConfigId, Step[] steps){

        ContentValues values = new ContentValues();

        values.put(SQLiteHelper.COLUMN_NAME, name);
        values.put(SQLiteHelper.COLUMN_BORD_CONFIG, 0);
        values.put(SQLiteHelper.COLUMN_STEPS, "");

        return database.insert(SQLiteHelper.TABLE_PROJECT, null, values);
    }

    public Project findById(long projectId){

        Cursor cursor = database.query(SQLiteHelper.TABLE_PROJECT, allColumns, SQLiteHelper.COLUMN_ID + " = " + projectId, null, null, null, null);
        cursor.moveToFirst();
        Project project = cursorToProject(cursor);
        cursor.close();
        return project;
    }

    private Project cursorToProject(Cursor cursor) {

        return new Project( cursor.getLong(0),      // id
                            cursor.getString(1),    // name
                            cursor.getInt(2),       // boardConfigId
                            cursor.getString(3))    // steps string
    }



    private static final Project[] projects = new Project[] {

        new Project("111",
                    "First project",
                    null,
                    new Step[] {
                        new Step(1000, new int[] { 0, 50, 100} ),
                        new Step(500 , new int[] {30,  0,  50} ),
                        new Step(2000, new int[] {60,  0,  50} ),
                        new Step(0, new int[] {90, 50, 100} )
                    }),


        new Project("222",
                    "Second project",
                    null,
                    new Step[] {
                        new Step(1000, new int[] { 0, 100,   0,   0}),
                        new Step(5000, new int[] {20,  80, 100,  50}),
                        new Step(2000, new int[] {40,  60,   0, 100}),
                        new Step(3000, new int[] {60,  40, 100,  50}),
                        new Step(0, new int[] {80,  20,   0,   0})
                    })

    };

    public static Project[] all() {
        return projects;
    }

}
