package fr.dmconcept.bob.client.communications

import android.content.Context
import fr.dmconcept.bob.client.models.{Project, BoardConfig, Step}
import android.accounts.NetworkErrorException
import BobCommunication._
import android.util.Log
import android.net.{NetworkInfo, ConnectivityManager}
import fr.dmconcept.bob.client.BobApplication
import fr.dmconcept.bob.client.communications.SendStepsAsyncTask.SendStepInput
import BobCommunication._

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

  context: Context

) {

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

    def isNetworkAvailable(): Boolean = Option(context
      .getSystemService(Context.CONNECTIVITY_SERVICE)
      .asInstanceOf[ConnectivityManager]
      .getActiveNetworkInfo
    ).map { ni : NetworkInfo =>
        ni.isConnected && (ni.getType == ConnectivityManager.TYPE_ETHERNET || ni.getType == ConnectivityManager.TYPE_WIFI)
    }.getOrElse(false)

    log(s"launchSendSteps($input)")

    assert(isNetworkAvailable(), "No network connection available")

    // Read the server IP from the preferences
    val serverIP = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
      .getString(BobApplication.PREFERENCES_SERVER_IP, "")
      .ensuring(_.nonEmpty, "Empty server IP address from preferences")

    SendStepsAsyncTask(serverIP, PORT)
      .execute(input)
      .get()
      .ensuring(_.error.isEmpty, "StepStepAsyncTask error")

  }

}
