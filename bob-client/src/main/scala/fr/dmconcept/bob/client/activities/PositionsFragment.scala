package fr.dmconcept.bob.client.activities

import PositionsFragment.Extras
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.{InputFilter, InputType}
import android.view._
import android.view.inputmethod.EditorInfo
import android.widget.{SeekBar, TextView}
import fr.dmconcept.bob.client.models.{BoardConfig, Step}
import fr.dmconcept.bob.client.utils.{PercentageInputFilter, StepDurationInputFilter}
import org.scaloid.common._
import org.scaloid.support.v4.SFragment

object PositionsFragment {

  object Extras {
    final val POSITION    = "fr.dmconcept.bob.extras.position"
    final val STEP        = "fr.dmconcept.bob.extras.step"
    final val BOARDCONFIG = "fr.dmconcept.bob.extras.boardConfig"
  }

  def getInstance(position : Int, step : Step, boardConfig: BoardConfig): Fragment = {

    val f = new PositionsFragment

    // Need to pass the ID by using the setArguments method
    f.setArguments({
      val b = new Bundle()
      //TODO use const
      b.putInt         (Extras.POSITION   , position   )
      b.putSerializable(Extras.STEP       , step       )
      b.putSerializable(Extras.BOARDCONFIG, boardConfig)
      b
    })

    f
  }

}

class PositionsFragment extends SFragment with TagUtil {

  implicit override val loggerTag = LoggerTag("BobClient")

  lazy val mPosition    = getArguments.getInt         (Extras.POSITION   ).ensuring(_ > -1, "Invalid position")
  lazy val mStep        = getArguments.getSerializable(Extras.STEP       ).asInstanceOf[Step]
  lazy val mBoardConfig = getArguments.getSerializable(Extras.BOARDCONFIG).asInstanceOf[BoardConfig]

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) : View = {

    info(s"PositionFragment.onCreateView($mPosition)")

    new SVerticalLayout {

      this += new SVerticalLayout {

        // Auto-play + duration widgets
        //TODO autoplay
        SCheckBox("Auto-play", toast("TODO checkbocAutoplay changed"))
          .wrap
          .Weight(1).>>

        STextView("Duration").wrap

        this += new SEditText(mStep.duration.toString) {

          inputType(InputType.TYPE_CLASS_NUMBER)

          // Set the Done button instead of Next
          imeOptions(EditorInfo.IME_ACTION_DONE)

          filters(Array[InputFilter](StepDurationInputFilter))

          onEditorAction { (v: TextView, actionId: Int, event: KeyEvent) =>

              val newVal = v.text.toString match {
                case "" =>
                  v.text = Step.MIN_STEP_DURATION // Set the field value to 0
                  Step.MIN_STEP_DURATION
                case s => s.toInt
              }

              if (newVal < Step.MIN_STEP_DURATION)
                error(s"The step must last at least ${Step.MIN_STEP_DURATION} ms")

              //TODO save the duration
              onDurationChanged(newVal)

              // Return false to tell that the input is not consumed and let Android hide the keyboard
              false
          }
        }.<<(80.dip, WRAP_CONTENT).>>

        STextView("ms").wrap

      }.orientation(HORIZONTAL)

      this += new SVerticalLayout {

        mStep.positions.zipWithIndex foreach {
          case (position, i) =>

            // Position layout
            this += new SVerticalLayout {

              // Servo port
              this += new STextView(mBoardConfig.servoConfigs(i).port.toString) {
                gravity(Gravity.RIGHT)
                padding(0, 0, 8, 0) // right
              }.<<(30.dip, WRAP_CONTENT).>>

              // Position percentage EditText
              val positionEditText = new SEditText(mStep.positions(i).toString) {

                inputType(InputType.TYPE_CLASS_NUMBER)

                // Set the Done button instead of Next
                imeOptions(EditorInfo.IME_ACTION_DONE)

                filters(Array[InputFilter](PercentageInputFilter))

                onEditorAction { (v: TextView, actionId: Int, event: KeyEvent) =>

                  // Update the seekbar position to the new value
                  val newVal = v.text.toString match {
                    case "" =>
                      v.text = "0" // Set the field to 0
                      0
                    case s => s.toInt
                  }

                  info(s"ProjectActivity - Servo $i changed to $newVal% (EditText)")

                  positionSeekBar.progress(newVal)

                  // Save the position
                  onStepPositionChanged()

                  // Return false to tell that the input is not consumed and let Android hide the keyboard
                  false

                }

              }.<<(55.dip, WRAP_CONTENT).>>

              this += positionEditText

              // Position seek bar
              lazy val positionSeekBar: SSeekBar = new SSeekBar() {

                progress(position)

                onStopTrackingTouch { seekbar: SeekBar =>

                  info(s"ProjectActivity - Servo $i changed to ${seekbar.progress}% (SeekBar)")

                  // Update the percentage edit text when the seekbar value changes
                  positionEditText.text(seekbar.progress.toString)

                  // Save the positions
                  onStepPositionChanged()
                }

              }.<<(MATCH_PARENT, WRAP_CONTENT).>>

              this += positionSeekBar

            }
              .orientation(HORIZONTAL)
              .padding(
                0, // left
                8, // top
                0, // right
                8 // bottom
              )
              .<<(MATCH_PARENT, WRAP_CONTENT).>>
        }

      }

    }

  }

  private def onStepPositionChanged() {
    toast("Position changed")
  }

  private def onDurationChanged(duration: Int) {
    toast(s"TODO onDurationChanged($duration)")
  }

  override def onDestroyView() {
    info(s"PositionFragment.onDestroyView($mPosition)")
    super.onDestroyView()
  }

}

