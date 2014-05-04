package fr.dmconcept.bob.client.models

import ServoConfig._

object ServoConfig extends BobSerializable[ServoConfig] {

  /**
   * The IOIO output ports
   */
  final val PERIPHERAL_PORTS = Vector(1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 18, 19, 20, 21, 22, 23, 24, 25, 26)

  /**
   * The timings in microseconds for the min and max positions
   */
  final val TIMING_RANGE = 550 to 2500

  final val FREQUENCY = 50

  def serialize(servoConfig: ServoConfig) = servoConfig.serialize

  def deserialize(serialized: Map[String, Any]): ServoConfig = {

    def toInt(field: String) = serialized(field).asInstanceOf[Double].toInt

    new ServoConfig(toInt("port"), (toInt("minTiming"), toInt("maxTiming")))
  }

}

case class ServoConfig(

  // The IOIO port
  port      : Int,

  // The servo timings for the start and end position
  timings   : (Int, Int)

) {

  // Check that the port is a peripheral port
  assert(PERIPHERAL_PORTS.contains(port), "Invalid IOIO port")

  // Check that the timings are in the timing limits
  assert(TIMING_RANGE.contains(timings._1), "Start timing out of range"                           )
  assert(TIMING_RANGE.contains(timings._2), "End timing out of range"                             )
  assert(timings._1 < timings._2          , "The end timing must be greater than the start timing")

  def serialize: Map[String, Any] = Map(
    "port"      -> port     ,
    "minTiming" -> timings._1,
    "maxTiming" -> timings._2
  )

  def toJson = ServoConfig.toJson(this)

}
