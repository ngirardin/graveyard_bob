package fr.dmconcept.bob.client.communications

import BobCommunication._
import android.content.Context
import android.net.{NetworkInfo, ConnectivityManager}
import android.util.Log
import fr.dmconcept.bob.client.BobApplication
import fr.dmconcept.bob.client.communications.SendStepsAsyncTask.SendStepInput
import fr.dmconcept.bob.client.models.{Project, BoardConfig, Step}
import org.scaloid.common._


/**
 *
 * This class handles all the communication with the Bob Server
 *
 */
object BobCommunication {

  final val PORT = 8000

  def log(message: String) = Log.i("communications.BobCommunication", message)

}

case class BobCommunication(

  c: Context

) {

  implicit val context = c

  def sendStep(boardConfig: BoardConfig, step: Step)  {

    log(s"sendStep($boardConfig, $step)")

    val steps = Vector(step)

    launchSendSteps(SendStepInput(boardConfig, steps))

  }

  def sendSteps(boardConfig: BoardConfig, startStep: Step, endStep: Step) {

    log(s"sendSteps($boardConfig, $startStep, $endStep)")

    val steps = Vector(startStep, endStep)

    launchSendSteps(SendStepInput(boardConfig, steps))

  }

  def sendSteps(project: Project) {

    log(s"sendSteps($project)")

    launchSendSteps(SendStepInput(project.boardConfig, project.steps))

  }

  private def launchSendSteps(input: SendStepInput) {

    def isNetworkAvailable: Boolean = Option(context
      .getSystemService(Context.CONNECTIVITY_SERVICE)
      .asInstanceOf[ConnectivityManager]
      .getActiveNetworkInfo
    ).fold(false) {ni: NetworkInfo =>
      ni.isConnected && (
        ni.getType == ConnectivityManager.TYPE_ETHERNET ||
        ni.getType == ConnectivityManager.TYPE_WIFI
      )
    }

    log(s"launchSendSteps($input)")

    assert(isNetworkAvailable, "No network connection available")

    // Read the server IP from the preferences
    defaultSharedPreferences.getString(BobApplication.Preferences.SERVER_IP, "") match {
      //TODO disable playing is empty server ip
      case "" => throw new RuntimeException("Empty server IP preferences empty")
      case ip => SendStepsAsyncTask(ip, PORT)
        .execute(input)
        .get()
        .ensuring(_.error.isEmpty, "StepStepAsyncTask error")
    }

  }

}
