package com.protogenefactory.ioiomaster.server.activities

import java.net.NetworkInterface

import android.content.Intent
import android.hardware.usb.{UsbAccessory, UsbManager}
import android.view.{Gravity, WindowManager}
import com.protogenefactory.ioiomaster.server.services.ServerService
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.rtsp.RtspServer
import org.apache.http.conn.util.InetAddressUtils
import org.scaloid.common._

import scala.collection.JavaConverters._

class StatusActivity extends SActivity {

  override implicit val loggerTag = LoggerTag("Bob")

  final val STREAM_WIDTH   = 320
  final val STREAM_HEIGHT  = 240
  final val STREAM_FPS     = 15
  final val STREAM_BITRATE = 128 * 1024

  lazy val serverService  = new LocalServiceConnection[ServerService]

  lazy val mSurfaceView = new SurfaceView(this, null)

  onCreate {

    info(s"StatusActivity.onCreate() $intent")

    // Prevent the screen to lock
    getWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    intent.fold(throw new RuntimeException("StatusActivity launched without intent")) { i: Intent =>

      i.getAction match {

        case UsbManager.ACTION_USB_ACCESSORY_ATTACHED =>

          setVisible(false)

          // IOIO attached
          i.getExtras.get("accessory").asInstanceOf[UsbAccessory]
            .ensuring(_.getModel == "IOIO", s"Unexpected model: $intent")

          info("StatusActivity.onCreate() Asking the service to start the IOIO looper")

          serverService.run { s =>
            s.startIOIOLooper()
            // Don't show the activity, only the notification that the service creates
            finish()
          }

        case _ =>

          contentView = new SVerticalLayout {

            STextView("IOIO board connected")
              .gravity(Gravity.CENTER_HORIZONTAL)
              .textSize(24.sp)
              .padding(0, 0, 0, 32.dip) // left, top, right, bottom

            STextView(s"This device IP address is:\n${getIPs.mkString("\n")}")
              .gravity(Gravity.CENTER_HORIZONTAL)
              .textSize(18.sp)

            this += mSurfaceView
              .<<((STREAM_WIDTH / 2).dip, (STREAM_HEIGHT / 2).dip).>>

          }.gravity(Gravity.CENTER)

          /*
          // Configure the stream
          SessionBuilder.getInstance()
            // Surface view needed for now by libstreaming
            .setSurfaceView(mSurfaceView)
//            .setPreviewOrientation(90)
            .setContext(getApplicationContext)
            .setAudioEncoder(SessionBuilder.AUDIO_NONE)
            .setVideoEncoder(SessionBuilder.VIDEO_H264)
            .setVideoQuality(new VideoQuality(STREAM_WIDTH, STREAM_HEIGHT, STREAM_FPS, STREAM_BITRATE))

          // Start the streaming service
          startService[RtspServer]
          */

      }

    }

  }

  override def onNewIntent(intent: Intent) {
    info(s"StatusActivity.onNewIntent() intent=$intent")
    //toast(s"New intent\n$intent")
  }

  onStop {
    // Stop the streaming server
    stopService[RtspServer]
  }

  onDestroy {
    info("StatusActivity.onDestroy()")
  }

  private def getIPs: Seq[String] = {

    NetworkInterface.getNetworkInterfaces.asScala.toSeq.map { intf =>

      intf.getInetAddresses.asScala.toSeq
        .filterNot(addr => addr.isLoopbackAddress)
        .map(addr => addr.getHostAddress.toUpperCase)
        .filter{ sAddr => InetAddressUtils.isIPv4Address(sAddr) }
    }.flatten

  }


}
