package com.protogenefactory.ioiomaster.client.connections

import java.io.{BufferedReader, InputStreamReader}
import java.net.URLEncoder

import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.client.models.json.BobJsonProtocol._
import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.{HttpResponse, HttpStatus}
import org.scaloid.common._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class RemoteConnection(remoteIP: String) extends Connection with TagUtil {

  implicit override val loggerTag = LoggerTag("Bob")

  /*
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
  */

  def playProject(project: Project) {

    info(s"RemoteConnection.playProject(${project.id})")

    /*
    if (!isNetworkAvailable)
      alert("No network connection", "You need to be connected on a local network to play the project")
    */

    val json = project.toJson.compactPrint

    def sendRequest() {

      val rootUrl = s"http://$remoteIP:${BobApplication.Params.PORT}/play"
      val params  = s"project=" + URLEncoder.encode(json, "UTF-8")

      val url = s"$rootUrl?$params"

      info(s"RemoteConnection.sendRequest() > url=$url")

      val httpClient = new DefaultHttpClient()
      val response   = httpClient.execute(new HttpGet(url))

      response.getStatusLine.getStatusCode match {

        case HttpStatus.SC_OK =>

          // TODO use scala iostream
          def readLine(response: HttpResponse) = new BufferedReader(
            new InputStreamReader(response.getEntity.getContent, "UTF-8")
          ).readLine

          val body: String = readLine(response)

          info(s"RemoteConnection.sendRequest() < body = $body")

          if (body != "BOB") {
            throw new RuntimeException("Invalid response: $body")
          }

        case status: Int =>
          throw new RuntimeException(s"HTTP $status")

      }

    }

    Future {
      info("RemoteConnection.sendRequest() -- Before sending request")
        sendRequest()
        info("RemoteConnection.sendRequest() -- After sending request")
        info("RemoteConnection.sendRequest() -- Dialog dismissed")
    }

  }

  def playPosition(boardConfig: BoardConfig, positions: Array[Int]) {

    info(s"RemoteConnection.playPosition() boardConfig=$boardConfig, positions=$positions")

    //TODO play position
    throw new RuntimeException("TODO play position")

  }

}
