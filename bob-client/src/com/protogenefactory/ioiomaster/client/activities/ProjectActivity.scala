package com.protogenefactory.ioiomaster.client.activities

import android.app
import android.app.ActionBar.{Tab, TabListener}
import android.app.{ActionBar, Activity}
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentStatePagerAdapter}
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener
import android.view.{Menu, MenuItem}
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.client.activities.ProjectActivity._
import com.protogenefactory.ioiomaster.client.communications.BobCommunication
import com.protogenefactory.ioiomaster.client.models.{Project, Step}
import org.scaloid.common._
import org.scaloid.support.v4.{SFragmentActivity, SViewPager}

object ProjectActivity {

  object Extras {
    val PROJECT_ID = "projectId"
  }

  object States {
    val CURRENT_STEP = "currentStep"
  }

}

class ProjectActivity extends SFragmentActivity /* with TraitContext[Context] */ with TagUtil with PositionsFragment.PositionsFragmentListener {

  implicit override val loggerTag = LoggerTag("BobClient")

  implicit val activity: Activity = this

  // The bob application
  lazy val application: BobApplication = getApplication.asInstanceOf[BobApplication]

  var project: Project = null

  override def onCreate(savedInstanceState: Bundle) {

    super.onCreate(savedInstanceState)

    val projectId = getIntent.getStringExtra(Extras.PROJECT_ID)

    project = application.projectsDao.findById(projectId)

    // Set the project name as the activity title
    setTitle(project.name)

    // Create the timeline tabs
    createTabs()

    // Need to create the tabs before selected the saved one
    if (savedInstanceState != null) {

      val currentStep = savedInstanceState.getInt(States.CURRENT_STEP).ensuring(_ > -1)
      info(s"ProjectActivity.onCreate(project=${project.id}) currentStep=$currentStep")

      viewPager.currentItem(currentStep)

    } else {
      info(s"ProjectActivity.onCreate(project=$projectId)")
    }

    // Create the view pager
    contentView = viewPager

  }

  lazy val viewPager = new SViewPager {

    setId(getUniqueId) // Need to set any id on the ViewPager

    setAdapter(new FragmentStatePagerAdapter(supportFragmentManager) {

      override def getCount: Int = project.steps.length

      override def getItem(position: Int): Fragment = {

        val isLastStep = position == project.steps.length - 1
        PositionsFragment.getInstance(position, project.steps(position), isLastStep, project.boardConfig)

      }

      override def getItemPosition(o: Any): Int = PagerAdapter.POSITION_NONE

    })

    setOnPageChangeListener(new SimpleOnPageChangeListener {
      override def onPageSelected(position: Int): Unit = {
        // Update the active tab
        getActionBar.setSelectedNavigationItem(position)
      }
    })

  }
  private def createTabs() {

    val actionBar = getActionBar

    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)

