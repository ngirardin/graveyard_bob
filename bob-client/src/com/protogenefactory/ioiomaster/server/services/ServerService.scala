package com.protogenefactory.ioiomaster.server.services

import java.net.BindException

import android.app.{Notification, Service}
import android.content.Intent
import android.os.Environment
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.connections.Playable
import com.protogenefactory.ioiomaster.client.models.{Project, ServoConfig}
import com.protogenefactory.ioiomaster.server.BobServer
import com.protogenefactory.ioiomaster.server.activities.StatusActivity
import com.protogenefactory.ioiomaster.server.services.ServerService._
import ioio.lib.api.PwmOutput
import ioio.lib.util.android.IOIOAndroidApplicationHelper
import ioio.lib.util.{BaseIOIOLooper, IOIOLooperProvider}
import org.scaloid.common._

import scala.util.Random

object ServerService {

  /**
   * The slice duration at which the servo position is played
   */
  final val PERIOD : Int = (1f / ServoConfig.FREQUENCY * 1000 * 2).toInt // 20ms * 2 = 40ms

}

class ServerService extends LocalService with IOIOLooperProvider with Playable {

  override implicit val loggerTag = LoggerTag("Bob")

  /**
   * Manage the connection to the IOIO Board
   */
  private final val helper_ = new IOIOAndroidApplicationHelper(this, this)

  /**
   * Represents the IOIO connection state
   */
  private var started_ = false

  lazy val httpServer = new BobServer(this)

  /**
   * The project lock storing the slices
   */
  var mProject = new ProjectLock(PERIOD)

  onCreate {
    info(s"ServerService.onCreate()")
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {

    super.onStartCommand(intent, flags, startId)

    info(s"ServerService.onStartCommand() intent=$intent, startId=$startId")

    // Don't stop on application exit
    Service.START_STICKY

  }

  /**
   *
   * Create the "IOIO connected" notification and start the looper
   */
  def startIOIOLooper() {

    info("ServerService.startIOIOLooper()")
    helper_.create()
    startHelper()

  }

  def isIOIOStarted: Boolean = {
    info(s"ServerService.getIOIOStatus() connected=$started_")
    started_
  }

  private def startHelper(/*intent: Intent*/) {

    info(s"ServerService.startHelper() started=$started_")

    if (!started_) {
      helper_.start()
      started_ = true
    } else {
      info(s"ServerService.startHelper() already started, restarting")
      toast("IOIO board reconnected")
      helper_.restart()
    }

  }

  private def stopHelper() {

    if (started_) {
      helper_.stop()
      started_ = false
    }

  }

  override def playProject(project: Project) {

    mProject.synchronized {

      if (!mProject.hasProject) {
        info(s"ServerService.playProject() project=[${project.id}] '${project.name}'")
        mProject.setProject(project)
        mProject.notifyAll()
      } else {
        info(s"ServerService.playProject() project=[${project.id}] '${project.name}' Project already playing")
        toast("Project already playing!")
      }

    }

  }

  override def playStep(project: Project, stepIndex: Int) {

    mProject.synchronized {

      if (!mProject.hasProject) {
        info(s"ServerService.playPosition() project=[${project.id}] '${project.name}' step: $stepIndex")
        mProject.setProject(project.copy(steps = project.steps.slice(stepIndex, stepIndex + 1)))
        mProject.notifyAll()
      } else {
        info(s"ServerService.playPosition() project=[${project.id}] '${project.name}' Project already playing")
        toast("Project already playing!")
      }

    }

  }

  override def getSounds: Seq[String] = {
    Seq("TODO", s"${Environment.getExternalStorageDirectory}/Ringtones", "TODO")
  }

  override def createIOIOLooper(connectionType: String, extra: Object) = new BaseIOIOLooper() {

    /*
    def startIOIOConnectionStateTimer() {
      new Timer().schedule(new TimerTask() {
        override def run() {
          if (ioio_.getState == IOIO.State.DEAD) {
            info("ServerService.DeadTimer IOIO disconnected")
            toast("IOIO board disconnected")
            this.cancel()
            stopForeground(true)
          } else {
            toast(s"IOIO: ${ioio_.getState}")
          }
        }
      }, 0, 10 * 1000)
    }
    */

    info(s"ServerService.createIOIOLooper() $connectionType")

    var pins: Seq[PwmOutput] = null

    /**
     * Called when the IOIO board is connected. Open all the PWM ports
     */
    override def setup() {

      throwExceptionOnMainThread {

        info(s"SequencerLooper.setup() Openning the ports")

        pins = ServoConfig.PERIPHERAL_PORTS.map(pin =>
          ioio_.openPwmOutput(pin, ServoConfig.FREQUENCY)
        )

        try {
          info(s"SequenceLooper.setup() Starting HTTP server")
          httpServer.start()
        } catch {
          case be: BindException =>
            error("Address already in use")
            stopSelf()
        }

        info(s"SequenceLooper.setup() Displaying notification")

        // Display the app notification
        showNotification()

      }

    }

    var lastLog = 0

    override def loop() {

      throwExceptionOnMainThread {

        mProject.synchronized {

          // Wait for a project
          if (mProject.hasProject) {

            ioio_.beginBatch()

            mProject.pinsIndexes.zip(mProject.getSlice).foreach { case (pinIndex, slice) =>
                //info(s"Pin: $pinIndex, slice: $slice")
                pins(pinIndex).setPulseWidth(slice)
            }

            ioio_.endBatch()
          } else {

            if (lastLog > 10) {
              lastLog = 0
              info("ServerService.SequencerLooper.loop() no project")
              lastLog = lastLog + 1
            }

          }
        }

        // Sleep till the next cycle
        Thread.sleep(PERIOD)

      }

    }

    override def disconnected() {

      info(s"ServerService.SequencerLooper.disconnected()")
      toast(s"ServerService.SerquencerLooper.disconnected()")

      throwExceptionOnMainThread {
        info("ServerServer.SequencerLooper.disconnected() Stopping HTTP server")
        httpServer.stop()

        info("ServerServer.SequencerLooper.disconnected() Stopping helper")
        stopHelper()

        info("ServerServer.SequencerLooper.disconnected() Stopping service")
        stopForeground(true)
      }

    }

    override def incompatible() {

      error("ServerService.SequencerLooper.incompatible()")
      alert("Incompatible board", "This app need a board with at least the V5 firmware")
      stopForeground(true)

    }

  }

  private def showNotification() {

    runOnUiThread {

      // Service starting. Create a notification.
      val notification = new Notification.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setTicker("IOIO connected")
        .setContentTitle("IOIO connected")
        .setContentText("Touch for more information")
        .setContentIntent(pendingActivity[StatusActivity])
        .build()

      startForeground(new Random().nextInt(), notification)

    }

  }

  /**
   * Catch the exceptions that the code block may produce and forward
   * them to the UI thread to make the app crash instead of throwing
   * a silent UncaughtException
   *
   * @param code The code block on which to catch exceptions
   */
  private def throwExceptionOnMainThread(code: => Unit): Unit= {
    try {
      code
    } catch {
      case e : Throwable => runOnUiThread(throw e)
    }
  }

}
