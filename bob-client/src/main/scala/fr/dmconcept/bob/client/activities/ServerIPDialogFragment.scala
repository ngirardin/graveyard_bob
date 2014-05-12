package fr.dmconcept.bob.client.activities

import android.app.{AlertDialog, Dialog}
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.os.Bundle
import fr.dmconcept.bob.client.BobApplication
import java.util.regex.Pattern
import org.scaloid.common._
import org.scaloid.support.v4.SDialogFragment

object ServerIPDialogFragment extends SDialogFragment with TagUtil {

  implicit override val loggerTag = LoggerTag("BobClient")

  final val IP_REGEXP = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

  def editTextIP: SEditText = new SEditText("Server IP address") {

    hint("IP address")

    text(defaultSharedPreferences.getString(BobApplication.Preferences.SERVER_IP, ""))

  }

  //TODO fix crash on orientation change
  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {

    val builder = new AlertDialogBuilder("Server IP address", "Start the server app and type the IP address that it displays.") {

      setView(new SVerticalLayout {

        STextView("Server IP").wrap
        this += editTextIP

        orientation(HORIZONTAL)
        padding(50, 0, 50, 0) // left and right

      })

      positiveButton(onClick = {

        defaultSharedPreferences
          .edit
          .putString(BobApplication.Preferences.SERVER_IP, editTextIP.getText.toString)
          .apply()

      })

      negativeButton()

    }

    val alertDialog = builder.create()

    alertDialog.setOnShowListener(new OnShowListener {

      override def onShow(dialog: DialogInterface): Unit = {

        val d = dialog.asInstanceOf[AlertDialog]

        def updateOKButton(text: CharSequence) {

          val valid = text.length() > 0 && IP_REGEXP.matcher(text.toString).matches()

          if (!valid)
            editTextIP.setError("Invalid IP address")

          d.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(valid)
        }

        // Update the OK button when showing the dialog
        updateOKButton(editTextIP.getText)

        // Update the OK button when the text changes
        editTextIP.onTextChanged({ (text: CharSequence, start: Int, lenghtBefore: Int, lenghtAfter: Int) =>
          updateOKButton(text)
        })
      }

    })

    alertDialog

  }

}

