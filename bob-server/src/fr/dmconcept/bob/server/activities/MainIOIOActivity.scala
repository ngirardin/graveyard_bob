package fr.dmconcept.bob.server.activities

import ioio.lib.util.android.IOIOActivity
import android.widget.TextView
import java.util
import fr.dmconcept.bob.server.{R, BobServer}
import fr.dmconcept.bob.server.ioio.ProjectLock
import android.os.{SystemClock, Bundle}
import android.view.View
import android.util.Log
import MainIOIOActivity._
import android.app.AlertDialog
import android.content.{Intent, DialogInterface}
import java.io.IOException
import java.net.{SocketException, InetAddress, NetworkInterface}
import java.util.Collections
import org.apache.http.conn.util.InetAddressUtils
import android.hardware.usb.{UsbAccessory, UsbManager}
import ioio.lib.util.{BaseIOIOLooper, IOIOLooper}
import ioio.lib.api.{DigitalOutput, Sequencer}
import fr.dmconcept.bob.client.models.{ServoConfig, Project}
import collection.JavaConverters._

object MainIOIOActivity {

  final val TAG = "BobServer ------------------- "

  final val PERIOD  = 20000 /* microseconds */ /* 50hz = 0.02s = 20ms = 20.000us */
  final val MIN     = 1000 * 2 /* periods */   /* 1000us * 0.5us periods         */
  final val MAX     = 2000 * 2 /* periods */   /* 2000us * 0.5us periods         */
  final val PERCENT = ((MAX - MIN).toFloat / 100f).intValue()

}

class MainIOIOActivity extends IOIOActivity {

  var textViewIP  : TextView = null
  var textViewLog : TextView = null

  var ips: util.ArrayList[String] = null
  var mBobServer: BobServer = null

  val mProject = new ProjectLock()

  override def onCreate(savedInstanceState: Bundle) {

    super.onCreate(savedInstanceState)

    setContentView(R.layout.main_ioio_activity)

    textViewIP  = findViewById(R.id.textViewIP).asInstanceOf[TextView]
    textViewLog = findViewById(R.id.textViewLog).asInstanceOf[TextView]

    info("onCreate()")

    findViewById(R.id.buttonQuit).setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View): Unit = finish()
    })

    // Get the device IP addresses
    ips = getIPs()

    // Display an error message and exit the app if the device has no IP address
    if (ips.isEmpty()) {
      runOnUiThread(new Runnable() {

        override def run() {

          new AlertDialog.Builder(MainIOIOActivity.this)
            .setTitle("No network connection")
            .setMessage("The app needs a Wifi or Ethernet network connection")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              override def onClick(dialog: DialogInterface, wich: Int): Unit = finish()
            })
          .show()
        }
      })

      return;

    }

    /*
    mBobServer = new BobServer(new BobServer.BobServerListener() {

      override def onServerStarted() {

        // Display the IP addresses
        textViewIP.setText("");

        ips.asScala.foreach(ip =>
          textViewIP.setText(s"${textViewIP.getText} $ip\n")
        )

      }

      override def onCantStart(exception: IOException) {

        runOnUiThread(new Runnable() {

          override def run() {
            new AlertDialog.Builder(MainIOIOActivity.this)
              .setTitle("Application already running")
              .setMessage("You can only run one instance of the application")
              .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                override def onClick(dialog: DialogInterface, which: Int): Unit = finish()
              })
              .show()
          }
        })

      }

      override def onPlayRequest(project: Project) {

        info(s"onPlayRequest($project.id}")

        /*
        synchronized (mProject) {

          if (mProject.getProject() != null && !mProject.getProject().boardConfig.id.equals(project.boardConfig.id)) {

            runOnUiThread(new Runnable() {
              override def run {
                new AlertDialog.Builder(MainIOIOActivity.this)
                  .setTitle("Reconnect the board")
                  .setMessage("The project played has a different board configuration, you need to restart the app")
                  .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    override def onClick(dialog: DialogInterface, which: Int): Unit = finish()
                  })
                  .show()
              }
            })

            return
          }

          mProject.setProject(project)
          info(s"Project ${mProject.getProject().name} duration=${mProject.getDuration()} ms, slices=${mProject.getSliceCount()}")
          mProject.notifyAll()
        }
        */
      }

    })
  */

  }

  private def info(message: String) {

    Log.i(TAG, message)

    runOnUiThread(new Runnable() {
      override def run = textViewLog.setText(textViewLog.getText() + message + "\n")
    })

  }

  override def onNewIntent(intent: Intent) {

    super.onNewIntent(intent)

    info("MainActivity.onNewIntent(" + intent + ")")

    val extras = intent.getExtras()

    if (!intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)) {
      Log.i(TAG, "MainActivity.onNewIntent() - Not an ACTION_USB_ACCESSORY_ATTACHED action")
      return
    }

    val usbAccessory = extras.get("accessory").asInstanceOf[UsbAccessory]

    if (!usbAccessory.getModel().equals("IOIO")) {
      info(s"MainActivity.onNewIntent() - $usbAccessory don't match the IOIO model")
      return
    }

    //TODO better newIntent handling
    createIOIOLooper()

  }
