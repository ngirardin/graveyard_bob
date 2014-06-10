package fr.dmconcept.bob.server;

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONException
import BobServer._
import fi.iki.elonen.NanoHTTPD.{Response, Method, IHTTPSession}
import fr.dmconcept.bob.client.models.Project
import spray.json._

object BobServer {

  final val TAG = "Bob"

  final val PORT = 8000

  final val ROOT_URL      = "/play"
  final val PROJECT_PARAM = "project"
  final val RESPONSE_OK   = "BOB"
  final val RESPONSE_NOT_FOUND = "Not found"

}

class BobServer(onProject: Project => Unit) extends NanoHTTPD(PORT) {

  override def serve(session: IHTTPSession): Response = {

    Log.i(TAG, s"serve() ${session.getMethod()} ${session.getUri()} ${session.getParms()}")

    if (session.getMethod() == Method.GET && session.getUri().equals(ROOT_URL)) {

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

    } else {
      Log.e(TAG, s"BobServer.serve() Invalid request: ${session.getMethod()} ${session.getUri()} ${session.getParms()}")
      new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, RESPONSE_NOT_FOUND)
    }

  }

}
