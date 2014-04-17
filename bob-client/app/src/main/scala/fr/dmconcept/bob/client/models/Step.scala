package fr.dmconcept.bob.client.models

import scala.collection.immutable.Vector

object Step extends BobSerializable[Step] {

  final val DEFAULT_DURATION = 5000

  def serialize(step: Step) = step.serialize

  def deserialize(serialized: Map[String, Any]) = Step(
    serialized("duration" ).asInstanceOf[Int],
    serialized("positions").asInstanceOf[Vector[Int]]
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
    "positions" -> positions
  )

}
