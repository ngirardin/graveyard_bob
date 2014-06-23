package com.protogenefactory.ioiomaster.server.services

import android.app.{Notification, Service}
import android.content.Intent
import android.os.SystemClock
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.connections.Connection
import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project, ServoConfig}
import com.protogenefactory.ioiomaster.server.activities.StatusActivity
import com.protogenefactory.ioiomaster.server.services.ServerService._
import ioio.lib.api.Sequencer.{ChannelConfig, ChannelCue}
import ioio.lib.api.{DigitalOutput, Sequencer}
import ioio.lib.util.android.IOIOAndroidApplicationHelper
import ioio.lib.util.{BaseIOIOLooper, IOIOLooperProvider}
import org.scaloid.common._

import scala.util.Random

object ServerService {

  final val PERIOD = 20000 // microseconds (50hz = 0.02s = 20ms = 20.000us)
  final val MIN = 1000 * 2 // periods      (1000us * 0.5us periods)
  final val MAX = 2000 * 2 // periods      (2000us * 0.5us periods)
  final val PERCENT = ((MAX - MIN).toFloat / 100f).intValue()

}

class ServerService extends LocalService with IOIOLooperProvider with Connection {

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
  var mProject: ProjectLock = ProjectLock.empty

  onCreate {
    info(s"ServerService.onCreate()")
  }


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

  private def startHelper(/*intent: Intent*/) {

    info(s"ServerService.startHelper() started=$started_")

    if (!started_) {
      helper_.start()
      started_ = true
    } else {
      info(s"ServerService.startHelper() already started, restarting")
      /*
      if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
      */
      toast("ServerService.startHelper() Restaring helper")
      helper_.restart()
      /*
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


  override def playProject(project: Project) {

    mProject.synchronized {

      if (mProject.project == null) {
        info(s"ServerService.playProject() project=[${project.id}] '${project.name}'")
        toast(s"ServerService.playProject() project=[${project.id}] ${project.name}")
        mProject.setProject(project)
        mProject.notifyAll()
      } else {
        info(s"ServerService.playProject() project=[${project.id}] '${project.name}' Project already playing")
        toast("Project already playing!")
      }

    }

  }

  override def playPosition(boardConfig: BoardConfig, positions: Array[Int]) {
    throw new NotImplementedError()
  }

  override def createIOIOLooper(connectionType: String, extra: Object) = new BaseIOIOLooper() {


    final val CLK     = Sequencer.Clock.CLK_2M; /* 0.5us periods */
    final val INITIAL = 3000; /* 1500 us * (1 / 0.5microseconds) */
    final val SLICE   =  PERIOD / 16; /* Slice duration in 16microseconds period */

    var sequencer_ : Sequencer = null

    val channelConfigs: Array[ChannelConfig] = ServoConfig.PERIPHERAL_PORTS.map(pin =>
      new Sequencer.ChannelConfigPwmPosition(CLK, PERIOD, INITIAL, new DigitalOutput.Spec(pin))
    ).toArray

    val channelCues = ServoConfig.PERIPHERAL_PORTS.map(pin =>
      new Sequencer.ChannelCuePwmPosition()
    ).toArray

    override def setup() /*throws ConnectionLostException, InterruptedException*/ {

      info(s"SequencerLooper.setup() Openning ports ${ServoConfig.PERIPHERAL_PORTS}")

      sequencer_ = ioio_.openSequencer(channelConfigs)

      notification("IOIO connected", "Touch for more information")

      mProject.synchronized {
        info("SequencerLooper.setup() Waiting for a project")
        mProject.wait()
      }

      info("SequencerLooper.setup() Got a project, pre-filling the sequencer")

      sequencer_.waitEventType(Sequencer.Event.Type.STOPPED)
      while (sequencer_.available() > 0)
        push()

      info("SequencerLooper.setup() Pre-filling done, starting the sequencer")

      sequencer_.start()

    }

    override def loop() /* throws ConnectionLostException, InterruptedException */ {
      push()
    }

    var lastLog      = 0l
    var currentSlice = 0

    private def push() /* throws ConnectionLostException, InterruptedException */ {

      val slices = mProject.slices(currentSlice)

//      info(s"SequencerLooper.push() slices=$slices")

      val cues = channelCues.zipWithIndex.map { case (channel, i) =>

        if (i < slices.length) {

          val pulseWidth = slices(i)
//          info(s"SequencerLooper.push()   i=$i ${channel.pulseWidth} -> $pulseWidth")
          channel.pulseWidth = pulseWidth
          channel.asInstanceOf[ChannelCue]

        } else {
//          info(s"SequencerLooper.push()   i=$i ${channel.pulseWidth} -> default")
          channel.asInstanceOf[ChannelCue]
        }

      }

      sequencer_.push(cues, SLICE /* 20ms */); // Unit value is 16microseconds, 62500 = 1 s

      val now = SystemClock.elapsedRealtime()

      if (now - lastLog > 100) {
        lastLog = now
        info(s"SequencerLooper.push() $currentSlice/${mProject.slices}")
      }

      currentSlice = currentSlice + 1

      if (currentSlice == mProject.sliceCount) {

        sequencer_.stop()

        toast("Playing done")

        mProject.synchronized {

          mProject.project = null

          info("SequencerLooper.push() Sequence done, waiting for a new project")
          mProject.wait()

          info(s"SequencerLooper.push() Got a new project: ${mProject.duration} ms, ${mProject.sliceCount} slices")
          currentSlice = 0

          info("SequencerLooper.push() Wait for sequencer to stop...")
          sequencer_.waitEventType(Sequencer.Event.Type.STOPPED)

          info("SequencerLooper.push() Prefilling the sequencer...")
          while (sequencer_.available() > 0) {
            push()
          }

          info("SequencerLooper.push() Starting the sequencer - before")
          sequencer_.start()
          info("SequencerLooper.push() Starting the sequencer - after")
        }
      }

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
