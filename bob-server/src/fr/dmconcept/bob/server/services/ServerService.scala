package fr.dmconcept.bob.server.services

import android.content.{Context, Intent}
import android.app.{Service, Notification, NotificationManager}
import org.scaloid.common._
import fr.dmconcept.bob.server.R.drawable
import fr.dmconcept.bob.server.services.ServerService.Actions
import ioio.lib.util.android.IOIOAndroidApplicationHelper
import ioio.lib.util.{IOIOLooperProvider, BaseIOIOLooper}
import fr.dmconcept.bob.server.BobServer
import fr.dmconcept.bob.client.models.Project

object ServerService {

  object Actions {
    final val STOP = "stop"
  }

}

class ServerService extends LocalService with IOIOLooperProvider {

  override implicit val loggerTag = LoggerTag("Bob ----------------")

  private final val helper_ = new IOIOAndroidApplicationHelper(this, this)

  private var started_ = false

  lazy val bobServer = new BobServer(p => playProject(p))

  var mProject: Project = null

  // The notification manager
  lazy val nm = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

  onCreate {

    helper_.create()

    // Start the helper
    startHelper()

    // Start the web server
    bobServer.start()

    // Start the IOIO helper
    val ips = getIPs().mkString(", ")

    info("server.ServerService.onCreate() Waiting for a request to launch the helper...")
    info(s"                                Listening on $ips")

    notification("IOIO available for remote", s"Available at $ips")

  }

  def playProject(project: Project) {

    toast(s"server.ServerService.playProject($project)")
    info(s"server.ServerService.playProject($project)")

    //TODO check config
    mProject = project

  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {

    super.onStartCommand(intent, flags, startId)

    toast(s"onStartCommand(intent=$intent, flags=$flags, startId=$startId")

    /*
    if (intent != null && intent.getAction() == Actions.STOP) {

      //TODO remove
      toast("onStartCommand() Received stop action from the notification")
      info("onStartCommand() Received stop action from the notification")

      // User clicked the notification. Need to stop the service.
      cancelNotifAndStop()

    }
    */

    Service.START_STICKY

  }

  private def cancelNotifAndStop() {
    toast("Cancel notif and stop")
    nm.cancel(0)
    stopSelf()
  }

  onDestroy {
    //TODO remove
    toast("server.BobService.onDestroy()")
    info("server.BobService.onDestroy()")
    stopHelper()
    helper_.destroy()
  }

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

  override def createIOIOLooper(connectionType: String, extra: Object) = new BaseIOIOLooper() {

    override def setup() {
      //TODO remove
      toast(s"BaseIOIOLooper.setup() mProject=$mProject")

      while (mProject == null) {
        Thread.sleep(200)
      }

      ioio_.openDigitalOutput(1)
    }

    override def loop() {

    }

    override def disconnected() {
      toast("BaseIOIOLooper.disconnected()")
      cancelNotifAndStop()
    }

    override def incompatible() {
      alert("Incompatible board", "This app need a board with at least the V5 firmware")
      cancelNotifAndStop()
    }

  }

  private def notification(title: String, message: String) {

    runOnUiThread {

      // Service starting. Create a notification.
      val notification = new Notification.Builder(this)
        .setSmallIcon(drawable.ic_launcher)
        .setTicker(title)
        .setContentTitle(title)
        .setContentText(message)
        /*
        .setContentIntent(
          // HACK: PendingIntent.FLAG_CANCEL_CURRENT needed to fix security permissions issues on KitKat as in
          // https://code.google.com/p/android/issues/detail?id=61850
          PendingIntent.getService(
            this, /*requestCode=*/0 , new Intent(Actions.STOP, null, this, this.getClass), PendingIntent.FLAG_CANCEL_CURRENT
          )
        )
        */
        .build()

      notification.flags |= Notification.FLAG_ONGOING_EVENT

      /*
      nm.notify(0, notification)
      */

      nm.notify(new Random().nextInt, notification)
    }

  }

  private def getIPs(): List[String] = {
    List("192.168.0.1", "192.168.0.2")
  }
}
