package fr.dmconcept.bob.client.models.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.SystemClock
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper
import fr.dmconcept.bob.client.models.json.BobJsonProtocol._
import fr.dmconcept.bob.client.models.{ServoConfig, BoardConfig}
import org.scaloid.common._
import spray.json._

case class BoardConfigDao(database: SQLiteDatabase) extends TagUtil {

  implicit override val loggerTag = LoggerTag("BobClient")

  def save(boardConfig: BoardConfig) {

    val now = SystemClock.elapsedRealtime()

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_ID          , boardConfig.id  )
    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_NAME        , boardConfig.name)
    values.put(BobSqliteOpenHelper.BOARDCONFIG_COL_SERVO_CONFIG, boardConfig.servoConfigs.toJson.compactPrint)

    database
      .insert(BobSqliteOpenHelper.BOARDCONFIG_TABLE, null, values)
      .ensuring(_ > -1, "Can't save the board config")

    info(s"BoardConfigDao.save(${boardConfig.id}) took ${SystemClock.elapsedRealtime() - now} ms")

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

    info(s"BoardConfigDao.findById($id) took ${SystemClock.elapsedRealtime() - now} ms")

    boardConfig

  }

  def findAll(): Vector[BoardConfig] = {

    val now = SystemClock.elapsedRealtime

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

    info(s"BoardConfigDao.findAll() took ${SystemClock.elapsedRealtime - now} ms")

    boardConfigs

  }

  private def fromCursor(cursor: Cursor): BoardConfig = {

    val id               = cursor.getString(0)
    val name             = cursor.getString(1)
    val jsonServoConfigs = cursor.getString(2)

    BoardConfig(id, name, jsonServoConfigs.parseJson.convertTo[Vector[ServoConfig]])

  }

}
