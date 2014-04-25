package fr.dmconcept.bob.client.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view._
import android.widget._
import fr.dmconcept.bob.client.communications.BobCommunication
import fr.dmconcept.bob.client.{R, BobApplication}
import fr.dmconcept.bob.client.models.{ServoConfig, Step, Project}
import ProjectActivity._

object ProjectActivity {

  // The intent extra key for the project id
  def EXTRA_PROJECT_ID = "fr.dmconcept.bob.extras.projectId"

  final val TAG = "activities.ProjectListActivity"

  // The default step duration for new steps in ms
  final val DEFAULT_STEP_DURATION = 2000

  // The minimum step duration in ms
  final val MIN_STEP_DURATION = 100

}

class ProjectActivity extends Activity {

  // The bob application
  lazy val mApplication: BobApplication = getApplication.asInstanceOf[BobApplication]

  // The communication layer with the server
  lazy val mCommunication: BobCommunication = new BobCommunication(mApplication)

  // The timeline, duration edit text and positions layout widgets
  lazy val mTimeline         : LinearLayout = findViewById(R.id.timeline        ).asInstanceOf[LinearLayout]

  // The duration EditText
  lazy val mDurationEditText : EditText     = findViewById(R.id.editTextDuration).asInstanceOf[EditText    ]

  // The start and end positions layout
  lazy val mPositions        : LinearLayout = findViewById(R.id.positions       ).asInstanceOf[LinearLayout]

  // The current project
  lazy val mProject: Project = {
    val projectId = getIntent.getStringExtra(EXTRA_PROJECT_ID)
    mApplication.projectsDao.findById(projectId)
  }

  // The active step index
  var mStepIndex: Int = 0

  override def onCreate(savedInstanceState: Bundle) {

    def registerViewListeners() {

      // Duration changed
      mDurationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

        override def onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean = {

          val newDuration: Int = v.getText.toString.toInt

          newDuration match {
            case MIN_STEP_DURATION =>
              v.setError("The step can't last less than " + MIN_STEP_DURATION + " ms")

            case _ =>
              // Save the new duration
              val step = mProject.steps(mStepIndex).copy(duration = newDuration)
              val steps = mProject.steps.updated(mStepIndex, step)
              val project = mProject.copy(steps = steps)

              mApplication.projectsDao.saveSteps(project)

              // Redraw the timeline to reflect the new step duration
              updateTimeline()
          }

          true

        }

      })

      // Click on the start or end buttons
      findViewById(R.id.buttonStart).setOnClickListener(new View.OnClickListener() {
        override def onClick(v: View) = playStartStep()
      })

      findViewById(R.id.buttonEnd).setOnClickListener(new View.OnClickListener() {
        override def onClick(v: View) = playEndStep()
      })

    }

    super.onCreate(savedInstanceState)

    Log.i(TAG, "onCreate()")

    setContentView(R.layout.activity_project)

//    mApplication   = (BobApplication) getApplication()

//    mCommunication = new BobCommunication(mApplication)

    // Get the project from the DB according to the intent extra id
//    val projectId: Long = getIntent.getLongExtra(EXTRA_PROJECT_ID, -1)

