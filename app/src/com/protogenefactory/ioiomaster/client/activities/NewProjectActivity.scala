package com.protogenefactory.ioiomaster.client.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.inputmethod.EditorInfo
import android.view.{Gravity, Menu, MenuItem, View}
import android.widget._
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.client.activities.NewProjectActivity.STATES
import com.protogenefactory.ioiomaster.client.models.Project
import org.scaloid.common._

object NewProjectActivity {

  private object STATES {
    final val NAME        = "name"
    final val BOARDCONFIG = "boardconfig"
  }

}

class NewProjectActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("Bob")

  lazy val application    = getApplication.asInstanceOf[BobApplication]
  lazy val boardConfigDao = application.boardConfigDao

  lazy val editTextName = new SEditText() {

    hint("Project name")

    //Disable full screen keyboard on landscape
    setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)

    afterTextChanged { s: Editable =>

      setError(s.length == 0 match {
        case true => "Empty name"
        case false => null
      })

      // Update the save button menu state
      invalidateOptionsMenu()

    }

  }

  lazy val spinnerBoardConfig = new SSpinner()
    .adapter(adapter)
    .onItemSelected({ (parent: AdapterView[_], view: View, position: Int, id: Long) =>
    // Update the menu
    invalidateOptionsMenu()
  })

  lazy val adapter = SArrayAdapter(boardConfigDao.findAll().map(_.name).toArray)
    .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

  protected override def onCreate(savedInstanceState: Bundle) {

    super.onCreate(savedInstanceState)

    if (savedInstanceState != null) {
      editTextName.setText        (savedInstanceState.getCharSequence(STATES.NAME).ensuring(_ != null, "Empty name"))
      spinnerBoardConfig.selection(savedInstanceState.getInt(STATES.BOARDCONFIG))
  }

    contentView = new SVerticalLayout {

      this += editTextName

      this += new SLinearLayout {

        this += spinnerBoardConfig.<<.Weight(1).>>

        this += new SImageButton() {
          backgroundResource(android.R.drawable.ic_menu_add)
          onClick(
            startActivityForResult(SIntent[BoardConfigActivity], BoardConfigActivity.REQUEST_CODE)
          )
        }
        .marginLeft(16.dip)
        .marginTop(8.dip)
        .wrap.>>

      }.padding(0, 24.dip, 0, 0)

    }
    .padding(32.dip, 0, 32.dip, 0)
    .gravity(Gravity.CENTER)

  }

  override def onSaveInstanceState(outState: Bundle) {
    outState.putCharSequence(STATES.NAME       , editTextName.getText)
    outState.putInt         (STATES.BOARDCONFIG, spinnerBoardConfig.getSelectedItemPosition)
  }

  override def onCreateOptionsMenu(menu: Menu) : Boolean = {

    getMenuInflater.inflate(R.menu.main_save, menu)
    super.onCreateOptionsMenu(menu)

  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {

    val valid = editTextName.length() > 0

    // Disable the save button if the spinners have duplicated
    menu.findItem(R.id.action_save)
      .setEnabled(valid)
      .getIcon.setAlpha(if (valid) 255 else 130)

    true
  }

  override def onOptionsItemSelected(item: MenuItem) : Boolean = {

    item.getItemId match {

      case R.id.action_save =>

        // Create the project
        val name        = editTextName.getText.toString
        val boardConfig = boardConfigDao.findAll()(spinnerBoardConfig.selectedItemPosition)

        val project = Project(name, boardConfig)
        info(s"NewProjectActivity.onOptionsItemSelected(save) Saving $project")

        application.projectsDao.create(project)

        startActivity(SIntent[ProjectActivity].putExtra(ProjectActivity.Extras.PROJECT_ID, project.id))
        finish()

        true

      case _ => super.onOptionsItemSelected(item)

    }

  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

    // Back pressed on the Board Config Activity
    if (resultCode == Activity.RESULT_CANCELED) {
      info("NewProjectActivity.onActivityResult(resultCode=RESULT_CANCELED")
      return
    }

    // Back with a result
    requestCode.ensuring(_ == BoardConfigActivity.REQUEST_CODE, "Unexpected request code")
    resultCode .ensuring(_ == Activity.RESULT_OK              , "Unexpected result code")

    val id = data
      .getStringExtra(BoardConfigActivity.RESULTS.BOARD_CONFIG_ID)
      .ensuring(_ != null, "Null board config ID")

    info(s"NewProjectActivity.onActivityResult(resultCode=RESULT_OK) boardConfigID=$id")

    // Update the spinners
    // HACK should is notifyDataSetChanged() instead
    val boardConfigs = boardConfigDao.findAll()

    spinnerBoardConfig.setAdapter(SArrayAdapter(boardConfigs.map(_.name).toArray)
      .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item))

    // Select the return board config
    spinnerBoardConfig.selection(
      boardConfigs
        .zipWithIndex
        .filter { case (boardConfig, index) => boardConfig.id == id }
        .ensuring(_.size == 1)
        (0)._2
    )

  }

}

