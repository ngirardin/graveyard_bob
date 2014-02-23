package fr.dmconcept.bob.models.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.ServoConfig;
import fr.dmconcept.bob.models.helpers.BoardConfigSQLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;

public class BoardConfigDao {

    private SQLiteDatabase mDatabase;
    private BoardConfigSQLiteHelper mDBHelper;
    private String[] mAllColumns = { BoardConfigSQLiteHelper.COLUMN_ID, BoardConfigSQLiteHelper.COLUMN_NAME, BoardConfigSQLiteHelper.COLUMN_SERVO_CONFIG};


    public BoardConfigDao(Context context){
        mDBHelper = new BoardConfigSQLiteHelper(context);
    }

    public void open() throws SQLException{
        mDatabase = mDBHelper.getWritableDatabase();
    }

    public void close(){
        mDBHelper.close();
    }


    public long save(BoardConfig boardConfig){

        ContentValues values = new ContentValues();

        values.put(BoardConfigSQLiteHelper.COLUMN_NAME, boardConfig.getName());
        values.put(BoardConfigSQLiteHelper.COLUMN_SERVO_CONFIG, serializeServoConfigs(boardConfig.getAllServoConfigs()));

        return mDatabase.insert(BoardConfigSQLiteHelper.TABLE_BOARD_CONFIG, null, values);
    }


    public BoardConfig findById(long boardConfigId){

        Cursor cursor = mDatabase.query(BoardConfigSQLiteHelper.TABLE_BOARD_CONFIG, mAllColumns, BoardConfigSQLiteHelper.COLUMN_ID + " = " + boardConfigId, null, null, null, null);
        cursor.moveToFirst();
        BoardConfig boardConfig = new BoardConfig(cursor.getLong(0), cursor.getString(1), unSerializeServoConfigs(cursor.getString(2)));
        cursor.close();
        return boardConfig;
    }


    public ArrayList<BoardConfig> findAll(){

        ArrayList<BoardConfig> boardConfigs = new ArrayList<BoardConfig>();
        Cursor cursor = mDatabase.query(BoardConfigSQLiteHelper.TABLE_BOARD_CONFIG, mAllColumns, null, null, null, null, null);
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
