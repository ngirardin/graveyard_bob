package fr.dmconcept.bob.server.activities

import org.scaloid.common._
import fr.dmconcept.bob.server.services.ServerService
import android.hardware.usb.{UsbAccessory, UsbManager}
import fr.dmconcept.bob.BobApplication

/**
 * This activity is started when the IOIO board is connected. Its only goal is to start the
 * {@link services.BobIOIOService}, cause an intent-filter can't be attached to a service.
 *
 */
class IOIOAttachedActivity extends SActivity {

  override implicit val loggerTag = LoggerTag("Bob")

  onCreate {

    // Avoid the flashing of the empty activity
    setVisible(false)

    info(s"IOIOAttachedActivity.onCreate() IOIO board connected, starting the service")

    intent
      .getOrElse(throw new RuntimeException("No intent"))

      .ensuring(_.getAction == UsbManager.ACTION_USB_ACCESSORY_ATTACHED, s"Unexpected intent action: $intent")

      .getExtras.get("accessory").asInstanceOf[UsbAccessory]

      .ensuring(_.getModel == "IOIO", s"Unexpected model: $intent")

    // Start the server service
    getApplication.asInstanceOf[BobApplication].startServerService()

    // And exit
    finish()

  }

}
