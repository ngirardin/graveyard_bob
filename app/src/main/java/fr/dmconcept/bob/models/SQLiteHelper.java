package fr.dmconcept.bob.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jo on 23/02/14.
 */
public class SQLiteHelper extends SQLiteOpenHelper{


    public static final String TABLE_PROJECT = "project";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_BORD_CONFIG = "config";

    private static final String DATABASE_NAME = "project.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table " + TABLE_PROJECT + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_NAME + " text not null, " +
            COLUMN_STEPS + " text not null, " +
            COLUMN_BORD_CONFIG + " int not null);";

    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database){
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT);
        onCreate(database);
    }

}
