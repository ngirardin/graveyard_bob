package com.protogenefactory.ioiomaster.client.models.json

import com.protogenefactory.ioiomaster.client.models.{ServoConfig, Step, BoardConfig, Project}
import spray.json.DefaultJsonProtocol

object BobJsonProtocol extends DefaultJsonProtocol {

  /*
  // Need to be ordered by dependencies order
  implicit val servoConfigFormat = jsonFormat2(ServoConfig.apply)
  implicit val boardConfigFormat = jsonFormat3(BoardConfig.apply)
  implicit val stepFormat        = jsonFormat2(Step.apply       )
  implicit val projectFormat     = jsonFormat4(Project.apply    )
  */

  // Workaround for issue https://github.com/spray/spray-json/issues/53
  implicit val servoConfigFormat = jsonFormat[String, Int, ServoConfig] (
    ServoConfig.apply, "servo", "pin"
  )

  implicit val boardConfigFormat = jsonFormat[String, String, Vector[ServoConfig], BoardConfig](
    BoardConfig.apply, "id", "name", "servoConfigs"
  )

  implicit val stepFormat        = jsonFormat[Int, Vector[Int], Step](
    Step.apply, "duration", "positions"
  )

  implicit val projectFormat     = jsonFormat[String, String, BoardConfig, Vector[Step], Project](
    Project.apply, "id", "name", "boardConfig", "steps"
  )

}

