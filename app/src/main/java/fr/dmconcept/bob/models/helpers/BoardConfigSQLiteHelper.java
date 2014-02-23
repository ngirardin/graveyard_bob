package fr.dmconcept.bob.models.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jo on 23/02/14.
 */
public class BoardConfigSQLiteHelper extends SQLiteOpenHelper{


    public static final String TABLE_BOARD_CONFIG = "boardconfig";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SERVO_CONFIG = "servoconfig";

    private static final String DATABASE_NAME = "project.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table " + TABLE_BOARD_CONFIG + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_NAME + " text not null, " +
            COLUMN_SERVO_CONFIG + " text not null);";

    public BoardConfigSQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database){
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_BOARD_CONFIG);
        onCreate(database);
    }

}
