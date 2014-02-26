package fr.dmconcept.bob.models.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.ServoConfig;
import fr.dmconcept.bob.models.helpers.BobSqliteOpenHelper;

import java.util.ArrayList;

public class BoardConfigDao {

    private static final String TAG = "models.dao.BoardConfigDao";

    private static final String[] mAllColumns = {
        BobSqliteOpenHelper.BOARDCONFIG_COL_ID,
        BobSqliteOpenHelper.BOARDCONFIG_COL_NAME,
        BobSqliteOpenHelper.BOARDCONFIG_COL_SERVO_CONFIG
    };

    private SQLiteDatabase mDatabase;

    public BoardConfigDao(SQLiteDatabase database) {

        Log.i(TAG, "BoardConfigDao()");

        mDatabase = database;

    }

    public long save(BoardConfig boardConfig){

        Log.i(TAG, "save(" + boardConfig.getName() + ")");

        ContentValues values = new ContentValues();

        values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_NAME        , boardConfig.getName());
        values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_SERVO_CONFIG, serializeServoConfigs(boardConfig.getAllServoConfigs()));

        return mDatabase.insert(BobSqliteOpenHelper.BOARDCONFIG_TABLE, null, values);
    }

    public BoardConfig findById(long boardConfigId){

        Log.i(TAG, "findById(" + boardConfigId + ")");

        Cursor cursor = mDatabase.query(BobSqliteOpenHelper.BOARDCONFIG_TABLE, mAllColumns, BobSqliteOpenHelper.BOARDCONFIG_COL_ID + " = " + boardConfigId, null, null, null, null);
        cursor.moveToFirst();
        BoardConfig boardConfig = new BoardConfig(cursor.getLong(0), cursor.getString(1), unSerializeServoConfigs(cursor.getString(2)));
        cursor.close();
        return boardConfig;
    }


    public ArrayList<BoardConfig> findAll(){

        Log.i(TAG, "findAll()");

        ArrayList<BoardConfig> boardConfigs = new ArrayList<BoardConfig>();
        Cursor cursor = mDatabase.query(BobSqliteOpenHelper.BOARDCONFIG_TABLE, mAllColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            BoardConfig boardConfig = new BoardConfig(cursor.getLong(0), cursor.getString(1), unSerializeServoConfigs(cursor.getString(2)));
            boardConfigs.add(boardConfig);
            cursor.moveToNext();
        }

        cursor.close();
        return boardConfigs;
    }

    private String serializeServoConfigs(ArrayList<ServoConfig> servoConfigs){
        return "";
    }


    private ArrayList<ServoConfig> unSerializeServoConfigs(String servoConfigs){
        return new ArrayList<ServoConfig>();
    }

}
