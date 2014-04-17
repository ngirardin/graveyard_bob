package fr.dmconcept.bob.client.models.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import ProjectDao.log
import fr.dmconcept.bob.client.models.{Step, Project}
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper
import scala.util.parsing.json.{JSON, JSONArray}
import android.os.SystemClock

object ProjectDao {

  private final val TAG = "models.dao.ProjectDao"

  def log(msg: String) = Log.i(TAG, msg)

}

case class ProjectDao(

  database       : SQLiteDatabase,

  boardConfigDao : BoardConfigDao

) {

  private def serializeSteps(project: Project): String = JSONArray(project.steps.map(_.serialize).toList).toString

  private def deserializeSteps(serialized: String): Vector[Step] = JSON
    .parseFull(serialized).asInstanceOf[JSONArray].list.asInstanceOf[List[Map[String, Any]]]
    .map { s: Map[String, Any] => Step.deserialize(s)}
    .toVector

  def create(project: Project): Long = {

    log("create({$project.name})")

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.PROJECT_COL_NAME        , project.name          )
    values.put(BobSqliteOpenHelper.PROJECT_COL_BOARD_CONFIG, project.boardConfig.id)
    values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS       , serializeSteps(project))

    database.insert(BobSqliteOpenHelper.PROJECT_TABLE, null, values)

  }

  def saveSteps(project: Project) {

    val now = SystemClock.elapsedRealtime()

    val values = new ContentValues()

    values.put(BobSqliteOpenHelper.PROJECT_COL_STEPS, serializeSteps(project))

    database.update(
      BobSqliteOpenHelper.PROJECT_TABLE,
      values,
      s"${BobSqliteOpenHelper.PROJECT_COL_ID} =?",
      Array(project.id)
    ).ensuring(_ == 1, s"The database.update() method didn't returned 1")

    val elapsed = SystemClock.elapsedRealtime() - now

    log(s"save(project ${project.id} took $elapsed ms")

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

  def findById(id: Long): Project = {

    val now = SystemClock.elapsedRealtime()

    val cursor: Cursor = database.query(
      BobSqliteOpenHelper.PROJECT_TABLE,
      BobSqliteOpenHelper.PROJECT_ALL,
      s"${BobSqliteOpenHelper.PROJECT_COL_ID} = $id",
      null,
      null,
      null,
      null
    ).ensuring(_.getCount == 1, "The request returned more than 1 project")

    cursor.moveToFirst()

    val project = fromCursor(cursor)

    cursor.close()

    val elapsed = SystemClock.elapsedRealtime() - now

    log("findById($id) took $elapsed ms")

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

    while (!cursor.isAfterLast()) {
        projects = projects :+ fromCursor(cursor)
        cursor.moveToNext();
    }

    cursor.close();

    val elapsed = SystemClock.elapsedRealtime() - now
    log("findById($id) took $elapsed ms")

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
