package fr.dmconcept.bob.client.models.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.SystemClock
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper
import fr.dmconcept.bob.client.models.json.BobJsonProtocol._
import fr.dmconcept.bob.client.models.{Step, Project}
import org.scaloid.common._
import spray.json._

case class ProjectDao (

  database       : SQLiteDatabase,

  boardConfigDao : BoardConfigDao

) extends TagUtil {

  implicit override val loggerTag = LoggerTag("BobClient")

  def create(project: Project) {

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.PROJECT_COL_ID          , project.id            )
    values.put(BobSqliteOpenHelper.PROJECT_COL_NAME        , project.name          )
    values.put(BobSqliteOpenHelper.PROJECT_COL_BOARD_CONFIG, project.boardConfig.id)
    values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS       , project.steps.toJson.compactPrint)

    info(s"ProjectDao.create($project) values=$values")

    database.insert(BobSqliteOpenHelper.PROJECT_TABLE, null, values)

  }

  def saveSteps(project: Project) {

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

  //TODO dead code?
  /*
  def savePosition(Project project, int stepIndex, int positionIndex, int newValue) {

      Log.i(TAG, "savePosition(project: " + project.getId() + ", step: " + stepIndex + ", position: " + positionIndex + ", newValue: " + newValue);

      project.getStep(stepIndex).setPosition(positionIndex, newValue);
      saveSteps(project);

  }

  public void saveDuration(Project project, int stepIndex, int newDuration) {

      Log.i(TAG, "saveDuration(project: " + project.getId() + ", step: " + stepIndex + ", newDuration: " + newDuration);

      project.getStep(stepIndex).setDuration(newDuration);
      saveSteps(project);

  }
  */

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

    info(s"ProjectDao.findAll() took ${SystemClock.elapsedRealtime() - now} ms")

    projects

  }

  private def fromCursor(cursor: Cursor): Project =

    Project(
      cursor.getString(0)                         ,         // PROJECT_COL_ID
      cursor.getString(1)                         ,         // PROJECT_COL_NAME
      boardConfigDao.findById(cursor.getString(2)),         // PROJECT_COL_BOARD_CONFIG
      cursor.getString(3).parseJson.convertTo[Vector[Step]] // PROJECT_COL_STEPS
    )

  // TODO implement
  def delete(project: Project) = ???

}
