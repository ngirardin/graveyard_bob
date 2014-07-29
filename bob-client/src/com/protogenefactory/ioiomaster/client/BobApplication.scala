package com.protogenefactory.ioiomaster.client

import java.util.UUID

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import com.protogenefactory.ioiomaster.client.BobApplication.Preferences
import com.protogenefactory.ioiomaster.client.connections.{Connection, LocalConnection, RemoteConnection}
import com.protogenefactory.ioiomaster.client.models.dao.{BoardConfigDao, ProjectDao}
import com.protogenefactory.ioiomaster.client.models.helpers.BobSqliteOpenHelper
import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project, ServoConfig, Step}
import com.protogenefactory.ioiomaster.server.services.ServerService
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

  implicit override val loggerTag = LoggerTag("Bob")

  lazy val sqliteOpenHelper : BobSqliteOpenHelper = new BobSqliteOpenHelper(this)
  lazy val sqliteDatabase   : SQLiteDatabase      = sqliteOpenHelper.getWritableDatabase

  lazy val boardConfigDao   : BoardConfigDao      = new BoardConfigDao(sqliteDatabase)
  lazy val projectsDao      : ProjectDao          = new ProjectDao    (sqliteDatabase, boardConfigDao)

  var connection: Connection = null

  override def onCreate() {

    info("BobApplication.onCreate()")

    if (/*BuildConfig.DEBUG && */ projectsDao.isEmpty)
      firstRun()

    // Start the server service
    startService[ServerService]

    super.onCreate()

  }

  private def firstRun() {

    info("BobApplication.onCreate() First run, creating the servos config fixtures")

    def servoConfigs(servoPins: List[Int]): Vector[ServoConfig] = servoPins.map { s =>
      ServoConfig(s"Servo $s", s)
    }.toVector

    val configs = List(
      BoardConfig("Servos on 2 first pins", servoConfigs(List(3, 4)               )),
      BoardConfig("Servos on all pins"    , servoConfigs(List(3, 4, 5, 6, 7, 10, 11, 12, 13)))
    )

    configs.foreach(boardConfigDao.create)

    info("BobApplication.onCreate() First run, creating the project fixtures")

    // Project 1
    projectsDao.create(
      Project(UUID.randomUUID.toString, "Simple demo project", configs(0), Vector(
        Step(4000, Vector( 1, 100)),
        Step(2000, Vector(99, 1)),
        Step(4000, Vector(50, 100)),
        Step(0   , Vector( 1, 1))
      ))
    )

    // Project 2
    projectsDao.create(
      Project(UUID.randomUUID.toString, "Bob demo project", configs(1), Vector(
        Step( 500, Vector(  0, 100,   0, 100,   0,  40,   0,  0,  40)),
        Step(1000, Vector(100,   0,  20,  80, 100,  60,  20, 20,  60)),
        Step( 200, Vector(  0, 100,  40,  60,  20,  20,  40, 40,  20)),
        Step( 500, Vector(100,   0,  60,  40,  80,  80,  60, 60,  80)),
        Step( 200, Vector(  0, 100,  80,  20,  40,   0,  80, 80,   0)),
        Step( 500, Vector(  0, 100,   0, 100,   0,  40,   0,  0,  40)),
        Step(1000, Vector(100,   0,  20,  80, 100,  60,  20, 20,  60)),
        Step( 200, Vector(  0, 100,  40,  60,  20,  20,  40, 40,  20)),
        Step( 500, Vector(100,   0,  60,  40,  80,  80,  60, 60,  80)),
        Step( 200, Vector(  0, 100,  80,  20,  40,   0,  80, 80,   0)),
        Step(  0 , Vector(100,   0, 100,   0,  60, 100, 100,  0, 100))
      ))
    )

  }

  def setLocalConnection(serverService: LocalServiceConnection[ServerService]) {
    info("BobApplication.setLocalConnection()")
    connection = new LocalConnection(serverService)
  }

  def setRemoteConnection(remoteIP: String) {

    info(s"BobApplication.setRemoteConnection($remoteIP)")

    defaultSharedPreferences
      .edit
      .putString(Preferences.SERVER_IP, remoteIP)
      .apply()

    connection = new RemoteConnection(remoteIP)

  }

  def serverIP = defaultSharedPreferences.getString(Preferences.SERVER_IP, "")

}
