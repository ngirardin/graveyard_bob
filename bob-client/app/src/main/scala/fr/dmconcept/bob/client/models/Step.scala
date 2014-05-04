package fr.dmconcept.bob.client.models

import scala.collection.immutable.Vector
import scala.util.parsing.json.JSONArray

object Step extends BobSerializable[Step] {

  /** The minimum step duration in ms **/
  final val MIN_STEP_DURATION = 100

  /** The maximum step duration in ms **/
  final val MAX_STEP_DURATION = 60 * 1000

  /** The default step duration for a new step **/
  final val DEFAULT_DURATION = 5000

  def serialize(step: Step) = step.serialize

  def deserialize(serialized: Map[String, Any]) = Step(
    serialized("duration" ).asInstanceOf[Double].toInt,
    serialized("positions").asInstanceOf[List[Double]].map(_.toInt).toVector
  )

  def apply(positions: Vector[Int]) = new Step(DEFAULT_DURATION, positions)

}

case class Step(

  // The step getDuration in ms
  duration  : Int,

  // The servos position in percentage
  positions : Vector[Int]

) {

  assert(duration >= 0, "The duration must be positive")

  def serialize = Map(
    "duration"  -> duration,
    "positions" -> JSONArray(positions.toList)
  )

}
