package fr.dmconcept.bob.client.models.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import fr.dmconcept.bob.client.models.{Step, Project}
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper
import scala.util.parsing.json.{JSONObject, JSON, JSONArray}
import android.os.SystemClock
import fr.dmconcept.bob.client.BobLogger
import ProjectDao.log

object ProjectDao extends BobLogger {

  val TAG = "models.dao.ProjectDao"

}

case class ProjectDao(

  database       : SQLiteDatabase,

  boardConfigDao : BoardConfigDao

) {

  private def serializeSteps(project: Project): String = JSONArray(
    project.steps.toList.map(s => JSONObject(s.serialize))
  ).toString()

  private def deserializeSteps(jsonSteps: String): Vector[Step] =
    JSON
    .parseFull(jsonSteps)
    .getOrElse(throw new RuntimeException(s"Can't deserialize the steps: $jsonSteps"))
    .asInstanceOf[List[Map[String,Any]]]
    .map { jsonStep: Map[String, Any] => Step.deserialize(jsonStep)}
    .toVector

  def create(project: Project) {

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.PROJECT_COL_ID          , project.id            )
    values.put(BobSqliteOpenHelper.PROJECT_COL_NAME        , project.name          )
    values.put(BobSqliteOpenHelper.PROJECT_COL_BOARD_CONFIG, project.boardConfig.id)
    values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS       , serializeSteps(project))

    log(s"create()", values.toString)

    database.insert(BobSqliteOpenHelper.PROJECT_TABLE, null, values)

  }

  def saveSteps(project: Project) {

    val now = SystemClock.elapsedRealtime()

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS, serializeSteps(project))

    database.update(
      BobSqliteOpenHelper.PROJECT_TABLE,
      values,
      s"${BobSqliteOpenHelper.PROJECT_COL_ID} = ?",
      Array(project.id)
    ).ensuring(_ == 1, s"The database.update() method didn't returned 1")

    val elapsed = SystemClock.elapsedRealtime() - now

    log(s"saveSteps()", s"$values took $elapsed ms")

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

    val elapsed = SystemClock.elapsedRealtime() - now

    log(s"findById($id)", s"took $elapsed ms")

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

    val elapsed = SystemClock.elapsedRealtime() - now
    log(s"findAll()", s"took $elapsed ms")

    projects

  }

  private def fromCursor(cursor: Cursor): Project =
    Project(
      cursor.getString(0)                         , // PROJECT_COL_ID
      cursor.getString(1)                         , // PROJECT_COL_NAME
      boardConfigDao.findById(cursor.getString(2)), // PROJECT_COL_BOARD_CONFIG
      deserializeSteps(cursor.getString(3))         // PROJECT_COL_STEPS
    )

  // TODO implement
  def delete(project: Project) = ???

}