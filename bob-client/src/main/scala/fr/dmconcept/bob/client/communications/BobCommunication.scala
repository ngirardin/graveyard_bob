package fr.dmconcept.bob.client.communications

import android.app.{ProgressDialog, Activity}
import android.content.DialogInterface.OnClickListener
import android.content.{DialogInterface, Context}
import android.net.{NetworkInfo, ConnectivityManager}
import fr.dmconcept.bob.client.BobApplication
import fr.dmconcept.bob.client.activities.ProjectActivity
import fr.dmconcept.bob.client.models.Project
import java.io.{InputStreamReader, BufferedReader}
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.{HttpResponse, HttpStatus}
import org.scaloid.common._
import spray.json._

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

    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Future

    val json = project.toJson.compactPrint

    def sendRequest() {

      //TODO put request in body
      val url = s"http://$serverIP:${BobApplication.Params.PORT}/play"

      info(s"BobCommunication.send() url=$url")

      val httpClient = new DefaultHttpClient()
      val httpPost   = new HttpPost(url)

      httpPost.setEntity(new UrlEncodedFormEntity({
        val l = new java.util.ArrayList[BasicNameValuePair]()
        l.add(new BasicNameValuePair("project", json))
        l
      }))

      val response = httpClient.execute(httpPost)

      response.getStatusLine.getStatusCode match {

        case HttpStatus.SC_OK =>

          // TODO use scala iostream
          def readLine(response: HttpResponse) = new BufferedReader(
            new InputStreamReader(response.getEntity.getContent, "UTF-8")
          ).readLine

          val body: String = readLine(response)

          info("BobCommunication.send() body = ")

          if (!body.equals("BOB"))
            throw new RuntimeException("Invalid response: $body")

        case status: Int =>
          throw new RuntimeException(s"HTTP $status")

      }

    }

    val progress = new ProgressDialog(context)
    progress.setTitle("Playing the project...")
    progress.setMessage("Touch outside to cancel")
    progress.setIndeterminate(true)
    progress.setCancelable(true)
    progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = dialog.cancel()
    })
    progress.setOnCancelListener(new DialogInterface.OnCancelListener {
      override def onCancel(dialog: DialogInterface): Unit = {
        dialog.cancel()
        //TODO cancel the playing
        alert("TODO","cancel")
      }
    })
    progress.show()

    Future {
      sendRequest()
      runOnUiThread(progress.dismiss())
    }

  }

}
