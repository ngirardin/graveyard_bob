package com.protogenefactory.ioiomaster.client.connections

import java.io.{BufferedReader, InputStreamReader}
import java.net.{ConnectException, URLEncoder}

import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.client.models.json.BobJsonProtocol._
import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.{HttpResponse, HttpStatus}
import org.scaloid.common._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

case class RemoteConnection(remoteIP: String) extends Connection with TagUtil {

  implicit override val loggerTag = LoggerTag("Bob")

  final val rootUrl = s"http://$remoteIP:${BobApplication.Params.PORT}"

  def playProject(project: Project) {

    info(s"RemoteConnection.playProject(${project.id})")

    /*
    if (!isNetworkAvailable)
      alert("No network connection", "You need to be connected on a local network to play the project")
    */

    val json = project.toJson.compactPrint

    def sendRequest() {

      val params  = s"project=" + URLEncoder.encode(json, "UTF-8")

      val url = s"$rootUrl/play?$params"

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
      sendRequest()
    }

  }

  //TODO implement play position
  def playPosition(boardConfig: BoardConfig, positions: Array[Int]) {
    throw new NotImplementedError()
  }

  override def ping(): Boolean = {

    import scala.concurrent.duration._

    val result = Await.result[Boolean]({
      Future {
        val url = s"$rootUrl/ping"

        info(s"RemoteConnection.ping() Ping URL=$url")

        try {
          val httpClient = new DefaultHttpClient()
          val response = httpClient.execute(new HttpGet(url))

          info(s"RemoteConnection.ping() Response=${response.getStatusLine.getStatusCode}")
          response.getStatusLine.getStatusCode match {
            case HttpStatus.SC_OK => true
            case _ => false
          }
        } catch {
          case e: ConnectException => false
        }
      }
    }, 5.seconds)

    info(s"RemoteConnection.ping() result=$result")
    result

  }

  override def getSounds: Seq[String] = ???

}
