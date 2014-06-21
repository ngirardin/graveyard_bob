package com.protogenefactory.ioiomaster.client.models.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.SystemClock
import com.protogenefactory.ioiomaster.client.models.helpers.BobSqliteOpenHelper
import com.protogenefactory.ioiomaster.client.models.json.BobJsonProtocol._
import com.protogenefactory.ioiomaster.client.models.{Project, Step}
import org.scaloid.common._
import spray.json._

case class ProjectDao (
  database       : SQLiteDatabase,
  boardConfigDao : BoardConfigDao
) extends TagUtil {

  implicit override val loggerTag = LoggerTag("Bob")

  def create(project: Project) {

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.PROJECT_COL_ID          , project.id            )
    values.put(BobSqliteOpenHelper.PROJECT_COL_NAME        , project.name          )
    values.put(BobSqliteOpenHelper.PROJECT_COL_BOARD_CONFIG, project.boardConfig.id)
    values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS       , project.steps.toJson.compactPrint)

    info(s"ProjectDao.create($project) values=$values")

    database.insert(BobSqliteOpenHelper.PROJECT_TABLE, null, values)

  }

  def updateSteps(project: Project) {

    val now = SystemClock.elapsedRealtime()

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS, project.steps.toJson.compactPrint)

    database.update(
      BobSqliteOpenHelper.PROJECT_TABLE,
      values,
      s"${BobSqliteOpenHelper.PROJECT_COL_ID} = ?",
      Array(project.id)
    ).ensuring(_ == 1, s"The database.update() method didn't returned 1")

    info(s"ProjectDao.saveSteps(${project.id}) took ${SystemClock.elapsedRealtime() - now} ms")

  }

  def updateName(project: Project, newName: String) {

    val now = SystemClock.elapsedRealtime()

    val values = new ContentValues()
    values.put(BobSqliteOpenHelper.PROJECT_COL_NAME, newName)

    database.update(
      BobSqliteOpenHelper.PROJECT_TABLE,
      values,
      s"${BobSqliteOpenHelper.PROJECT_COL_ID} = ?",
      Array(project.id)
    ).ensuring(_ == 1, s"The database.update() method didn't returned 1")

    info(s"ProjectDao.updateName(${project.id}) took ${SystemClock.elapsedRealtime() - now} ms")

  }

  def isEmpty(): Boolean = {

    val cursor = database.query(
      BobSqliteOpenHelper.PROJECT_TABLE,
      BobSqliteOpenHelper.PROJECT_ALL,
      null,
      null,
      null,
      null,
      null
    )

    val empty = cursor.getCount() == 0

    cursor.close()

    empty

  }

  def findById(id: String): Project = {

    val now = SystemClock.elapsedRealtime()

    val cursor: Cursor = database.query(
      BobSqliteOpenHelper.PROJECT_TABLE,            // table
      BobSqliteOpenHelper.PROJECT_ALL,              // columns
      BobSqliteOpenHelper.PROJECT_COL_ID + " = ?", // selection
      Array(id),                                    // selectionArgs
      null,
      null,
      null
    ).ensuring(_.getCount == 1, s"The request returned more than 1 project")

    cursor.moveToFirst()

    val project = fromCursor(cursor)

    cursor.close()

    info(s"ProjectDao.findById($id) took ${SystemClock.elapsedRealtime() - now} ms")

    project

  }

  def findAll(): Vector[Project] = {

    val now = SystemClock.elapsedRealtime()

    val cursor = database.query(
      BobSqliteOpenHelper.PROJECT_TABLE,
      BobSqliteOpenHelper.PROJECT_ALL,
      null,
      null,
      null,
      null,
      null
    )

    cursor.moveToFirst()

    var projects: Vector[Project] =  Vector[Project]()

    while (!cursor.isAfterLast) {
        projects = projects :+ fromCursor(cursor)
        cursor.moveToNext()
    }

    cursor.close()

    info(s"ProjectDao.findAll() took ${SystemClock.elapsedRealtime() - now} ms to retrieve ${projects.length} projects")

    projects

  }

  def delete(project: Project) {

    val now = SystemClock.elapsedRealtime()

    database.delete(
      BobSqliteOpenHelper.PROJECT_TABLE,
      s"${BobSqliteOpenHelper.PROJECT_COL_ID} = ?",
      Array(project.id)
    ).ensuring(_ == 1, s"The database.delete() method didn't returned 1")

    info(s"ProjectDao.delete(${project.id}) took ${SystemClock.elapsedRealtime() - now} ms")

  }

  private def fromCursor(cursor: Cursor): Project =
    Project(
      cursor.getString(0)                         ,         // PROJECT_COL_ID
      cursor.getString(1)                         ,         // PROJECT_COL_NAME
      boardConfigDao.findById(cursor.getString(2)),         // PROJECT_COL_BOARD_CONFIG
      cursor.getString(3).parseJson.convertTo[Vector[Step]] // PROJECT_COL_STEPS
    )

}
