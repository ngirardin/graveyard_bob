package com.protogenefactory.ioiomaster.client.connections

import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project}
import org.scaloid.common._

class LocalConnection extends Connection with TagUtil {

  override def playProject(project: Project) {

    //TODO play local project
    info(s"playProject() project=${project.id}")
    throw new NotImplementedError("LocalConnection.playProject")

  }

  override def playPosition(boardConfig: BoardConfig, positions: Array[Int]) {

    //TODO play local position
    info(s"LocalPosition.playPosition() boardConfig=$boardConfig, positions=$positions")
    throw new NotImplementedError("LocalConnection.playPosition")

  }

}
