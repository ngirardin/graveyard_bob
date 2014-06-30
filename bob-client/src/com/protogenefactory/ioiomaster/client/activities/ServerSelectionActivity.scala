package com.protogenefactory.ioiomaster.client.activities

import java.util.Date
import java.util.regex.Pattern

import android.view.Gravity
import android.view.inputmethod.EditorInfo
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.server.services.ServerService
import org.scaloid.common._

class ServerSelectionActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("Bob")

  final val IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

  lazy val application = getApplication.asInstanceOf[BobApplication]

  val serverService = new LocalServiceConnection[ServerService]

  lazy val buttonConnectLocal: SButton = new  SButton("Use local IOIO board") {
    onClick({

      application.setLocalConnection(serverService)

      startActivity[PlayActivity]

      // Remove itself from the stack
      finish()
    })

    serverService.run(s => {
      val available = s.isIOIOStarted
      info(s"ServerSelectionActivity.buttonConnectLocal Local connection available: $available")
      enabled(available)
    })
  }

  lazy val editTextIP : SEditText = new SEditText("Server IP address") {

    // Avoid full screen keyboard on landscape
    setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)

    hint("IP address")

    text(application.serverIP)

    onTextChanged({ (text: CharSequence, start: Int, lenghtBefore: Int, lenghtAfter: Int) =>

      val valid = text.length() > 0 && IP_REGEXP.matcher(text.toString).matches()

      if (!valid)
        setError("Invalid IP address")

      buttonConnectRemote.setEnabled(valid)

    })

  }

  lazy val buttonConnectRemote : SButton = new SButton("Connect") {
    onClick({

      val ip = editTextIP.getText.toString

      application.setRemoteConnection(ip)

      if (application.connection.ping()) {
        startActivity[PlayActivity]
        // Remove itself from the stack
        finish()
      } else {
        alert(s"Can't connect to the app", s"Unable to connect to $ip, check that the IOIO is connected on the other device.")
      }

    })
  }.enabled(!application.serverIP.isEmpty)

  onCreate {

    info("ServerSelectionActivity.onCreate()")

    contentView = new SVerticalLayout {

      this += new SVerticalLayout {

        this += buttonConnectLocal.<<(250.dip, WRAP_CONTENT).>>

        this += new SLinearLayout {
          this += new STextView("Server IP")
          this += editTextIP.<<(150.dip, WRAP_CONTENT).>>
          this += buttonConnectRemote
        }.wrap.>>

      }
        .<<.Weight(1).>>
        .gravity(Gravity.CENTER)

      val pm = getPackageManager.getPackageInfo(getPackageName, 0)
      val version = pm.versionName
      val buildDate = new Date(pm.lastUpdateTime)

      STextView(s"Version $version\nBuilt $buildDate")
        .gravity(Gravity.CENTER_HORIZONTAL)

    }

  }

}

