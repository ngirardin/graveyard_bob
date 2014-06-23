package com.protogenefactory.ioiomaster.client.activities

import java.util.Date
import java.util.regex.Pattern

import android.view.Gravity
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.server.services.ServerService
import org.scaloid.common._

class ServerSelectionActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("Bob")

  final val IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

  lazy val application = getApplication.asInstanceOf[BobApplication]

  val serverService = new LocalServiceConnection[ServerService]

  lazy val editTextIP : SEditText = new SEditText("Server IP address") {

    hint("IP address")

    text(application.serverIP)

    onTextChanged({ (text: CharSequence, start: Int, lenghtBefore: Int, lenghtAfter: Int) =>

      val valid = text.length() > 0 && IP_REGEXP.matcher(text.toString).matches()

      if (!valid)
        setError("Invalid IP address")

      buttonConnectRemote.setEnabled(valid)

    })

  }

  lazy val buttonConnectLocal: SButton = new  SButton("Use local IOIO board") {
    onClick({

      application.setLocalConnection()

      startActivity[PlayActivity]

      // Remove itself from the stack
      finish()
    })
  }

  lazy val buttonConnectRemote : SButton = new SButton("Connect") {
    onClick({

      application.setRemoteConnection(editTextIP.getText.toString)

      startActivity[PlayActivity]

      // Remove itself from the stack
      finish()
    })
  }.enabled(false)

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

      val pm = getPackageManager().getPackageInfo(getPackageName(), 0)
      val version = pm.versionName
      val buildDate = new Date(pm.lastUpdateTime)

      STextView(s"Version $version\nBuilt $buildDate")
        .gravity(Gravity.CENTER_HORIZONTAL)

    }

  }

}

