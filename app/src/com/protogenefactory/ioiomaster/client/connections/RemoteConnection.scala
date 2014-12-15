package com.protogenefactory.ioiomaster.client.connections

import java.net.{ConnectException, URLEncoder}

import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.client.models.Project
import com.protogenefactory.ioiomaster.client.models.json.BobJsonProtocol._
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.scaloid.common._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.io.Source

case class RemoteConnection(remoteIP: String) extends Connection with TagUtil {

  implicit override val loggerTag = LoggerTag("Bob")

  final val rootUrl = s"http://$remoteIP:${BobApplication.Params.PORT}"

  def playProject(project: Project) {

    info(s"RemoteConnection.playProject() project=${project.id}")

    /*
    if (!isNetworkAvailable)
      alert("No network connection", "You need to be connected on a local network to play the project")
    */

    Future {
      sendRequest(project)
    }

  }

  def playStep(project: Project, stepIndex: Int) {

    info(s"RemoteConnection.playPosition() project=${project.id}, $stepIndex=$stepIndex)")

    /*
    if (!isNetworkAvailable)
      alert("No network connection", "You need to be connected on a local network to play the project")
    */

    Future {
      sendRequest(project, Some(stepIndex))
    }

  }

  private def sendRequest(project: Project, stepIndex:Option[Int] = None) {

    val json = project.toJson.compactPrint

    val params  = s"project=" + URLEncoder.encode(json, "UTF-8") + stepIndex.fold(""){ s => s"&step=$s" }

    val url = s"$rootUrl/play?$params"

    info(s"RemoteConnection.sendRequest() > url=$url")

    val httpClient = new DefaultHttpClient()
    val response   = httpClient.execute(new HttpGet(url))

    response.getStatusLine.getStatusCode match {

      case HttpStatus.SC_OK =>

        val body: String = Source.fromInputStream(response.getEntity.getContent).mkString

        info(s"RemoteConnection.sendRequest() < body = $body")

        if (body != "BOB") {
          throw new RuntimeException("Invalid response: $body")
        }

      case status: Int =>
        throw new RuntimeException(s"HTTP $status")

    }

  }

  override def ping(): Future[Boolean] = {

    Future {

      val url = s"$rootUrl/ping"

      info(s"RemoteConnection.ping() Ping URL=$url")

      try {
        val httpClient = new DefaultHttpClient()
        val response = httpClient.execute(new HttpGet(url))

        info(s"RemoteConnection.ping() Response=${response.getStatusLine.getStatusCode}")
        response.getStatusLine.getStatusCode match {
          case HttpStatus.SC_OK => true
          case _                => false
        }

      } catch {
        case e: ConnectException =>
          info(s"RemoteConnection.ping() Timeout")
          false
      }

    }

  }

}
