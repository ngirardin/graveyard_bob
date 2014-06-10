package com.protogenefactory.ioiomaster.client.communications

import android.app.Activity
import android.content.{DialogInterface, Context}
import android.net.{NetworkInfo, ConnectivityManager}
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.client.activities.ProjectActivity
import com.protogenefactory.ioiomaster.client.models.Project
import java.io.{InputStreamReader, BufferedReader}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.{HttpResponse, HttpStatus}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.scaloid.common._
import spray.json._
import com.protogenefactory.ioiomaster.client.models.json.BobJsonProtocol._
import org.apache.http.params.HttpParams
import java.net.URLEncoder

case class BobCommunication(projectActivity: ProjectActivity) extends TagUtil {

  implicit override val loggerTag = LoggerTag("BobClient")

  implicit val context: Activity = projectActivity

  private def isNetworkAvailable: Boolean = Option(context
    .getSystemService(Context.CONNECTIVITY_SERVICE)
    .asInstanceOf[ConnectivityManager]
    .getActiveNetworkInfo
  ).fold(false) {ni: NetworkInfo =>
    ni.isConnected && (
      ni.getType == ConnectivityManager.TYPE_ETHERNET ||
        ni.getType == ConnectivityManager.TYPE_WIFI
      )
  }

  def send(serverIP: String, project: Project) {

    info(s"BobCommunication.send(${project.id})")

    if (!isNetworkAvailable)
      alert("No network connection", "You need to be connected on a local network to play the project")

    val json = project.toJson.compactPrint

    def sendRequest() {

      //TODO put request in body
      val rootUrl = s"http://$serverIP:${BobApplication.Params.PORT}/play"
      val params  = s"project=" + URLEncoder.encode(json, "UTF-8")

      val url = s"$rootUrl?$params"

      info(s"BobCommunication.send() > url=$url")

      val httpClient = new DefaultHttpClient()
      val response   = httpClient.execute(new HttpGet(url))

      response.getStatusLine.getStatusCode match {

        case HttpStatus.SC_OK =>

          // TODO use scala iostream
          def readLine(response: HttpResponse) = new BufferedReader(
            new InputStreamReader(response.getEntity.getContent, "UTF-8")
          ).readLine

          val body: String = readLine(response)

          info(s"BobCommunication.send() < body = $body")

          if (body != "BOB") {
            error("Invalid response")
            throw new RuntimeException("Invalid response: $body")
          }

        case status: Int =>
          throw new RuntimeException(s"HTTP $status")

      }

    }

    Future {
      info("BobCommunication.send() -- Before sending request")
        sendRequest()
        info("BobCommunication.send() -- After sending request")
        info("BobCommunication.send() -- Dialog dismissed")
    }

  }

}
