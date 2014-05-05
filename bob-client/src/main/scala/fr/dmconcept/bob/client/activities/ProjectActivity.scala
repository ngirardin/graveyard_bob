package fr.dmconcept.bob.client.activities

import ProjectActivity._
import android.app
import android.app.ActionBar
import android.app.ActionBar.{TabListener, Tab}
import android.content.Context
import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentPagerAdapter}
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener
import android.view.{MenuItem, Menu}
import fr.dmconcept.bob.client.R
import fr.dmconcept.bob.client.communications.BobCommunication
import fr.dmconcept.bob.client.models.{Step, Project}
import org.scaloid.common.LoggerTag
import org.scaloid.common._
import org.scaloid.support.v4.{SFragmentActivity, SViewPager}

object ProjectActivity {

  object Extras {
    val PROJECT_ID = "fr.dmconcept.bob.extras.projectId"
  }

}


class ProjectActivity extends SFragmentActivity with TraitContext[Context] with TagUtil {

  implicit override val loggerTag = LoggerTag("BobClient")

  // The bob application
  lazy val mApplication: BobApplication = getApplication.asInstanceOf[BobApplication]

  // The communication layer with the server
  lazy val mCommunication: BobCommunication = new BobCommunication(mApplication)

  // Deserialize the project from the intent extra
  lazy val mProject = getIntent.getSerializableExtra(Extras.PROJECT_ID).asInstanceOf[Project]

  override def onCreate(savedInstance: Bundle) {

    super.onCreate(savedInstance)

    info("ProjectActivity.onCreate()")

    // Set the project name as the activity title
    setTitle(mProject.name)

    // Create the timeline tabs
    val actionBar = getActionBar

    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)

    mProject.steps.zipWithIndex.foreach { case (step: Step, i: Int) =>

      actionBar.addTab(actionBar
        .newTab()
        .setText(s"${i + 1}")
        .setTabListener(new TabListener {

          override def onTabSelected(tab: Tab, ft: app.FragmentTransaction): Unit = {

            val position = tab.getPosition
            // Select the positions view pager for this step
            viewPager.setCurrentItem(position)
           }

          override def onTabReselected(tab: Tab, ft: app.FragmentTransaction): Unit = {}

          override def onTabUnselected(tab: Tab, ft: app.FragmentTransaction): Unit = {}

        })
      )
    }

    lazy val viewPager = new SViewPager {

      setId(0x0001) // Need to set any id on the ViewPager

      setAdapter(new FragmentPagerAdapter(supportFragmentManager) {

        override def getCount: Int = mProject.steps.length

        override def getItem(position: Int): Fragment =
          PositionsFragment.getInstance(position, mProject.steps(position), mProject.boardConfig)

      })

      setOnPageChangeListener(new SimpleOnPageChangeListener {
        override def onPageSelected(position: Int): Unit = {
          // Update the active tab
          getActionBar.setSelectedNavigationItem(position)
        }
      })

    }

    contentView = viewPager

  }

  override def onCreateOptionsMenu(menu: Menu) : Boolean = {

    getMenuInflater.inflate(R.menu.project, menu)

    super.onCreateOptionsMenu(menu)

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
      case _ => super.onOptionsItemSelected(item)
    }

  }

  def deleteStep() {

    //TODO implement step deletion
    alert("Step deletion", "TODO")
    /*
    mProject.copy(
      steps = mProject.steps.zipWithIndex.filterNot { _._2 == mStepIndex }.unzip._1
    )

    mApplication.projectsDao.saveSteps(mProject)

    mStepIndex = mStepIndex - 1

    updateTimeline()
    */

  }

  def newStep() {

    //TODO implement step creation
    alert("Step creation", "TODO")

    /*
    // Add the new step and save the project steps
    mApplication.projectsDao.saveSteps(
      mProject.copy(
        steps = mProject.steps :+ Step(Vector.fill(mProject.boardConfig.servoConfigs.length)(50))
      )
    )

    // Select the last period (the last step is the end step)
    mStepIndex = mProject.steps.length - 2

    // Update the timeline
    updateTimeline()
    */

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

}
