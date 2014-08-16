package com.protogenefactory.ioiomaster.client.models.helpers

import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.util.Log
import com.protogenefactory.ioiomaster.client.models.helpers.BobSqliteOpenHelper._

object BobSqliteOpenHelper {

  final val DATABASE_NAME = "bobclient.db"
  private final val DATABASE_VERSION: Int = 1

  /**
   * BOARD CONFIG
   */
  final val BOARDCONFIG_TABLE = "boardconfig"
  final val BOARDCONFIG_COL_ID = "_id"
  final val BOARDCONFIG_COL_NAME = "name"
  final val BOARDCONFIG_COL_SERVO_CONFIG = "servoconfig"
  final val BOARDCONFIG_ALL: Array[String] = Array(BOARDCONFIG_COL_ID, BOARDCONFIG_COL_NAME, BOARDCONFIG_COL_SERVO_CONFIG)
  private final val BOARDCONFIG_CREATE_TABLE = "create table " + BOARDCONFIG_TABLE + "(" + BOARDCONFIG_COL_ID + " text primary key, " + BOARDCONFIG_COL_NAME + " text not null, " + BOARDCONFIG_COL_SERVO_CONFIG + " text not null" + ");"
  /**
   * PROJECT
   */
  final val PROJECT_TABLE = "project"
  final val PROJECT_COL_ID = "_id"
  final val PROJECT_COL_NAME = "name"
  final val PROJECT_COL_BOARD_CONFIG = "config"
  final val PROJECT_COL_STEPS = "steps"
  final val PROJECT_ALL: Array[String] = Array(PROJECT_COL_ID, PROJECT_COL_NAME, PROJECT_COL_BOARD_CONFIG, PROJECT_COL_STEPS)
  private final val PROJECT_CREATE_TABLE = "create table " + PROJECT_TABLE + "(" + PROJECT_COL_ID + " text primary key, " + PROJECT_COL_NAME + " text not null, " + PROJECT_COL_BOARD_CONFIG + " int not null, " + PROJECT_COL_STEPS + " text not null" + ");"
}

class BobSqliteOpenHelper(context: Context) extends SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  @Override def onCreate(database: SQLiteDatabase) {
    Log.i("Bob", "BobSqliteOpenHelper.onCreate() Creating the database")
    database.execSQL(BOARDCONFIG_CREATE_TABLE)
    database.execSQL(PROJECT_CREATE_TABLE)
  }

  @Override def onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
  }
}