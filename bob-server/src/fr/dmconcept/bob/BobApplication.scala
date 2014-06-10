package fr.dmconcept.bob

import fr.dmconcept.bob.server.services.ServerService
import android.app.Application
import org.scaloid.common._

object BobApplication {

}

class BobApplication extends Application with SContext {

  def startServerService() {

    info("BobApplication.startServerService()")

    startService[ServerService]

  }

}
