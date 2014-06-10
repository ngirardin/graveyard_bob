package com.protogenefactory.ioiomaster.client.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.{View, Menu, MenuItem}
import android.widget.AdapterView
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.activities.BoardConfigActivity.{STATES, RESULTS}
import com.protogenefactory.ioiomaster.client.models.{BoardConfig, ServoConfig}
import org.scaloid.common._

object BoardConfigActivity {

  final val REQUEST_CODE = 1

  private object STATES {
    final val NAME     = "name"
    final val SPINNERS = "spinners"
  }

  object RESULTS {
    final val BOARD_CONFIG_ID = "boardconfig"
  }

}

class BoardConfigActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("BobClient")

  lazy val boardConfigDao = getApplication.asInstanceOf[BobApplication].boardConfigDao

  val ports        = ServoConfig.PERIPHERAL_PORTS
  val portsSpinner = (Vector("none") ++ ports.map(p => s"pin $p")).toArray

  lazy val editTextName = new SEditText() {
    // Avoid full screen keyboard on landscape
    setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)
    hint("Board configuration name")
  }

  lazy val spinners = (0 until BoardConfig.MAX_SERVOS).map { i: Int =>

    new SSpinner()
      .adapter(
        SArrayAdapter(portsSpinner)
          .dropDownViewResource(android.R.layout.simple_spinner_item)
      )
      .onItemSelected({ (parent: AdapterView[_], view: View, position: Int, id: Long) =>

      // Update the menu
      invalidateOptionsMenu()

    })

  }

  override def onCreate(savedInstanceState: Bundle) {

    super.onCreate(savedInstanceState)

    info(s"BoardConfigActivity.onCreate($savedInstanceState)")

    if (savedInstanceState != null)
      restoreState(savedInstanceState)

    contentView = new SVerticalLayout {

      this += editTextName.<<(FILL_PARENT, WRAP_CONTENT).>>

      spinners.zipWithIndex.foreach { case (spinner: SSpinner, i: Int) =>

        this += new SVerticalLayout {
          STextView(s"Servo ${i + 1} connected to") .wrap
          this += spinner.<<.wrap.>>
        }.orientation(HORIZONTAL)

      }

    }.padding(
      32, // left
       0, // top
      32, // right
       0 // bottom
    )

  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    outState.putCharSequence(STATES.NAME    , editTextName.text)
    outState.putIntArray    (STATES.SPINNERS, spinners.map(_.selectedItemPosition).toArray)
  }

  private def restoreState(savedInstanceState: Bundle) {

    val savedName     = savedInstanceState.getCharSequence(STATES.NAME    ).ensuring(_ != null, "Empty saved name"    )
    val savedSpinners = savedInstanceState.getIntArray    (STATES.SPINNERS).ensuring(_ != null, "Empty saved spinners")

    info(s"BoardConfigActivity.onCreate() name=$savedName, spinners=$savedSpinners")

    editTextName.text(savedName)

    spinners.zip(savedSpinners).foreach { case (spinner: SSpinner, itemPosition: Int) =>
      spinner.selection(itemPosition)
    }

  }

  override def onCreateOptionsMenu(menu: Menu) : Boolean = {

    getMenuInflater.inflate(R.menu.main_save, menu)
    super.onCreateOptionsMenu(menu)

  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {

    val valid = validateSpinners()

    // Disable the save button if the spinners have duplicated
    menu.findItem(R.id.action_save)
      .setEnabled(valid)
      .getIcon.setAlpha(if (valid) 255 else 130)

    true
  }

  private def validateSpinners(): Boolean = {

    val pins = spinners
      .map(_.selectedItemPosition)
      .filterNot(_ == 0) // Remove the "nothing"

    //TODO highlight duplicate spinners
    pins.nonEmpty && (pins.size == pins.distinct.size)

  }

  override def onOptionsItemSelected(item: MenuItem) : Boolean = {

    item.getItemId match {

      case R.id.action_save =>

        val servoConfigs = spinners
          .zipWithIndex
          // Remove the servo not connected (spinner index == 0)
          .map { case (spinner: SSpinner, servoIndex: Int) =>
            // Bring back the pin to 0 indexed and the servoIndex to 1 indexed
            (spinner.selectedItemPosition - 1, servoIndex + 1)
          }
          .filter { case (pinIndex: Int, servoIndex : Int) =>

            val valid = pinIndex > -1

            if (!valid)
              info(s"BoardConfigActivity.onOptionsItemSelected() Servo $servoIndex not connected")

            valid
          }
          // Create the servo configs
          .map { case (pinIndex : Int, servoIndex : Int) =>

            val sc = ServoConfig(s"Servo $servoIndex", ServoConfig.PERIPHERAL_PORTS(pinIndex))
            info(s"BoardConfigActivity.onOptionsItemSelected() Creating $sc")
            sc

          }

        val boardConfig = BoardConfig(editTextName.getText.toString, servoConfigs)

        info(s"BoardConfigActivity.onOptionsItemSelected() Saving $boardConfig")
        boardConfigDao.create(boardConfig)

        // Send back the board config to the New Project Fragment
        setResult(Activity.RESULT_OK, new Intent().putExtra(RESULTS.BOARD_CONFIG_ID, boardConfig.id))
        finish()
        true

      case _ => super.onOptionsItemSelected(item)

    }

  }
}

