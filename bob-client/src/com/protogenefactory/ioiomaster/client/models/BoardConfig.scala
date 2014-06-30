package com.protogenefactory.ioiomaster.client.models

import java.util.UUID

import com.protogenefactory.ioiomaster.client.models.json.BobJsonProtocol._
import spray.json._

object BoardConfig {

  /**
   * Max quantity of servo per board
   */
  final val MAX_SERVOS = ServoConfig.PERIPHERAL_PORTS.length

  def apply(name: String, servoConfigs: Seq[ServoConfig]): BoardConfig = {
    BoardConfig(UUID.randomUUID.toString, name, servoConfigs)
  }

  def deserialize(json: JsValue) = json.convertTo[BoardConfig]

}

case class BoardConfig(
  id           : String,
  name         : String,
  servoConfigs : Seq[ServoConfig]
) {

  assert(!name.isEmpty        , "Empty name"         )
  assert(servoConfigs.nonEmpty, "Empty servo configs")

  {
    //Check that the servo configs don't contain duplicate pins
    val ports = servoConfigs.map(_.pin)
    assert(ports.size == ports.distinct.size, "Expected only distinct ports")
  }

}
