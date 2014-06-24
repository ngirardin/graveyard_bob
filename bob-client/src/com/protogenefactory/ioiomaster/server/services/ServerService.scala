package com.protogenefactory.ioiomaster.server.services

import java.util.{Date, Timer, TimerTask}

import android.app.{Notification, Service}
import android.content.Intent
import android.os.SystemClock
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.connections.Connection
import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project, ServoConfig}
import com.protogenefactory.ioiomaster.server.activities.StatusActivity
import com.protogenefactory.ioiomaster.server.services.ServerService._
import ioio.lib.api.Sequencer.{ChannelConfig, ChannelCue, ChannelCuePwmPosition}
import ioio.lib.api.{IOIO, DigitalOutput, Sequencer}
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

      //TODO check that project is already playing
      /*
      if (mProject.project == null) {
      */
        info(s"ServerService.playProject() project=[${project.id}] '${project.name}'")
        toast(s"ServerService.playProject() ${project.name}")
        mProject.setProject(project)
        mProject.notifyAll()
      /*
      } else {
        info(s"ServerService.playProject() project=[${project.id}] '${project.name}' Project already playing")
        toast("Project already playing!")
      }
      */

    }

  }

  override def playPosition(boardConfig: BoardConfig, positions: Array[Int]) {
    throw new NotImplementedError()
  }

  override def createIOIOLooper(connectionType: String, extra: Object) =
  //TODO LESS UGLY pleaaaaase
  if (connectionType == "ioio.lib.impl.SocketIOIOConnection") {
    // Don't create a looper for the socket connection
    info("ServerService.createIOIOLooper() Socket IOIO Connection, don't create the looper")
    null
  } else new BaseIOIOLooper() {


    final val CLK = Sequencer.Clock.CLK_2M;
    /* 0.5us periods */
    final val INITIAL = 3000;
    /* 1500 us * (1 / 0.5microseconds) */
    final val SLICE = PERIOD / 16;

    /* Slice duration in 16microseconds period */

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

    def startSequencerStateTimer() {
      new Timer().schedule(new TimerTask {
        override def run() {
          val sequencerState = sequencer_ match {
            case null => "null"
            case _    => s"${sequencer_.getLastEvent.`type`}, numCuesStarted=${sequencer_.getLastEvent.numCuesStarted}, available=${sequencer_.available()}"
          }
//          toast(s"Sequencer: $sequencerState")
          info(s"ServerSevice.SequencerWatcher state=$sequencerState")
        }
      }, 0, 500)
    }

    var sequencer_ : Sequencer = null

    info(s"ServerService.createIOIOLooper()")

    /*
    startIOIOConnectionStateTimer()
    startSequencerStateTimer()
    */

    val channelConfigs: Array[ChannelConfig] = ServoConfig.PERIPHERAL_PORTS.map(pin =>
      new Sequencer.ChannelConfigPwmPosition(CLK, PERIOD, INITIAL, new DigitalOutput.Spec(pin))
    ).toArray

    val channelCues : Array[ChannelCuePwmPosition] = ServoConfig.PERIPHERAL_PORTS.toArray.map(pin =>
      new Sequencer.ChannelCuePwmPosition()
    )

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

      //info(s"SequencerLooper.push() slices=$slices")

      channelCues.zipWithIndex.foreach { case (channel, i) =>

        if (i < slices.length) {

          val pulseWidth = slices(i)
          //info(s"SequencerLooper.push()   i=$i ${channel.pulseWidth} -> $pulseWidth")
          channel.pulseWidth = pulseWidth

        } else {
          //info(s"SequencerLooper.push()   i=$i ${channel.pulseWidth} -> default")
        }

      }

      val channelCuesArray = channelCues.map(_.asInstanceOf[ChannelCue])

      sequencer_.push(channelCuesArray, SLICE /* 20ms */); // Unit value is 16microseconds, 62500 = 1 s

      val now = SystemClock.elapsedRealtime()

      if (now - lastLog > 100) {
        lastLog = now
        info(s"ServerService.SequencerLooper.push() $currentSlice/${mProject.slices.length}")
      }

      currentSlice = currentSlice + 1

      if (currentSlice == mProject.sliceCount) {

        toast("Project playing done")

        info("ServerService.SequencerLooper.push() [1] Last slice, stopping sequencer")

        /*
        sequencer_.stop()

        info("ServerService.SequencerLooper.push() [2] Waiting for the sequencer to stop...")
        sequencer_.waitEventType(Sequencer.Event.Type.STOPPED)

        info("ServerService.SequencerLooper.push() [3] Sequencer stopper, waiting for a new project")
        */

        mProject.synchronized {

          mProject.wait()

          info(s"ServerService.SequencerLooper.push() [4] Got a new project: ${mProject.duration} ms, ${mProject.sliceCount} slices")
          currentSlice = 0

          info(s"ServerService.SequencerLooper.push() [5] Prefilling the sequencer (available=${sequencer_.available})...")

          /*
          while (true) {
            info(s"available: ${sequencer_.available}")
            Thread.sleep(500)
          }
          */
        /*
          while (sequencer_.available() > 0) {
            info(s"ServerService.SequencerLooper.push() [6]   prefilling (available ${sequencer_.available}...")
            push()
          }
          */

          info("ServerService.SequencerLooper.push() [7] Starting the sequencer - before")
          /*
          sequencer_.start()
          */
          info("ServerService.SequencerLooper.push() [8] Starting the sequencer - after")
        }
      }

    }

    override def disconnected() {

      info(s"ServerService.SequencerLooper.disconnected()")
      toast(s"ServerService.SerquencerLooper.disconnected()")

      stopHelper()
      stopForeground(true)

    }

    override def incompatible() {

      error("ServerService.SequencerLooper.incompatible()")
      alert("Incompatible board", "This app need a board with at least the V5 firmware")
      stopForeground(true)

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
