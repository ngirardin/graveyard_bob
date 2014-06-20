package com.protogenefactory.ioiomaster.client.activities

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.{InputFilter, InputType}
import android.view._
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.{SeekBar, TextView}
import com.protogenefactory.ioiomaster.client.activities.PositionsFragment.{Extras, PositionsFragmentListener, States}
import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Step}
import com.protogenefactory.ioiomaster.client.utils.{PercentageInputFilter, StepDurationInputFilter}
import org.scaloid.common._
import org.scaloid.support.v4.SFragment

object PositionsFragment {

  object Extras {
    final val STEP_INDEX  = "stepindex"
    final val STEP        = "step"
    final val LAST_STEP   = "isLastStep"
    final val BOARDCONFIG = "boardconfig"
  }

  private object States {
    final val DURATION  = "duration"
    final val POSITIONS = "positions"
  }

  def getInstance(position : Int, step : Step, isLastStep: Boolean, boardConfig: BoardConfig): Fragment = {

    val f = new PositionsFragment

    // Need to pass the ID by using the setArguments method
    f.setArguments({
      val b = new Bundle()
      b.putInt         (Extras.STEP_INDEX , position   )
      b.putSerializable(Extras.STEP       , step       )
      b.putBoolean     (Extras.LAST_STEP  , isLastStep )
      b.putSerializable(Extras.BOARDCONFIG, boardConfig)
      b
    })

    f
  }

  trait PositionsFragmentListener {

    /**
    def onAutoplayChanged(checked: Boolean): Unit
    **/

    def onDurationChanged(stepIndex: Int, newDuration: Int)

    def onStepPositionChanged(stepIndex: Int, positionIndex: Int, newPosition: Int)

  }

}

class PositionsFragment extends SFragment with TagUtil {

  implicit override val loggerTag = LoggerTag("Bob")

  lazy val stepIndex    = getArguments.getInt         (Extras.STEP_INDEX ).ensuring(_ > -1, "Invalid position")
  lazy val mStep        = getArguments.getSerializable(Extras.STEP       ).asInstanceOf[Step]
  lazy val isLastStep   = getArguments.getBoolean     (Extras.LAST_STEP  )
  lazy val mBoardConfig = getArguments.getSerializable(Extras.BOARDCONFIG).asInstanceOf[BoardConfig]

  var mPositionsFragmentListener: PositionsFragmentListener = null

  override def onAttach(activity: Activity) {

    super.onAttach(activity)

    // Attach the listener used to communicate with the activity
    mPositionsFragmentListener = activity.asInstanceOf[PositionsFragmentListener]

  }

  lazy val editTextDuration = new SEditText(mStep.duration.toString) {

    inputType(InputType.TYPE_CLASS_NUMBER)

    // Set the Done button instead of Next and don't display full screen keyboard
    imeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI)

    filters(Array[InputFilter](StepDurationInputFilter))

