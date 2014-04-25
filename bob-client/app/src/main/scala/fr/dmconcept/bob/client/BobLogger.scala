package fr.dmconcept.bob.client

import android.util.Log

trait BobLogger {

  val TAG: String

  def log(method: String, message: String) = Log.i("BobClient", s"$TAG.$method $message")

}
