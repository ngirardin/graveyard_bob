package com.protogenefactory.ioiomaster.server.activities

import java.net.NetworkInterface

import android.content.Intent
import android.hardware.usb.{UsbAccessory, UsbManager}
import android.view.Gravity
import com.protogenefactory.ioiomaster.server.services.ServerService
import org.apache.http.conn.util.InetAddressUtils
import org.scaloid.common._

import scala.collection.JavaConverters._

class StatusActivity extends SActivity {

  override implicit val loggerTag = LoggerTag("Bob")

  lazy val serverService  = new LocalServiceConnection[ServerService]

  onCreate {

    info(s"StatusActivity.onCreate() $intent")

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

            STextView(s"Remote connection IP\n${getIPs.mkString("\n")}")
              .gravity(Gravity.CENTER_HORIZONTAL)
              .textSize(18.sp)

          }.gravity(Gravity.CENTER)

      }

    }

  }

  override def onNewIntent(intent: Intent) {
    info(s"StatusActivity.onNewIntent() intent=$intent")
    toast(s"New intent\n$intent")
  }

  onDestroy {
    info("StatusActivity.onDestroy()")
  }

  private def getIPs(): Seq[String] = {

    NetworkInterface.getNetworkInterfaces().asScala.toSeq.map { intf =>

      intf.getInetAddresses().asScala.toSeq
        .filterNot(addr => addr.isLoopbackAddress)
        .map(addr => addr.getHostAddress().toUpperCase)
        .filter{ sAddr => InetAddressUtils.isIPv4Address(sAddr) }
    }.flatten

  }


}
