package fr.dmconcept.bob.client.models

import fr.dmconcept.bob.client.models.json.BobJsonProtocol._
import scala.collection.immutable.Vector
import spray.json._

object Step {

  /** The minimum step duration in ms **/
  final val MIN_STEP_DURATION = 100

  /** The maximum step duration in ms **/
  final val MAX_STEP_DURATION = 60 * 1000

  /** The default step duration for a new step **/
  final val DEFAULT_DURATION = 5000

  def deserialize(json: JsValue) = json.convertTo[Step]

  def apply(positions: Vector[Int]) = new Step(DEFAULT_DURATION, positions)

}

case class Step(

  // The step getDuration in ms
  duration  : Int,

  // The servos position in percentage
  positions : Vector[Int]

) {

  assert(duration >= 0, "The duration must be positive")

}
