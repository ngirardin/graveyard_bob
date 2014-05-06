package fr.dmconcept.bob.client.activities

import ProjectActivity._
import android.app
import android.app.ActionBar
import android.app.ActionBar.{TabListener, Tab}
import android.content.Context
import android.os.Bundle
import android.support.v4.app.{FragmentStatePagerAdapter, Fragment, FragmentPagerAdapter}
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener
import android.view.{MenuItem, Menu}
import fr.dmconcept.bob.client.communications.BobCommunication
import fr.dmconcept.bob.client.models.{Step, Project}
import fr.dmconcept.bob.client.{TR, BobApplication, R}
import org.scaloid.common._
import org.scaloid.support.v4.{SFragmentActivity, SViewPager}
import android.support.v4.view.PagerAdapter

object ProjectActivity {

  object Extras {
    val PROJECT_ID = "fr.dmconcept.bob.extras.projectId"
  }

}

class ProjectActivity extends SFragmentActivity with TraitContext[Context] with TagUtil with PositionsFragment.PositionsFragmentListener {

  implicit override val loggerTag = LoggerTag("BobClient")

  // The bob application
  lazy val mApplication: BobApplication = getApplication.asInstanceOf[BobApplication]

  // The communication layer with the server
  lazy val mCommunication: BobCommunication = new BobCommunication(mApplication)

  // Deserialize the project from the intent extra
  //TODO replace by projectdao
  var mProject: Project = null

  override def onCreate(savedInstance: Bundle) {

    super.onCreate(savedInstance)

    info("ProjectActivity.onCreate()")

    //TODO replace by projectdao
    mProject = getIntent.getSerializableExtra(Extras.PROJECT_ID).asInstanceOf[Project]

    // Set the project name as the activity title
    setTitle(mProject.name)

    // Create the timeline tabs
    createTabs()

    contentView = viewPager

  }

  private def createTabs() {
    val actionBar = getActionBar

    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)

    mProject.steps.zipWithIndex.foreach { case (step: Step, i: Int) =>
      actionBar.addTab(newTab(actionBar).setText(s"${i + 1}"))
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


  lazy val viewPager = new SViewPager {

    setId(0x0001) // Need to set any id on the ViewPager

    setAdapter(new FragmentStatePagerAdapter(supportFragmentManager) {

      override def getCount: Int = mProject.steps.length

      override def getItem(position: Int): Fragment =
        PositionsFragment.getInstance(position, mProject.steps(position), mProject.boardConfig)

      override def getItemPosition(o: Any): Int = PagerAdapter.POSITION_NONE

    })

    setOnPageChangeListener(new SimpleOnPageChangeListener {
      override def onPageSelected(position: Int): Unit = {
        // Update the active tab
        getActionBar.setSelectedNavigationItem(position)
      }
    })

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
      defaultSharedPreferences.getBoolean("autoplay", false)
    )

    true
  }

  override def onOptionsItemSelected(item: MenuItem) : Boolean = {

    def t(f: => Unit): Boolean = {
      f
      true
    }

    item.getItemId match {
      case R.id.action_deleteStep  => t(deleteStep() )
      case R.id.action_insertStep  => t(newStep()    )
      case R.id.action_playProject => t(playProject())
      case R.id.action_autoplay    => 
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
    if (tabIndex == mProject.steps.length - 1) {

      // Drop the last step
      val withoutLast = mProject.steps.init // Drop the last step

      mProject = mProject.copy(
        steps = withoutLast.updated(tabIndex - 1, withoutLast.last.copy(duration = 0)) // Set the new last step duration to 0
      )

    } else {

      // Delete the step
      mProject = mProject.copy(
        steps = mProject.steps.zipWithIndex.filterNot { _._2 == tabIndex }.unzip._1
      )

    }

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
    mProject = mProject.copy(
      steps = (mProject.steps.take(tabIndex) :+ mProject.steps(tabIndex).copy()) ++ mProject.steps.drop(tabIndex)
    )

    // Notify the page adapter that the project changed
    viewPager.getAdapter.notifyDataSetChanged()

    // Add the tab and select it
    actionBar.addTab(newTab(actionBar), tabIndex + 1, true)

    // Update the tab texts
    updateTabsText()

    // Update the delete menu state
    invalidateOptionsMenu()

  }

  /*
  def playStartStep() {

    Log.i(TAG, "playStartStep()")

    try {
      Toast.makeText(this, "Playing the start position.", Toast.LENGTH_SHORT).show()
      mCommunication.sendStep(mProject.boardConfig, mProject.steps(mStepIndex))
    } catch {
      case e: Throwable => showNetworkErrorDialog(e)
    }

  }

  def playEndStep() {

    Log.i(TAG, "playEndStep()")

    try {
      Toast.makeText(this, "Playing the end position.", Toast.LENGTH_SHORT).show()
      mCommunication.sendStep(mProject.boardConfig, mProject.steps(mStepIndex + 1))
    } catch {
      case e: Throwable => showNetworkErrorDialog(e)
    }

  }

  def playWholeStep() {

    Log.i(TAG, "playWholeStep()")

    try {
      Toast.makeText(this, "Playing the step.", Toast.LENGTH_SHORT).show()
      mCommunication.sendSteps(mProject.boardConfig, mProject.steps(mStepIndex), mProject.steps(mStepIndex + 1))
    } catch {
      case e: Throwable => showNetworkErrorDialog(e)
    }

  }
  */

  def playProject() {

    info("ProjectActivity.playProject")

    new AlertDialogBuilder("Playing project", "The project is playing...") {
      negativeButton("Cancel" /*android.R.string.cancel, toast("Cancelled")*/)
    }.show()

    /*
    try {
      Toast.makeText(this, "Playing the project.", Toast.LENGTH_SHORT).show()
      mCommunication.sendSteps(mProject)
    } catch {
      case e: Throwable => showNetworkErrorDialog(e)
    }
    */

  }

  /*
  def showNetworkErrorDialog(exception: Throwable) {
    new AlertDialog.Builder(this)
      .setTitle("Network error")
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setMessage(s"Can't contact the Bob Server app: ${exception.getMessage}.")
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        override def onClick(dialog: DialogInterface, which: Int) {
          dialog.dismiss()
        }
      })
      .show()
  }
  */

  def onAutoplayChanged(checked: Boolean) {

    // Save the autoplay status to the preferences
    defaultSharedPreferences
      .edit
      .putBoolean("autoplay", checked)
      .apply()

  }


}
