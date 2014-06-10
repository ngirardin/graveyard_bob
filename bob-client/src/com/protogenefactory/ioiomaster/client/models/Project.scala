package com.protogenefactory.ioiomaster.client.models

import com.protogenefactory.ioiomaster.client.models.json.BobJsonProtocol._
import java.util.UUID
import spray.json._

object Project {

  final val TAG = "models.Project"

  /**
   * The step default start and end position when creating an empty project
   */
  final val STEP_DEFAULT_POSITION = (0, 100)

  def deserialize(json: JsValue) = json.convertTo[Project]

  /**
   * Creates a new Project with a start and end Step
   *
   * @param name the project name
   * @param boardConfig the board config of the project
   *
   * @return the project
   */
  def apply(name: String, boardConfig: BoardConfig): Project = {

    val randomUUID = UUID.randomUUID.toString

    // Create the start and end steps with the same position count as defined
    // in the board config
    val positionsCount = boardConfig.servoConfigs.length

    val steps: Vector[Step] = Vector(
      Step(   Vector.fill(positionsCount)(  0)),
      Step(0, Vector.fill(positionsCount)(100))
    )

    Project(randomUUID, name, boardConfig, steps)

  }

}

case class Project(

  id: String,

  name: String,

  boardConfig: BoardConfig,

  steps: Vector[Step]

) {

  assert(!name.isEmpty           , "The project name is empty"                )
  assert(steps.length >=2        , "The project must contain at least 2 steps")
  assert(steps.last.duration == 0, "The last step duration must be equal to 0")

  steps.foreach(checkPositionsCount)

  /**
   * @return the project duration in seconds
   */
  def duration: Int = steps.map(_.duration).sum

  /*
  /**
   * Create a new step at the end of the project
   */
  public void addStep(int duration) {

      Log.i(TAG, "addStep(" + duration + ")");

      ArrayList<Integer> positions = new ArrayList<Integer>();

      for(ServoConfig ignored : mBoardConfig.getServoConfigs())
          positions.add(50);

      // Set the length on the last step
      mSteps.get(mSteps.size() - 1).setDuration(duration);

      // Add the new step
      mSteps.add(new Step(0, positions));

  }

  public void removeStep(int i) {

      Log.i(TAG, "removeStep(" + i + ")");

      mSteps.remove(i);

      // If the last step is removed, set the last step duration to 0
      if (i == mSteps.size()) {
          Log.i(TAG, "removeStep() - Removing last step setting the new last step duration to 0");
          mSteps.get(mSteps.size() -1).setDuration(0);
      }

  }
  */

  /**
   * Check that the step contains the same number of positions as defined in the board config
   */
  private def checkPositionsCount(step: Step) {

    val expected = boardConfig.servoConfigs.length

    assert(
      step.positions.length == expected,
      s"The step position count don't match the board config servo count: expecting $expected but got ${step.positions.length} in $step"
    )

  }

}
