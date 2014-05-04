package fr.dmconcept.bob.client.activities

import ServerIPSelectionActivity._
import android.app.Activity
import android.content.{Context, SharedPreferences}
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.{Button, TextView}
import fr.dmconcept.bob.client.{BobApplication, R}
import java.util.regex.Pattern

object ServerIPSelectionActivity {

  val TAG       = ".activities.ServerIPSelectionActivity"
  val IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

}

/* TODO use
case class TypedResource[A](id: Int)
case class TypedLayout[A](id: Int)
*/
/*
class ServerIPSelectionActivity extends DialogFragment { //with TypedViewHolder

  var preferences: SharedPreferences = null

  var mEditTextIP: TextView = null

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {

    val builder = new AlertDialog.Builder(getActivity());

    builder

      .setMessage("the message")

      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

        def onClick(dialog: DialogInterface, which: Int) {
          // nothing
        }

      })

      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

        def onClick(dialog: DialogInterface, id: Int) {
          // nothing
        }

      });

    builder.create()

  }

}
*/

class ServerIPSelectionActivity extends Activity /* with TypedViewHolder */ {

  lazy val application = getApplication.getPackageName

  def log(message: String) = Log.i(application, s"$TAG message")

  var preferences: SharedPreferences = null

  var mEditTextIP: TextView = null

  override def onCreate(savedInstanceState: Bundle) {

    preferences = getSharedPreferences(getPackageName, Context.MODE_PRIVATE)

    super.onCreate(savedInstanceState)

    setContentView(R.layout.layout_serveripselection)

    mEditTextIP = findViewById(R.id.editTextIP).asInstanceOf[TextView]

    // Update the IP edit text with the value read from the preferences, if any
    if (preferences.contains(BobApplication.PREFERENCES_SERVER_IP)) {

      val ip = preferences.getString(BobApplication.PREFERENCES_SERVER_IP, "")
      mEditTextIP.setText(ip)

      // TODO use string interpolation
      log("onCreate() - IP stored in preference: ")
      log(ip)

    } else {
      log("onCreate() - No IP stored in preferences")
    }

    findViewById(R.id.buttonConnect).asInstanceOf[Button].setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View) {

        val ip = mEditTextIP.getText.toString

        // Check that the IP is not empty and a valid ip
        if (ip.length() > 0 && !IP_REGEXP.matcher(ip).matches()) {
          mEditTextIP.setError("Invalid IP address")
          log("onClickListener - Invalid IP " + ip)
          return
        }

        // Store the IP address in the preferences
        preferences.edit()
          .putString(BobApplication.PREFERENCES_SERVER_IP, ip)
          .apply()

        // TODO use scala interpolation
        log("clickListener - IP updated to ")
        log(ip)

        // Start the project list activity
        finish()

      }

    })

  }

}