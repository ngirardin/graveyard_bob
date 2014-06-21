package com.protogenefactory.ioiomaster.server.activities

import android.content.Intent
import android.hardware.usb.{UsbAccessory, UsbManager}
import com.protogenefactory.ioiomaster.server.services.ServerService
import org.scaloid.common._

class StatusActivity extends SActivity {

  override implicit val loggerTag = LoggerTag("Bob")

  lazy val serverService  = new LocalServiceConnection[ServerService]

  lazy val textViewStatus = new STextView("-------")

  onCreate {

    info(s"StatusActivity.onCreate() $intent")

    intent.fold(throw new RuntimeException("StatusActivity launched without intent")) { i: Intent =>

      i.getAction match {

        case UsbManager.ACTION_USB_ACCESSORY_ATTACHED =>

          setVisible(false)

          // IOIO attached
          i.getExtras.get("accessory").asInstanceOf[UsbAccessory]
            .ensuring(_.getModel == "IOIO", s"Unexpected model: $intent")

          info("StatusActivity.onCreate() #################### Asking the service to start the IOIO looper")

          serverService.run { s =>
            toast("IOIO Connected, start looper")
            s.startIOIOLooper()
            // Don't show the activity, only the notification that the service creates
            finish()
          }

        case _ =>

          toast(s"From notification")

          serverService.run { s =>
            textViewStatus.text(s.getIOIOStatus)
          }

          contentView = new SVerticalLayout {
            STextView("The IOIO board is connected.")
            STextView(s"Start the IOIO Master Control or connect remotely using the IP address: ${getIPs.mkString(" or ")}")
            this += textViewStatus
          }


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

  private def getIPs(): List[String] = {
    //TODO use real IP
    List("192.168.0.1", "192.168.0.2")
  }

}
