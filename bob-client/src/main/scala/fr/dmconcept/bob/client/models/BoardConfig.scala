package fr.dmconcept.bob.client.models

import fr.dmconcept.bob.client.models.json.BobJsonProtocol._
import java.util.UUID
import spray.json._

object BoardConfig extends {

  def apply(name: String, servoConfigs: Vector[ServoConfig]): BoardConfig = {
    BoardConfig(UUID.randomUUID.toString, name, servoConfigs)
  }

  def deserialize(json: JsValue) = json.convertTo[BoardConfig]

}

case class BoardConfig(

    id           : String,

    name         : String,

    servoConfigs : Vector[ServoConfig]

) {

  assert(!name.isEmpty        , "Empty name"         )
  assert(servoConfigs.nonEmpty, "Empty servo configs")

}
