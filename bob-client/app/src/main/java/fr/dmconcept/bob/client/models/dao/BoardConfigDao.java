package fr.dmconcept.bob.client.models.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.dmconcept.bob.client.models.BoardConfig;
import fr.dmconcept.bob.client.models.ServoConfig;
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper;
import fr.dmconcept.bob.client.models.serializers.ServoConfigSerializer;

import java.util.ArrayList;

public class BoardConfigDao {

    private static final String TAG = "models.dao.BoardConfigDao";

    private SQLiteDatabase mDatabase;

    public BoardConfigDao(SQLiteDatabase database) {

        Log.i(TAG, "BoardConfigDao()");

        mDatabase = database;

    }

    public long save(BoardConfig boardConfig){

        Log.i(TAG, "save(" + boardConfig.getName() + ")");

        ContentValues values = new ContentValues();

        values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_NAME        , boardConfig.getName());
        values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_SERVO_CONFIG, ServoConfigSerializer.serialize(boardConfig.getServoConfigs()));

        long row = mDatabase.insert(BobSqliteOpenHelper.BOARDCONFIG_TABLE, null, values);

        if (row == -1)
            throw new RuntimeException("Can't save the board config");

        return row;
    }

    public BoardConfig findById(long boardConfigId){

        Log.i(TAG, "findById(" + boardConfigId + ")");

        Cursor cursor = mDatabase.query(BobSqliteOpenHelper.BOARDCONFIG_TABLE, BobSqliteOpenHelper.BOARDCONFIG_ALL, BobSqliteOpenHelper.BOARDCONFIG_COL_ID + " = " + boardConfigId, null, null, null, null);

        cursor.moveToFirst();

        BoardConfig boardConfig = fromCursor(cursor);

        cursor.close();
        return boardConfig;
    }


    public ArrayList<BoardConfig> findAll(){

        Log.i(TAG, "findAll()");

        ArrayList<BoardConfig> boardConfigs = new ArrayList<BoardConfig>();
        Cursor cursor = mDatabase.query(BobSqliteOpenHelper.BOARDCONFIG_TABLE, BobSqliteOpenHelper.BOARDCONFIG_ALL, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            boardConfigs.add(fromCursor(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return boardConfigs;

    }

    private BoardConfig fromCursor(Cursor cursor) {

        ArrayList<ServoConfig> servoConfigs = ServoConfigSerializer.deserialize(cursor.getString(2));
        return new BoardConfig( cursor.getLong(0), cursor.getString(1), servoConfigs);

    }

}