    project.steps.zipWithIndex.foreach { case (step: Step, stepIdex: Int) =>
      actionBar.addTab(newTab(actionBar).setText(s"${stepIdex + 1}"))
    }
  }

  private def newTab(actionBar: ActionBar): ActionBar.Tab = {

    // Pass the actionBar variable to avoid calling getActionBar several time
    actionBar.newTab().setTabListener(new TabListener {

      override def onTabSelected(tab: Tab, ft: app.FragmentTransaction): Unit = {
        val position = tab.getPosition
        // Select the positions view pager for this step
        viewPager.setCurrentItem(position)
      }

      override def onTabReselected(tab: Tab, ft: app.FragmentTransaction): Unit = {}

      override def onTabUnselected(tab: Tab, ft: app.FragmentTransaction): Unit = {}

    })

  }

  def updateTabsText() {

    val actionBar = getActionBar

    for (i <- 0 until actionBar.getTabCount)
      actionBar.getTabAt(i).setText(s"${i + 1}")

  }

  override def onDurationChanged(stepIndex: Int, newDuration: Int) {

    info(s"ProjectActivity.onDurationChanged(stepIndex=$stepIndex, newDuration=$newDuration")

    val oldSteps     = project.steps
    val oldStep      = oldSteps(stepIndex)

    val newStep      = oldStep.copy(duration = newDuration)
    val newSteps     = oldSteps.updated(stepIndex, newStep)

    project = project.copy(steps = newSteps)

    application.projectsDao.updateSteps(project)

  }

  override def onStepPositionChanged(stepIndex: Int, servoIndex: Int, newPosition: Int) {

    info(s"ProjectActivity.onStepPositionChanged(stepIndex=SstepIndex, servoIndex=$servoIndex, newPosition=$newPosition")

    val oldSteps     = project.steps
    val oldStep      = oldSteps(stepIndex)
    val oldPositions = oldStep.positions

    val newPositions = oldPositions.updated(servoIndex, newPosition)
    val newStep      = oldStep.copy(positions = newPositions)
    val newSteps     = oldSteps.updated(stepIndex, newStep)

    project = project.copy(steps = newSteps)

    application.projectsDao.updateSteps(project)

  }

  override def onSaveInstanceState(outState: Bundle) {

    val currentStep = viewPager.getCurrentItem

    info(s"ProjectActivity.onSaveInstanceState() currentStep=$currentStep")

    outState.putInt(States.CURRENT_STEP, currentStep)

  }

  override def onCreateOptionsMenu(menu: Menu) : Boolean = {
    getMenuInflater.inflate(R.menu.project, menu)
    super.onCreateOptionsMenu(menu)
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {

    // Disable the delete button if only two steps left
    menu.findItem(R.id.action_deleteStep).setVisible(
      getActionBar.getTabCount > 2
    )

    // Check the autoplay checkbox according to the preferences
    menu.findItem(R.id.action_autoplay).setChecked(
      defaultSharedPreferences.getBoolean(BobApplication.Preferences.AUTOPLAY, false)
    )

    true
  }

  override def onOptionsItemSelected(item: MenuItem) : Boolean = {

    def t(f: => Unit): Boolean = {
      f
      true
    }

    item.getItemId match {

      case R.id.action_deleteStep   => t(deleteStep())

      case R.id.action_insertStep   => t(newStep())

      case R.id.action_playProject  => t(playProject())

      case R.id.action_autoplay     =>
        item.setChecked(!item.isChecked)
        t(onAutoplayChanged(item.isChecked))

      case _ => super.onOptionsItemSelected(item)

    }

  }

  def deleteStep() {

    val actionBar = getActionBar
    val tabCount  = actionBar.getTabCount
    val tabIndex  = actionBar.getSelectedNavigationIndex

    // If deleting the last step, set the last -1 step duration to 0
    if (tabIndex == project.steps.length - 1) {

      // Drop the last step
      val withoutLast = project.steps.init // Drop the last step

      project = project.copy(
        steps = withoutLast.updated(tabIndex - 1, withoutLast.last.copy(duration = 0)) // Set the new last step duration to 0
      )

    } else {

      // Delete the step
      project = project.copy(
        steps = project.steps.zipWithIndex.filterNot { _._2 == tabIndex }.unzip._1
      )

    }

    // Save the project
    application.projectsDao.updateSteps(project)

    // Notify the page adapter that the project changed
    viewPager.getAdapter.notifyDataSetChanged()

    // Delete the tab
    actionBar.removeTabAt(tabIndex)

    // Update the tab texts
    updateTabsText()

    // Disable the delete menu if only two tabs left (3 before deletion)
    if (tabCount == 3)
      invalidateOptionsMenu()

  }

  def newStep() {

    val actionBar = getActionBar
    val tabIndex  = actionBar.getSelectedNavigationIndex

    // Add of a copy of the current step after the current step
    project = project.copy(
      steps = (project.steps.take(tabIndex) :+ project.steps(tabIndex).copy()) ++ project.steps.drop(tabIndex)
    )

    // Save the project
    application.projectsDao.updateSteps(project)

    // Notify the page adapter that the project changed
    viewPager.getAdapter.notifyDataSetChanged()

    // Add the tab and select it
    actionBar.addTab(newTab(actionBar), tabIndex + 1, true)

    // Update the tab texts
    updateTabsText()

    // Update the delete menu state
    invalidateOptionsMenu()

  }

  //TODO update
  def playProject() {

    info("ProjectActivity.playProject")

    try {

      //TODO show play dialog
      // The communication layer with the server
      val mCommunication: BobCommunication = new BobCommunication(this)

      mCommunication.send(application.serverIP, project)

      val progress = new android.app.ProgressDialog(this)
      progress.setTitle("Playing the project...")
      progress.setMessage("Touch outside to cancel")
      progress.setIndeterminate(true)
      progress.setCancelable(true)
      progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int): Unit = dialog.cancel()
      })
      progress.setOnCancelListener(new DialogInterface.OnCancelListener {
        override def onCancel(dialog: DialogInterface): Unit = {
          dialog.cancel()
          //TODO cancel the playing
          alert("TODO","cancel")
        }
      })
      progress.show()

      new java.util.Timer().schedule(new java.util.TimerTask() {
        override def run() {
          progress.dismiss()
        }
      }, project.duration)

    } catch {
      case e: Throwable =>
        alert("Network error", "Check that the server app is running and connected to the same network that this device.")
        e.printStackTrace()
    }

  }

  def onAutoplayChanged(checked: Boolean) {

    // Save the autoplay status to the preferences
    defaultSharedPreferences
      .edit
      .putBoolean(BobApplication.Preferences.AUTOPLAY, checked)
      .apply()

  }

}
