package com.protogenefactory.ioiomaster.client.models

import com.protogenefactory.ioiomaster.client.models.ServoConfig._
import com.protogenefactory.ioiomaster.client.models.json.BobJsonProtocol.servoConfigFormat
import spray.json._

object ServoConfig {

  /**
   * The IOIO output ports
   */
  final val PERIPHERAL_PORTS = Vector(1, 2, 3, 4, 5, 6, 7, 10, 11 /*, 12, 13, 14, 18, 19, 20, 21, 22, 23, 24, 25, 26*/)

  /**
   * The timings in microseconds for the min and max positions
   */
  final val TIMING_RANGE = 550 to 2500

  final val FREQUENCY = 50

  def deserialize(json: JsValue): ServoConfig = json.convertTo[ServoConfig]

}

case class ServoConfig(

  servo   : String,   // The servo name
  pin     : Int       // The IOIO pin

) {

  servo.ensuring(_.nonEmpty)

  // Check that the port is a peripheral port
  assert(PERIPHERAL_PORTS.contains(pin), "Invalid IOIO pin")

}
