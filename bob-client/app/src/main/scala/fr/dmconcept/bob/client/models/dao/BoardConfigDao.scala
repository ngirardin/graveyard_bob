package fr.dmconcept.bob.client.models.dao

import android.util.Log
import BoardConfigDao._
import android.database.sqlite.SQLiteDatabase
import fr.dmconcept.bob.client.models.{ServoConfig, BoardConfig}
import android.content.ContentValues
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper
import android.database.Cursor
import scala.util.parsing.json.{JSONObject, JSONArray, JSON}

object BoardConfigDao {

  final val TAG = "models.dao.BoardConfigDao"

}

case class BoardConfigDao(database: SQLiteDatabase) {

  def save(boardConfig: BoardConfig) {

    Log.i(TAG, s"save(${boardConfig.name})")

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_ID          , boardConfig.id          )
    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_NAME        , boardConfig.name        )
    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_SERVO_CONFIG, {
      val list: List[Map[String, Any]] = boardConfig.servoConfigs.map(_.serialize).toList
      JSONArray(list).toString()
    })

    database
      .insert(BobSqliteOpenHelper.BOARDCONFIG_TABLE, null, values)
      .ensuring(_ > -1, "Can't save the board config")
  }

  def findById(id: String): BoardConfig = {

    Log.i(TAG, s"findById($id)")

    val cursor = database.query(
      BobSqliteOpenHelper.BOARDCONFIG_TABLE,
      BobSqliteOpenHelper.BOARDCONFIG_ALL  ,
      s"${BobSqliteOpenHelper.BOARDCONFIG_COL_ID} = $id",
      null,
      null,
      null,
      null
    )

    cursor
      .moveToFirst()
      .ensuring(true, "Can't move to the first record")

    val boardConfig = fromCursor(cursor)

    cursor.close()

    boardConfig

  }

  def findAll(): Vector[BoardConfig] = {

    Log.i(TAG, "findAll()")

    val cursor = database.query(
      BobSqliteOpenHelper.BOARDCONFIG_TABLE,
      BobSqliteOpenHelper.BOARDCONFIG_ALL,
      null,
      null,
      null,
      null,
      null
    )

    cursor
      .moveToFirst()
      .ensuring(true, "Can't move to the first record")

    var boardConfigs = Vector[BoardConfig]()

    while (!cursor.isAfterLast) {
      boardConfigs = boardConfigs :+ fromCursor(cursor)
      cursor.moveToNext()
    }

    cursor.close()

    boardConfigs

  }

  private def fromCursor(cursor: Cursor): BoardConfig = {

    val id           = cursor.getString(0)
    val name         = cursor.getString(1)
    val servoConfigs = JSON.parseFull(cursor.getString(2)) match {

      case Some(arr: JSONArray) => arr.list.asInstanceOf[List[JSONObject]].map { j: JSONObject =>
        ServoConfig.deserialize(j.obj)
      }.toVector

      case None                  => throw new RuntimeException("Can't deserialize the servo configs")
    }

    BoardConfig(id, name, servoConfigs)

  }

}
