package fr.dmconcept.bob.client.activities

import android.content.{Context, Intent, SharedPreferences}
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.View
import android.widget.TextView

import java.util.regex.Pattern

import fr.dmconcept.bob.client.BobApplication
import fr.dmconcept.bob.client.R

import ServerIPSelectionActivity._

object ServerIPSelectionActivity {

  val TAG       = "activities.activities.ServerIPSelectionActivity"
  val IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

  def log(message: String) {
    Log.i(TAG, message)
  }

}

/* TODO use
case class TypedResource[A](id: Int)
case class TypedLayout[A](id: Int)
*/
class ServerIPSelectionActivity extends ActionBarActivity /* with TypedViewHolder */ {

  var preferences: SharedPreferences = null

  var mEditTextIP: TextView = null

  override def onCreate(savedInstanceState: Bundle) {

    preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)

    super.onCreate(savedInstanceState)

    setContentView(R.layout.layout_projectlist_ip)

    mEditTextIP = findViewById(R.id.editTextIP).asInstanceOf[TextView]

    // Update the IP edit text with the value read from the preferences, if any
    if (preferences.contains(BobApplication.PREFERENCES_SERVER_IP)) {

      val ip = preferences.getString(BobApplication.PREFERENCES_SERVER_IP, "")
      mEditTextIP.setText(ip)

      log(s"onCreate() - IP stored in preference: $ip")

    } else {
      log(s"onCreate() - No IP stored in preferences")
    }

    findViewById(R.id.buttonConnect)

      .setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View) {

        val ip = mEditTextIP.getText().toString()

        // Check that the IP is not empty and a valid ip
        if (ip.length() > 0 && !IP_REGEXP.matcher(ip).matches()) {
          mEditTextIP.setError("Invalid IP address")
          log(s"onClickListener - Invalid IP $ip")
          return
        }

        // Store the IP address in the preferences
        preferences.edit()
          .putString(BobApplication.PREFERENCES_SERVER_IP, ip)
          .apply()

        log(s"clickListener - IP updated to $ip")

        // Start the project list activity
        startActivity(
          new Intent(ServerIPSelectionActivity.this, classOf[ProjectListActivity])
        )

      }
    })

  }

}
