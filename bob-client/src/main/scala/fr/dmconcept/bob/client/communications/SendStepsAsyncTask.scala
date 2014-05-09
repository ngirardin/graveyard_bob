package fr.dmconcept.bob.client.communications

import SendStepsAsyncTask._
import android.os.AsyncTask
import android.util.Log
import fr.dmconcept.bob.client.communications.SendStepsAsyncTask.{SendStepResult, SendStepInput}
import fr.dmconcept.bob.client.models.json.BobJsonProtocol._
import fr.dmconcept.bob.client.models.{Step, BoardConfig}
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import spray.json._


object SendStepsAsyncTask {

  final val RESPONSE_OK = "BOB"

  def log(message: String) = Log.i("communication.SendSteps", message)

  case class SendStepInput(
    boardConfig : BoardConfig,
    steps       : Vector[Step]
  )

  case class SendStepResult(
    error: Option[String]
  )

}

case class SendStepsAsyncTask(

  host: String,

  port: Int

) extends AsyncTask[SendStepInput, Void, SendStepResult] {

  override def doInBackground(params: SendStepInput*): SendStepResult = {

    try {

        log("doInBackground() - Sending request...")
        sendRequest(params(0))
        log("doInBackground() - Request sent")

        SendStepResult(None)

    } catch {
      case e: Throwable =>
        log("doInBackground() - Request error: ${e.getMessage()}")
        SendStepResult(Option(e.getMessage))
    }

  }

  @throws [IOException]("if the network fails")
  def sendRequest(input: SendStepInput) {

    val serializedServoConfigs: String = input.boardConfig.toJson.compactPrint
    val serializedSteps       : String = input.steps.toJson.compactPrint

    //TODO put request in body
    val url = s"http://$host:$port/step?servoconfig=$serializedServoConfigs&steps=$serializedSteps"

    log(s"sendRequest($url)")

    val httpClient = new DefaultHttpClient()
    val httpPost   = new HttpPost(url)

    val response = httpClient.execute(httpPost)

    response.getStatusLine.getStatusCode match {

      case HttpStatus.SC_OK =>

        // TODO use scala iostream
        def readLine(response: HttpResponse) = new BufferedReader(
            new InputStreamReader(response.getEntity.getContent, "UTF-8")
          ).readLine

        val body = readLine(response)

        log(s"sendRequest() <- HTTP 200 - $body")

        if (!body.equals(RESPONSE_OK))
          throw new RuntimeException("Invalid response: $body")

      case status: Int =>
        throw new RuntimeException(s"HTTP $status")

    }

  }

}
