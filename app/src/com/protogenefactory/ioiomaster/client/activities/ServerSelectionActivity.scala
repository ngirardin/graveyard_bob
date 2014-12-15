package com.protogenefactory.ioiomaster.client.activities

import java.util.Date
import java.util.regex.Pattern

import android.app.Activity
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.text.{Editable, TextWatcher}
import android.view.{View, Gravity}
import android.view.inputmethod.EditorInfo
import android.widget.{EditText, LinearLayout, Button, TextView}
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.server.services.ServerService
import org.scaloid.common._

class ServerSelectionActivity extends SActivity {

  lazy val application   = getApplication.asInstanceOf[BobApplication]
  lazy val serverService = new LocalServiceConnection[ServerService]

  override def onCreate(savedInstanceState: Bundle): Unit = {

    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_server_selection)

    serverService.run(service => {

      info(s"ServerSelectionActivity.buttonConnectLocal Local connection available: ${service.isIOIOStarted}")

      if (service.isIOIOStarted) {

        findViewById(R.id.layoutLocal).asInstanceOf[LinearLayout].setVisibility(View.VISIBLE)
        setupListenersLocalBoard()

      } else {

        findViewById(R.id.layoutRemote).asInstanceOf[LinearLayout].setVisibility(View.VISIBLE)
        setupListenersRemoteBoard()

      }

    })

    updateVersion()

  }

  private def setupListenersLocalBoard(): Unit = {

    findViewById(R.id.buttonConnectLocal).asInstanceOf[Button].setOnClickListener(new OnClickListener {

      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        application.setLocalConnection(serverService)
        startActivity[PlayActivity]
        // Remove itself from the stack
        finish()
      }

    })

  }

  private def setupListenersRemoteBoard(): Unit = {

    val IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

    val editTextIP          = findViewById(R.id.editTextIP         ).asInstanceOf[EditText]
    val buttonConnectRemote = findViewById(R.id.buttonConnectRemote).asInstanceOf[Button  ]

    // Avoid full screen keyboard on landscape
    editTextIP.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)

    editTextIP.hint("IP address")

    editTextIP.text(application.serverIP)

    editTextIP.addTextChangedListener(new TextWatcher() {

      override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) : Unit ={
        // Nothing
      }

      override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {

        val valid = s.length() > 0 && IP_REGEXP.matcher(s.toString).matches()

        if (!valid)
          editTextIP.setError("Invalid IP address")

        buttonConnectRemote.setEnabled(valid)

      }

      override def afterTextChanged(s: Editable): Unit = {
        // Nothing
      }

    })

    // Enable the button if an IP address is stored in the preferences
    if (application.serverIP.nonEmpty)
      buttonConnectRemote.setEnabled(true)

    buttonConnectRemote.onClick {

      // Disable the Connect button while connecting
      buttonConnectRemote.setEnabled(false)

      val ip = editTextIP.getText.toString

      application.setRemoteConnection(ip)

      if (application.connection.ping()) {
        startActivity[PlayActivity]
        // Remove itself from the stack
        finish()
      } else {
        alert(s"Can't connect to the app", s"Unable to connect to $ip, check that the IOIO is connected on the other device.")
        // Re-enable the Connect button
        buttonConnectRemote.setEnabled(true)
      }

    }

  }

  private def updateVersion() {

    val textViewVersion = findViewById(R.id.textViewVersion).asInstanceOf[TextView]

    val pm        = getPackageManager.getPackageInfo(getPackageName, 0)
    val version   = pm.versionName
    val buildDate = new Date(pm.lastUpdateTime)

    textViewVersion.setText(s"Version $version\nBuilt $buildDate")

  }

}