//    mTimeline         = (LinearLayout) findViewById(R.id.timeline        )
//    mDurationEditText = (EditText    ) findViewById(R.id.editTextDuration)
//    mPositions        = (LinearLayout) findViewById(R.id.positions       )

    // Register the views event listeners
    registerViewListeners()

    // Set the activity title as the project name
    setTitle(mProject.name)

    // Create the positions
    createPositions()

    // Fill the timeline with the steps
    updateTimeline()

  }

  /**
   * Delete all the timeline steps and create them with a width
   * matching their relative length
   */
  def updateTimeline() {

    Log.i(TAG, "updateTimeline()")

    val projectDuration: Float = mProject.duration

    mTimeline.removeAllViews()

    // Add the buttons to the timeline
    mProject.steps.zipWithIndex.foreach { case (step: Step, i: Int) =>

      val step: Step = mProject.steps(i)

      val durationRatio: Float = step.duration / projectDuration

      val button: ToggleButton = new ToggleButton(this)

      // Dynamically set the weight on the button according to the getDuration
      button.setLayoutParams(new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, durationRatio
      ))

      // Set the step id as the tag
      button.setTag(i)
      button.setTextOff((i + 1).toString)
      button.setTextOn ((i + 1).toString)

      button.setOnClickListener(new View.OnClickListener() {

        override def onClick(button: View) {
          mStepIndex = button.getTag.toString.toInt
          setActiveTimelineStep()
        }
      })

      // Add the button before the "new step" button
      mTimeline.addView(button)

    }

    setActiveTimelineStep()
  }

  /**
   * Enable the step button and update the position to the step value
   */
  def setActiveTimelineStep() {

    Log.i(TAG, "setActiveTimelineStep()")

    val stepCount : Int  = mProject.steps.length

    val step      : Step = mProject.steps(mStepIndex)

    // Set the default background on all position buttons
    for (i <- 0 to stepCount - 1)
    // Set the current button as selected
      mTimeline.getChildAt(i).asInstanceOf[ToggleButton].setChecked(i == mStepIndex)

    // Update the duration editText and position the cursor at the end
    val durationString: String = step.duration.toString
    mDurationEditText.setText     (durationString)
    mDurationEditText.setSelection(durationString.length())

    // Update the position sliders
    updatePositions()

  }

  class PositionListener(

    stepOffset    : Int,

    positionIndex : Int

  ) {

    def savePosition(newValue: Int) {

      val stepIndex: Int = mStepIndex + stepOffset

      val step = mProject.steps(stepIndex)

      val project = mProject.copy(
        steps = mProject.steps.updated(stepIndex, step.copy(
          positions = step.positions.updated(positionIndex, newValue)
        ))
      )

      mApplication.projectsDao.saveSteps(project)

    }

  }

  case class PositionTextEditorActionListener (
    seekBar  : SeekBar,
    step     : Int,
    position : Int
  ) extends PositionListener(step, position) with TextView.OnEditorActionListener {

    override def onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean = {

      val  newValue: Int = v.getText.toString.toInt

      if (newValue > 100) {
        v.setError("Position must be between 0 and 100")
      } else {
        seekBar.setProgress(newValue)
        savePosition(newValue)
      }

      true
    }

  }

  case class PositionSeekbarChangeListener(
    editText : EditText,
    step     : Int,
    position : Int
  ) extends PositionListener(step, position) with SeekBar.OnSeekBarChangeListener {

    override def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

      if (fromUser)
        // Update the percentage text
        editText.setText(String.valueOf(progress))

    }

    override def onStartTrackingTouch(seekBar: SeekBar) {
      // Do nothing
    }

    override def onStopTrackingTouch(seekBar: SeekBar) {
      savePosition(seekBar.getProgress)
    }
  }

  /**
   * Create the positions
   */
  def createPositions() {

    mProject.boardConfig.servoConfigs.zipWithIndex.foreach { case (servoConfig: ServoConfig, i: Int) =>

      // Inflate the position layout
      val positionLayout : LinearLayout = getLayoutInflater.inflate(R.layout.layout_project_positions, null).asInstanceOf[LinearLayout]

      val editPercentageLeft  : EditText = positionLayout.findViewById(R.id.editPercentageLeft).asInstanceOf[EditText]
      val seekbarLeft         : SeekBar  = positionLayout.findViewById(R.id.seekbarLeft ).asInstanceOf[SeekBar]
      val seekbarRight        : SeekBar  = positionLayout.findViewById(R.id.seekbarRight).asInstanceOf[SeekBar]
      val editPercentageRight : EditText = positionLayout.findViewById(R.id.editPercentageRight).asInstanceOf[EditText]

      // Set the listeners on the widgets
      editPercentageLeft .setOnEditorActionListener (new PositionTextEditorActionListener(seekbarLeft        , 0, i))
      seekbarLeft        .setOnSeekBarChangeListener(new PositionSeekbarChangeListener   (editPercentageLeft , 0, i))
      seekbarRight       .setOnSeekBarChangeListener(new PositionSeekbarChangeListener   (editPercentageRight, 1, i))
      editPercentageRight.setOnEditorActionListener (new PositionTextEditorActionListener(seekbarRight       , 1, i))

      // Append the position slider to the parent view
      mPositions.addView(positionLayout)

    }

  }

  def updatePositions() {

    val startPositions: Vector[Int] = mProject.steps(mStepIndex    ).positions
    val endPositions  : Vector[Int] = mProject.steps(mStepIndex + 1).positions

    mProject.boardConfig.servoConfigs.zipWithIndex.foreach { case (servoConfig: ServoConfig, i: Int) =>

      val startPosition : Int = startPositions(i)
      val endPosition   : Int = endPositions  (i)

      val positionLayout: LinearLayout = mPositions.getChildAt(i).asInstanceOf[LinearLayout]

      positionLayout.findViewById(R.id.editPercentageLeft ).asInstanceOf[EditText].setText    (startPosition.toString)
      positionLayout.findViewById(R.id.seekbarLeft        ).asInstanceOf[SeekBar ].setProgress(startPosition         )

      positionLayout.findViewById(R.id.seekbarRight       ).asInstanceOf[SeekBar ].setProgress(endPosition           )
      positionLayout.findViewById(R.id.editPercentageRight).asInstanceOf[EditText].setText    (endPosition.toString  )

    }

  }

  override def onCreateOptionsMenu(menu: Menu) : Boolean = {

    // Inflate the menu this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.project, menu)

    super.onCreateOptionsMenu(menu)

  }

  override def onOptionsItemSelected(item: MenuItem) : Boolean = {

    item.getItemId match {

      case R.id.action_playStep =>
        playWholeStep()
        true

      case R.id.action_playProject =>
        playProject()
        true

      case R.id.action_deleteStep =>
        deleteStep()
        true

      case R.id.action_addStep =>
        newStep()
        true

      case _ => super.onOptionsItemSelected(item)

    }

  }

  def deleteStep() {

    mProject.copy(
        steps = mProject.steps.zipWithIndex.filterNot { _._2 == mStepIndex }.unzip._1
    )

    mApplication.projectsDao.saveSteps(mProject)

    mStepIndex = mStepIndex - 1

    updateTimeline()

  }

  def newStep() {

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

  }

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

  def playProject() {

    Log.i(TAG, "playProject()")

    try {
      Toast.makeText(this, "Playing the project.", Toast.LENGTH_SHORT).show()
      mCommunication.sendSteps(mProject)
    } catch {
      case e: Throwable => showNetworkErrorDialog(e)
    }

  }

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

}
