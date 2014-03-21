package fr.dmconcept.bob.client.models.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BobSqliteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "models.helpers.BobSqliteOpenHelper";

    public  static final String DATABASE_NAME    = "project.db";
    private static final int    DATABASE_VERSION = 2;

    /**
     * BOARD CONFIG
     */
    public static final String BOARDCONFIG_TABLE            = "boardconfig";
    public static final String BOARDCONFIG_COL_ID           = "_id";
    public static final String BOARDCONFIG_COL_NAME         = "name";
    public static final String BOARDCONFIG_COL_SERVO_CONFIG = "servoconfig";

    public static final String[] BOARDCONFIG_ALL = {
        BOARDCONFIG_COL_ID,
        BOARDCONFIG_COL_NAME,
        BOARDCONFIG_COL_SERVO_CONFIG,
    };

    private static final String BOARDCONFIG_CREATE_TABLE =
        "create table " + BOARDCONFIG_TABLE + "(" +
            BOARDCONFIG_COL_ID           + " integer primary key autoincrement, " +
            BOARDCONFIG_COL_NAME         + " text not null, " +
            BOARDCONFIG_COL_SERVO_CONFIG + " text not null" +
        ");";


    /**
     * PROJECT
     */
    public static final String PROJECT_TABLE            = "project";
    public static final String PROJECT_COL_ID           = "_id";
    public static final String PROJECT_COL_NAME         = "name";
    public static final String PROJECT_COL_BOARD_CONFIG = "config";
    public static final String PROJECT_COL_STEPS        = "steps";

    public static final String[] PROJECT_ALL = {
        PROJECT_COL_ID,
        PROJECT_COL_NAME,
        PROJECT_COL_BOARD_CONFIG,
        PROJECT_COL_STEPS,
    };

    private static final String PROJECT_CREATE_TABLE =
        "create table " + PROJECT_TABLE + "(" +
            PROJECT_COL_ID           + " integer primary key autoincrement, " +
            PROJECT_COL_NAME         + " text not null, " +
            PROJECT_COL_BOARD_CONFIG + " int not null, "  +
            PROJECT_COL_STEPS        + " text not null"   +
        ");";

    public BobSqliteOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database){

        Log.i(TAG, "onCreate() - Creating the database");

        database.execSQL(BOARDCONFIG_CREATE_TABLE);
        database.execSQL(PROJECT_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){

        Log.i(TAG, "onUpgrade(database, " + oldVersion + ", " + newVersion + ")");

        database.execSQL("DROP TABLE IF EXISTS " + BOARDCONFIG_TABLE + ";");
        database.execSQL("DROP TABLE IF EXISTS " + PROJECT_TABLE     + ";");

        onCreate(database);

    }
}
