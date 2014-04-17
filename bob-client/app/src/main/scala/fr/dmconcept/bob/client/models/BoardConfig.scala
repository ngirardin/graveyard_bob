package fr.dmconcept.bob.client.models

import java.util.UUID
import BoardConfig._
import scala.util.parsing.json.{JSONArray, JSONObject}

object BoardConfig extends BobSerializable[BoardConfig] {

  def serialize(boardConfig: BoardConfig) = boardConfig.serialize

  def deserialize(serialized: Map[String, Any]) = {

    val id           = serialized("id"          ).asInstanceOf[String]
    val name         = serialized("name"        ).asInstanceOf[String]
    val servoConfigs = serialized("servoConfigs").asInstanceOf[List[Map[String, Any]]]
      .map(ServoConfig.deserialize)
      .toVector

    BoardConfig(id, name, servoConfigs)

  }

  def apply(name: String, servoConfigs: Vector[ServoConfig]): BoardConfig = {
    BoardConfig(UUID.randomUUID.toString, name, servoConfigs)
  }

}

case class BoardConfig(

    id           : String,

    name         : String,

    servoConfigs : Vector[ServoConfig]

) {

  assert(!name.isEmpty        , "Empty name"         )
  assert(servoConfigs.nonEmpty, "Empty servo configs")

  def serialize = Map(
    "id"           -> id,
    "name"         -> name,
    "servoConfigs" -> JSONArray(servoConfigs.map(_.serialize).toList)
  )

  def toJson = BoardConfig.toJson(this)

}