    onEditorAction { (v: TextView, actionId: Int, event: KeyEvent) =>

      val newDuration = v.text.toString match {
        case "" =>
          v.text = Step.MIN_STEP_DURATION // Set the field value to 0
          Step.MIN_STEP_DURATION
        case s => s.toInt
      }

      if (newDuration < Step.MIN_STEP_DURATION)
        error(s"The step must last at least ${Step.MIN_STEP_DURATION} ms")

      info(s"Duration changed ")
      //TODO save the duration
      mPositionsFragmentListener.onDurationChanged(stepIndex, newDuration)

      // Return false to tell that the input is not consumed and let Android hide the keyboard
      false
    }
  }

  lazy val editTextPositions = mStep.positions.zipWithIndex map { case (position, i) =>

    new SEditText(mStep.positions(i).toString) {

      inputType(InputType.TYPE_CLASS_NUMBER)

      // Set the Done button instead of Next and don't display the full-screen keyboard
      imeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI)

      filters(Array[InputFilter](PercentageInputFilter))

      onEditorAction { (v: TextView, actionId: Int, event: KeyEvent) =>

        // Update the seekbar position to the new value
        val newPosition = v.text.toString match {
          case "" =>
            v.text = "0" // Set the field to 0
            0
          case s => s.toInt
        }

        info(s"PositionsFragment ${mBoardConfig.servoConfigs(i).servo} [$i] changed to $newPosition% (EditText)")

        updateSeekbarPosition(i, newPosition)

        // Save the position
        mPositionsFragmentListener.onStepPositionChanged(stepIndex, i, newPosition)

        // Return false to tell that the input is not consumed and let Android hide the keyboard
        false

      }

    }
  }

  lazy val seekbarPositions = mStep.positions.zipWithIndex map { case (position, i) =>

    new SSeekBar() {

      progress(position)

      onSeekBarChangeListener(new OnSeekBarChangeListener {

        override def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          updateEditTextPosition(i, progress)
        }

        override def onStartTrackingTouch(seekBar: SeekBar) {}

        override def onStopTrackingTouch(seekBar: SeekBar) {

          val newPosition = seekBar.progress

          info(s"PositionsFragment ${mBoardConfig.servoConfigs(i).servo} [$i] changed to $newPosition% (SeekBar)")

          updateEditTextPosition(i, newPosition)

          // Save the positions
          mPositionsFragmentListener.onStepPositionChanged(stepIndex, i, newPosition)
        }

      })

    }
  }

  private def updateSeekbarPosition(servoIndex: Int, newPosition: Int) {
    seekbarPositions(servoIndex).progress(newPosition)
  }

  private def updateEditTextPosition(servoIndex: Int, newPosition: Int) {
    editTextPositions(servoIndex).text(newPosition.toString)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) : View = {

    if (savedInstanceState != null) {

      val duration  = savedInstanceState.getCharSequence(States.DURATION ).ensuring(_ != null   , "Missing duration")
      val positions = savedInstanceState.getIntArray    (States.POSITIONS).ensuring(_.length > 0, "Empty positions" )

      val logPositions = positions.mkString(",")
      info(s"PositionsFragment.onCreateView()        [stepIndex=$stepIndex] duration=$duration, positions=$logPositions")

      editTextDuration.setText(duration)

      positions.zipWithIndex.foreach { case (position: Int, i: Int) =>
          updateEditTextPosition(i, position)
          updateSeekbarPosition (i, position)
      }

    } else {
      info(s"PositionsFragment.onCreateView()        [stepIndex=$stepIndex]")
    }

    new SVerticalLayout {

      // Duration layout
      this += new SVerticalLayout {

        // Hide it if last step
        if (isLastStep)
          visibility(View.INVISIBLE)

        STextView("Duration").wrap

        this += editTextDuration.<<(80.dip, WRAP_CONTENT).>>

        STextView("ms").wrap

      }
      .orientation(HORIZONTAL)
      .<<(WRAP_CONTENT, WRAP_CONTENT).>>

      // Positions layout
      this += new SVerticalLayout {

        mStep.positions.zipWithIndex foreach { case (position, i) =>

          // Position layout
          this += new SVerticalLayout {

            val servoConfig = mBoardConfig.servoConfigs(i)
            val paddedPin   = "%02d".format(servoConfig.pin)

            // Servo port
            this += new STextView(s"$paddedPin ${servoConfig.servo}")
              .gravity(Gravity.LEFT)
              .<<(90.dip, WRAP_CONTENT).>>

            // EditText position
            this += editTextPositions(i)
              .<<(55.dip, WRAP_CONTENT).>>

            // SeekBar position
            this += seekbarPositions(i)
              .<<(MATCH_PARENT, WRAP_CONTENT).>>

          }
          .<<(MATCH_PARENT, WRAP_CONTENT).>>
          .orientation(HORIZONTAL)
          .padding(
            0    , // left
            8.dip, // top
            0    , // right
            0      // bottom
          )
        }

      }

    }
    .padding(
      8.dip, // left
      0    , // top
      0    , // right
      0      // bottom
    )

  }

  override def onSaveInstanceState(outState: Bundle) {

    super.onSaveInstanceState(outState)

    val duration  = editTextDuration.getText
    val positions = seekbarPositions.map(_.progress)

    val logPositions = positions.mkString(",")
    info(s"PositionsFragment.onSaveInstanceState() [stepIndex=$stepIndex] duration=$duration, progress=$logPositions")

    outState.putCharSequence(States.DURATION , duration         )
    outState.putIntArray    (States.POSITIONS, positions.toArray)

  }

  override def onDestroyView() {
    info(s"PositionsFragment.onDestroyView()       [stepIndex=$stepIndex]")
    super.onDestroyView()
  }

}

