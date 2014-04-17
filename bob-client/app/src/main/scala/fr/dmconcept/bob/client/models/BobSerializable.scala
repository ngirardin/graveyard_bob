package fr.dmconcept.bob.client.models

import scala.util.parsing.json.{JSON, JSONObject}

/**
 * Indicate the class can be serialized as a mpa
 */
trait BobSerializable[A] {

  def toJson(objectToSerialize: A): String = JSONObject(serialize(objectToSerialize)).toString()

  def serialize(objectToSerialize: A): Map[String, Any]

  def deserialize(serialized: Map[String, Any]) : A

}
