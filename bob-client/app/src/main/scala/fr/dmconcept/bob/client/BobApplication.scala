package fr.dmconcept.bob.client

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import fr.dmconcept.bob.client.models.helpers.BobSqliteOpenHelper
import fr.dmconcept.bob.client.models.dao.{ProjectDao, BoardConfigDao}
import fr.dmconcept.bob.client.models.{Step, Project, ServoConfig, BoardConfig}
import java.util.UUID
import BobApplication.TAG

object BobApplication {

  val TAG = "BobApplication"

  val PREFERENCES_SERVER_IP = "sever_ip"

}

class BobApplication extends Application {

  lazy val mOpenHelper     : BobSqliteOpenHelper = new BobSqliteOpenHelper(this)
  lazy val mDatabase       : SQLiteDatabase      = mOpenHelper.getWritableDatabase()
  lazy val mBoardConfigDao : BoardConfigDao      = new BoardConfigDao(mDatabase)
  lazy val mProjectsDao    : ProjectDao          = new ProjectDao(mDatabase, mBoardConfigDao)

  override def onCreate() {

    Log.i(TAG, "onCreate()")

    if (BuildConfig.DEBUG && mProjectsDao.findAll().isEmpty)
        // First run, create the fixtures
        createFixtures()

    super.onCreate()

  }

  def createFixtures() {

    Log.i(TAG, "DEBUG MODE - Creating servos config fixtures...")

    val configs = List(
      BoardConfig("Servos on pins 3 and 4", Vector(
        ServoConfig(3, (558, 2472)),
        ServoConfig(4, (558, 2472))
      )),

      BoardConfig("Servos on pins 3 to 9", Vector(
        ServoConfig(3, (558, 2472)),
        ServoConfig(4, (558, 2472)),
        ServoConfig(5, (558, 2472)),
        ServoConfig(6, (558, 2472)),
        ServoConfig(7, (558, 2472)),
        ServoConfig(8, (558, 2472)),
        ServoConfig(9, (558, 2472))
      ))
    )

    configs.foreach(mBoardConfigDao.save)

    Log.i(TAG, "DEBUG MODE - Creating project fixtures...")

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

    Log.i(TAG, "DEBUG MODE - Fixtures creation done")

  }

  override def onTerminate() {

    // Close the database and the DB helper
    mDatabase.close()
    mOpenHelper.close()

    super.onTerminate()

  }

}
