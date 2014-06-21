package com.protogenefactory.ioiomaster.server.services

import android.app.{Notification, NotificationManager, Service}
import android.content.{Context, Intent}
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.models.Project
import com.protogenefactory.ioiomaster.server.activities.StatusActivity
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
    helper_.create()
    startHelper()

    // Start the web server
    bobServer.start()
    */

    /*
    info("server.ServerService.onCreate() Waiting for a request to launch the helper...")
    info(s"                                Listening on $ips")
    */

  }

  var count = -1

  /**
   *
   * Create the "IOIO connected" notification and start the looper
   */
  def startIOIOLooper() {
    info("ServerService.startIOIOLooper")
    notification("IOIO connected", "Touch for more information")
    count = 0
  }

  def getIOIOStatus(): String = {
    info("ServerService.getIOIOStatus")
    count = count + 1
    s"Status: $count"
  }

  def playProject(project: Project) {

    toast(s"ServerService.playProject($project)")
    info(s"ServerService.playProject($project)")

    //TODO check config
    mProject = project

  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {

    super.onStartCommand(intent, flags, startId)

    info(s"ServerService.onStartCommand() intent=$intent, startId=$startId")

    /*
    if (intent != null && intent.getAction() == Actions.STOP) {

      //TODO remove
      toast("onStartCommand() Received stop action from the notification")
      info("onStartCommand() Received stop action from the notification")

      // User clicked the notification. Need to stop the service.
      cancelNotifAndStop()

    }
    */

    // Don't stop on application exit
    Service.START_STICKY

  }

  private def cancelNotifAndStop() {
    toast("ServiceServer.Cancel notif and stop")

    val nm = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    nm.cancel(0)

    stopSelf()
  }

  onDestroy {

    //TODO remove
    info("##################### ServiceServer.onDestroy() ##################")
    /*
    stopHelper()
    helper_.destroy()
    */
  }

  /*
  private def startHelper(/*intent: Intent*/) {
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
  */

  override def createIOIOLooper(connectionType: String, extra: Object) = new BaseIOIOLooper() {

    override def setup() {

      info(s"ServerService.IOIOLooper.setup() mProject=$mProject")
      toast(s"ServerService.IOIOLooper.setup() mProject=$mProject")

      while (mProject == null) {
        Thread.sleep(200)
      }

      ioio_.openDigitalOutput(1)
    }

    override def loop() {

      info(s"ServerService.IOIOLooper.loop() mProject=$mProject")
      toast(s"ServerService.IOIOLooper.loop() mProject=$mProject")

      Thread.sleep(5000)

    }

    override def disconnected() {
      info(s"ServerService.IOIOLooper.disconnected()")
      toast(s"ServerService.IOIOLooper.disconnected()")
      /*
      cancelNotifAndStop()
      */
      stopSelf()
    }

    override def incompatible() {
      alert("Incompatible board", "This app need a board with at least the V5 firmware")
      /*
      cancelNotifAndStop()
      */
    }

  }

  override def onTaskRemoved(rootIntent: Intent) {
    info("ServerService.onTaskRemoved")
  }

  private def notification(title: String, message: String) {

    runOnUiThread {

      // Service starting. Create a notification.
      val notification = new Notification.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setTicker(title)
        .setContentTitle(title)
        .setContentText(message)
        .setContentIntent(
          // HACK: PendingIntent.FLAG_CANCEL_CURRENT needed to fix security permissions issues on KitKat as in
          // https://code.google.com/p/android/issues/detail?id=61850
          pendingActivity[StatusActivity]
          /*
          PendingIntent.getService(
            this, /*requestCode=*/0 , new Intent(Actions.STOP, null, this, this.getClass), PendingIntent.FLAG_CANCEL_CURRENT
          )
          */
        )
        .build()

      val id = new Random().nextInt

      startForeground(id, notification)

    }

  }

}
