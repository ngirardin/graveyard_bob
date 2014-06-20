package com.protogenefactory.ioiomaster.client

import java.util.UUID

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import com.protogenefactory.ioiomaster.client.BobApplication.Preferences
import com.protogenefactory.ioiomaster.client.models.dao.{BoardConfigDao, ProjectDao}
import com.protogenefactory.ioiomaster.client.models.helpers.BobSqliteOpenHelper
import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project, ServoConfig, Step}
import org.scaloid.common._

object BobApplication {

  object Preferences {
    val SERVER_IP = "serverIP"
    val AUTOPLAY  = "autoplay"
  }

  object Params {
    val PORT = 8000
  }

}

class BobApplication extends Application with SContext {

  implicit override val loggerTag = LoggerTag("BobClient")

  lazy val sqliteOpenHelper : BobSqliteOpenHelper = new BobSqliteOpenHelper(this)
  lazy val sqliteDatabase   : SQLiteDatabase      = sqliteOpenHelper.getWritableDatabase

  lazy val boardConfigDao   : BoardConfigDao      = new BoardConfigDao(sqliteDatabase)
  lazy val projectsDao      : ProjectDao          = new ProjectDao    (sqliteDatabase, boardConfigDao)

  override def onCreate() {

    info("BobApplication.onCreate()")

    if (/*BuildConfig.DEBUG && */ projectsDao.findAll().isEmpty)
      firstRun()

    super.onCreate()

  }

  private def firstRun() {

    info("BobApplication.onCreate() First run, creating the servos config fixtures")

    def servoConfigs(servoPins: List[Int]): Vector[ServoConfig] = servoPins.map { s =>
      ServoConfig(s"Servo $s", s)
    }.toVector

    val configs = List(
      BoardConfig("Servos on pins 3 and 4", servoConfigs(List(3, 4)               )),
      BoardConfig("Servos on pins 1 to 7" , servoConfigs(List(1, 2, 3, 4, 5, 6, 7)))
    )

    configs.foreach(boardConfigDao.create)

    info("BobApplication.onCreate() First run, creating the project fixtures")

    // Project 1
    projectsDao.create(
      Project(UUID.randomUUID.toString, "Simple demo project", configs(0), Vector(
        Step(4000, Vector( 1, 50)),
        Step(2000, Vector(99, 50)),
        Step(4000, Vector(50, 50)),
        Step(0   , Vector( 1, 50))
      ))
    )

    // Project 2
    projectsDao.create(
      Project(UUID.randomUUID.toString, "Bob demo project", configs(1), Vector(
        Step( 500, Vector(  0, 100,   0, 100,   0,  40,   0)),
        Step(1000, Vector(100,   0,  20,  80, 100,  60,  20)),
        Step( 200, Vector(  0, 100,  40,  60,  20,  20,  40)),
        Step( 500, Vector(100,   0,  60,  40,  80, 800,  60)),
        Step( 200, Vector(  0, 100,  80,  20,  40,   0,  80)),
        Step(  0 , Vector(100,   0, 100,   0,  60, 100, 100)),

        Step( 500, Vector(  0, 100,   0, 100,   0,  40,   0)),
        Step(1000, Vector(100,   0,  20,  80, 100,  60,  20)),
        Step( 200, Vector(  0, 100,  40,  60,  20,  20,  40)),
        Step( 500, Vector(100,   0,  60,  40,  80, 800,  60)),
        Step( 200, Vector(  0, 100,  80,  20,  40,   0,  80)),
        Step(  0 , Vector(100,   0, 100,   0,  60, 100, 100))
      ))
    )

  }

  override def onTerminate() {

    info("BobApplication.onTerminate() Closing DB and open helper")

    // Close the database and the DB helper
    sqliteDatabase.close()
    sqliteOpenHelper.close()

    super.onTerminate()

  }

  def serverIP =
    defaultSharedPreferences.getString(Preferences.SERVER_IP, "")

  def serverIP(serverIP: String) {
    defaultSharedPreferences
      .edit
      .putString(Preferences.SERVER_IP, serverIP)
      .apply()
  }

}
