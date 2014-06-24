package fr.dmconcept.bob.server;

import android.util.Log
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.client.models.Project
import fi.iki.elonen.NanoHTTPD
import org.json.JSONException
import BobServer._
import fi.iki.elonen.NanoHTTPD.{Response, Method, IHTTPSession}
import spray.json._

object BobServer {

  final val TAG = "Bob"

  final val PORT = BobApplication.Params.PORT

  final val ROOT_URL      = "/play"
  final val PING_URL      = "/ping"
  final val PROJECT_PARAM = "project"
  final val RESPONSE_OK   = "BOB"
  final val RESPONSE_NOT_FOUND = "Not found"

}

class BobServer(onProject: Project => Unit) extends NanoHTTPD(PORT) {

  Log.i("Bob", "BobServer()")

  override def serve(session: IHTTPSession): Response = {

    Log.i(TAG, s"serve() ${session.getMethod()} ${session.getUri()} ${session.getParms()}")

    session.getMethod() match {
      case Method.GET =>

        session.getUri match {

          case ROOT_URL =>

            val json = session.getParms().get(PROJECT_PARAM).parseJson

            try {

              val project = Project.deserialize(json)
              onProject(project)
              new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, RESPONSE_OK);

            } catch {
              case e: JSONException =>
                Log.e("BobServer", s"Can't deserialize the JSON: $json", e)
                new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Bad request")
            }

          case PING_URL =>
            new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, RESPONSE_OK);
        }

      case _ =>
        Log.e(TAG, s"BobServer.serve() Invalid request: ${session.getMethod()} ${session.getUri()} ${session.getParms()}")
        return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, RESPONSE_NOT_FOUND)

    }
  }

}
