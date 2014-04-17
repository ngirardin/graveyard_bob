package fr.dmconcept.bob.client

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper
import fr.dmconcept.bob.client.models.dao.{ProjectDao, BoardConfigDao}
import fr.dmconcept.bob.client.models.{Step, Project, ServoConfig, BoardConfig}
import java.util.UUID
import BobApplication._

object BobApplication extends BobLogger {

  val PREFERENCES_SERVER_IP = "sever_ip"

  val TAG = "BobApplication"

}

class BobApplication extends Application {

  lazy val mOpenHelper     : BobSqliteOpenHelper = new BobSqliteOpenHelper(this)
  lazy val mDatabase       : SQLiteDatabase      = mOpenHelper.getWritableDatabase()
  lazy val mBoardConfigDao : BoardConfigDao      = new BoardConfigDao(mDatabase)
  lazy val mProjectsDao    : ProjectDao          = new ProjectDao(mDatabase, mBoardConfigDao)

  override def onCreate() {

    log("onCreate()", "Application starting")

    if (/*BuildConfig.DEBUG && */ mProjectsDao.findAll().isEmpty)
      firstRun()

    super.onCreate()

  }

  private def firstRun() {

    log("onCreate()", "First run - Creating the servos config fixtures...")

    def servoConfigs(servoPins: Range): Vector[ServoConfig] = servoPins.map(
      ServoConfig(_, (558, 2472))
    ).toVector

    val configs = List(
      BoardConfig("Servos on pins 3 and 4", servoConfigs(3 to 4)),
      BoardConfig("Servos on pins 3 to 9" , servoConfigs(3 to 9))
    )

    configs.foreach(mBoardConfigDao.save)

    log("onCreate()", "First run - Creating the project fixtures...")

    // Project 1
    mProjectsDao.create(
      Project(UUID.randomUUID.toString, "Simple demo project", configs(0), Vector(
        Step(4000, Vector( 1, 50)),
        Step(2000, Vector(99, 50)),
        Step(4000, Vector(50, 50)),
        Step(0   , Vector( 1, 50))
      ))
    )

    // Project 2
    mProjectsDao.create(
      Project(UUID.randomUUID.toString, "Bob demo project", configs(1), Vector(
        Step( 5000, Vector(  0, 20, 20, 10, 20, 80, 100)),
        Step( 6000, Vector( 25, 40, 40, 20, 40, 60,  50)),
        Step( 5000, Vector( 75, 60, 60, 30, 60, 40,  25)),
        Step(10000, Vector(100, 80, 80, 40, 80, 20,   0))
      ))
    )

    log("onCreate", "First run - Fixtures creation done")

  }

  override def onTerminate() {

    log("onTerminate()", "Closing DB and open helper")

    // Close the database and the DB helper
    mDatabase.close()
    mOpenHelper.close()

    super.onTerminate()

  }

}
