package com.protogenefactory.ioiomaster.client.activities

import java.util.regex.Pattern

import android.view.Gravity
import org.scaloid.common._

class ServerSelectionActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("Bob")

  final val IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

  lazy val application= getApplication.asInstanceOf[BobApplication]

  lazy val editTextIP : SEditText = new SEditText("Server IP address") {

    hint("IP address")

    text(application.serverIP)

    onTextChanged({ (text: CharSequence, start: Int, lenghtBefore: Int, lenghtAfter: Int) =>

      val valid = text.length() > 0 && IP_REGEXP.matcher(text.toString).matches()

      if (!valid)
        setError("Invalid IP address")

      buttonConnect.setEnabled(valid)

    })

  }

  lazy val buttonConnect : SButton = new SButton("Connect") {

    onClick({

      application.serverIP(editTextIP.getText.toString)

      startActivity[PlayActivity]

      // Remove itself from the stack
      finish()

    })

  }

  onCreate {

    contentView = new SVerticalLayout {

      this += new SLinearLayout {

        this += new STextView("Server IP")

        this += editTextIP

      }.wrap.>>

      this += buttonConnect.<<.wrap.>>

      gravity(Gravity.CENTER)

    }

  }

}

