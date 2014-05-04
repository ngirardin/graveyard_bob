package fr.dmconcept.bob.client.models.dao

import fr.dmconcept.bob.client.models.{ServoConfig, BoardConfig}
import android.content.ContentValues
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper
import android.database.Cursor
import scala.util.parsing.json.{JSONObject, JSONArray, JSON}
import fr.dmconcept.bob.client.BobLogger
import android.os.SystemClock
import BoardConfigDao.log
import android.database.sqlite.SQLiteDatabase

object BoardConfigDao extends BobLogger {

  val TAG = "models.dao.BoardConfigDao"

}

case class BoardConfigDao(database: SQLiteDatabase) {

  def save(boardConfig: BoardConfig) {

    val now = SystemClock.elapsedRealtime()

    val values = new ContentValues()

    val servoConfigs = {
      JSONArray(
        boardConfig.servoConfigs.toList.map(sc => JSONObject(sc.serialize))
      ).toString()
    }

    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_ID          , boardConfig.id  )
    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_NAME        , boardConfig.name)
    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_SERVO_CONFIG, servoConfigs    )

    database
      .insert(BobSqliteOpenHelper.BOARDCONFIG_TABLE, null, values)
      .ensuring(_ > -1, "Can't save the board config")

    log(s"save()", {
      val elapsed = SystemClock.elapsedRealtime() - now
      s"values: $values took $elapsed ms"
    })

  }

  def findById(id: String): BoardConfig = {

    val now = SystemClock.elapsedRealtime()

    val cursor = database.query(
      BobSqliteOpenHelper.BOARDCONFIG_TABLE,
      BobSqliteOpenHelper.BOARDCONFIG_ALL  ,
      s"${BobSqliteOpenHelper.BOARDCONFIG_COL_ID} = '$id'",
      null,
      null,
      null,
      null
    )

    cursor
      .moveToFirst()
      .ensuring(cond = true, "Can't move to the first record")

    val boardConfig = fromCursor(cursor)

    cursor.close()

    log(s"findById($id)", {
      val elapsed = SystemClock.elapsedRealtime() - now
      s"took $elapsed ms"
    })

    boardConfig

  }

  def findAll(): Vector[BoardConfig] = {

    val now = SystemClock.elapsedRealtime()

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
      .ensuring(cond = true, "Can't move to the first record")

    var boardConfigs = Vector[BoardConfig]()

    while (!cursor.isAfterLast) {
      boardConfigs = boardConfigs :+ fromCursor(cursor)
      cursor.moveToNext()
    }

    cursor.close()

    log(s"findAll()", {
      val elapsed = SystemClock.elapsedRealtime()
      s"took $elapsed ms"
    })

    boardConfigs

  }

  private def fromCursor(cursor: Cursor): BoardConfig = {

    val id               = cursor.getString(0)
    val name             = cursor.getString(1)
    val jsonServoConfigs = cursor.getString(2)

//    log(s"fromCursor()", s"Columns values are: id=$id, name=$name, jsonServoConfigs=$jsonServoConfigs")

    BoardConfig(id, name, {
      JSON.parseFull(jsonServoConfigs)
        .getOrElse(throw new RuntimeException(s"Can't deserialize the servo configs: $jsonServoConfigs"))
        .asInstanceOf[List[Map[String, Any]]]
        .map {
        servoConfigMap: Map[String, Any] =>
          ServoConfig.deserialize(servoConfigMap)
        }
        .toVector
    })

  }

}