override def createIOIOLooper: IOIOLooper = return new SequencerLooper()

  object SequencerLooper {
    final val CLK     = Sequencer.Clock.CLK_2M; /* 0.5us periods */
    final val INITIAL = 3000; /* 1500 us * (1 / 0.5microseconds) */
    final val SLICE   =  PERIOD / 16; /* Slice duration in 16microseconds period */
  }

  private class SequencerLooper extends BaseIOIOLooper {

    import SequencerLooper._

    var sequencer_ : Sequencer = null

    val channelCues = new util.ArrayList[Sequencer.ChannelCuePwmPosition]()

    override def setup() /*throws ConnectionLostException, InterruptedException*/ {

      mProject.synchronized {
        info("SequencerLooper.setup() Waiting for a project")
        mProject.wait()
      }

      info(s"SequencerLooper.setup() Setting config ${mProject.project.boardConfig.name}")

      val channelConfigs = new util.ArrayList[Sequencer.ChannelConfigPwmPosition]()

      for (sc: ServoConfig <- mProject.project.boardConfig.servoConfigs) {

        // Create the channel configs
        channelConfigs.add(
          new Sequencer.ChannelConfigPwmPosition(CLK, PERIOD, INITIAL, new DigitalOutput.Spec(sc.pin))
        )

        // Create the cues
        channelCues.add(new Sequencer.ChannelCuePwmPosition())

      }

      sequencer_ = ioio_.openSequencer(channelConfigs.toArray(new Array[Sequencer.ChannelConfig](channelConfigs.size)))

      // Pre-fill.
      sequencer_.waitEventType(Sequencer.Event.Type.STOPPED)
      while (sequencer_.available() > 0) {
        push()
      }

      info("SequencerLooper.start()")

      sequencer_.start()
    }

    override def loop() /* throws ConnectionLostException, InterruptedException */ {
      push()
    }

    var lastLog      = 0l
    var currentSlice = 0

    private def push() /* throws ConnectionLostException, InterruptedException */ {

      val slices = mProject.slices.get(currentSlice)

      for (i <- 0 to channelCues.size())
        channelCues.get(i).pulseWidth = slices.get(i)

      val c = channelCues.toArray(new Array[Sequencer.ChannelCue](channelCues.size))
      sequencer_.push(c, SLICE /* 20ms */); // Unit value is 16microseconds, 62500 = 1 s

      /*
      led1Cue.value = new Random().nextBoolean();
      */

      val now = SystemClock.elapsedRealtime()

      if (now - lastLog > 500) {
        lastLog = now
        Log.i(TAG, "#" + currentSlice + " " + slices)
      }

      currentSlice = currentSlice + 1

      if (currentSlice == mProject.sliceCount) {

        sequencer_.stop()

        mProject.synchronized {
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

          info("SequencerLooper.push() Starting the sequencer")
          sequencer_.start()
        }
      }

    }

    override def disconnected(): Unit = super.disconnected()

    override def incompatible(): Unit = super.incompatible()

  }

  override def onStart() {
    super.onStart()
    info("onStart")
  }

  override def onStop() {
    info("onStop")
    mBobServer.stop()

    super.onStop()
  }

  private def getIPs() : util.ArrayList[String] = {

    val ips = new util.ArrayList[String]()

    try {

      NetworkInterface.getNetworkInterfaces().asScala.foreach { intf =>

        val addrs = Collections.list(intf.getInetAddresses())

        addrs.asScala.foreach { addr =>

          if (!addr.isLoopbackAddress()) {

            val sAddr = addr.getHostAddress().toUpperCase()
            val isIPv4 = InetAddressUtils.isIPv4Address(sAddr)

            if (isIPv4)
              ips.add(sAddr)
          }
        }
      }

    } catch {
      case e: SocketException => {}
        // Do notihgn
    }

    ips

  }

}
