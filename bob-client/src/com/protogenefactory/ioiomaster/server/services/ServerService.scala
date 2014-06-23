package com.protogenefactory.ioiomaster.server.services

import android.app.{Notification, Service}
import android.content.Intent
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.models.{Project, ServoConfig}
import com.protogenefactory.ioiomaster.server.activities.StatusActivity
import ioio.lib.api.PwmOutput
import ioio.lib.util.android.IOIOAndroidApplicationHelper
import ioio.lib.util.{BaseIOIOLooper, IOIOLooperProvider}
import org.scaloid.common._

import scala.util.Random

class ServerService extends LocalService with IOIOLooperProvider {

  override implicit val loggerTag = LoggerTag("Bob")

  /**
   * Manage the connection to the IOIO Board
   */
  private final val helper_ = new IOIOAndroidApplicationHelper(this, this)

  /**
   * Represents the IOIO connection state
   */
  private var started_ = false

  /*
  lazy val bobServer = new BobServer(p => playProject(p))
  */

  /**
   * The project being played
   */
  var mProject: Project = null

  onCreate {

    info(s"ServerService.onCreate()")

    /*
    // Start the IOIO helper

    // Start the web server
    bobServer.start()
    */

    /*
    info("server.ServerService.onCreate() Waiting for a request to launch the helper...")
    info(s"                                Listening on $ips")
    */

  }

  /**
   *
   * Create the "IOIO connected" notification and start the looper
   */
  def startIOIOLooper() {

    info("ServerService.startIOIOLooper")
    helper_.create()
    startHelper()

  }

  def isIOIOStarted(): Boolean = {

    info(s"ServerService.getIOIOStatus() connected=$started_")
    started_

  }

  /*
  def playProject(project: Project) {

    toast(s"ServerService.playProject($project)")
    info(s"ServerService.playProject($project)")

    //TODO check config
    mProject = project

  }
  */

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {

    super.onStartCommand(intent, flags, startId)

    info(s"ServerService.onStartCommand() intent=$intent, startId=$startId")

    // Don't stop on application exit
    Service.START_STICKY

  }

  onDestroy {

    //TODO remove
    info("##################### ServiceServer.onDestroy() ##################")
    /*
    stopHelper()
    helper_.destroy()
    */
  }

  private def startHelper(/*intent: Intent*/) {

    info(s"ServerService.startHelper() started=$started_")

    if (!started_) {
      helper_.start()
      started_ = true
      /*
    } else {
      if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
        toast("start FLAG_ACTIVITY_NEW_TASK")
        helper_.restart()
      }
    */
    }
  }

  private def stopHelper() {

    if (started_) {
      helper_.stop()
      started_ = false
    }

  }

  override def createIOIOLooper(connectionType: String, extra: Object) = new BaseIOIOLooper() {

    var ports: Vector[PwmOutput] = null

    override def setup() {

      info(s"ServerService.IOIOLooper.setup() Openning PWM port ${ServoConfig.PERIPHERAL_PORTS.mkString(", ")}")
      toast(s"ServerService.IOIOLooper.setup() Openning PWM port ${ServoConfig.PERIPHERAL_PORTS.mkString(", ")}")

      ports = ServoConfig.PERIPHERAL_PORTS.map(pin =>
        ioio_.openPwmOutput(pin, 50)
      )

      notification("IOIO connected", "Touch for more information")

    }

    override def loop() {

      info(s"ServerService.IOIOLooper.loop() mProject=$mProject")
      toast(s"ServerService.IOIOLooper.loop() mProject=$mProject")

      Thread.sleep(5000)

    }

    override def disconnected() {

      info(s"ServerService.IOIOLooper.disconnected()")
      toast(s"ServerService.IOIOLooper.disconnected()")

      stopHelper()
      stopForeground(true)

    }

    override def incompatible() {

      alert("Incompatible board", "This app need a board with at least the V5 firmware")

      // getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager].cancel(0)

    }

  }

  private def notification(title: String, message: String) {

    runOnUiThread {

      // Service starting. Create a notification.
      val notification = new Notification.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setTicker(title)
        .setContentTitle(title)
        .setContentText(message)
        .setContentIntent(pendingActivity[StatusActivity])
        .build()

      startForeground(new Random().nextInt, notification)

    }

  }

}
