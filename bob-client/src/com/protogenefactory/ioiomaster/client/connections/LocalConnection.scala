package com.protogenefactory.ioiomaster.client.connections

import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project}
import com.protogenefactory.ioiomaster.server.services.ServerService
import org.scaloid.common._

class LocalConnection(serverService: LocalServiceConnection[ServerService]) extends Connection with TagUtil {

  override def playProject(project: Project) {

    info(s"playProject() project=${project.id}")

    serverService.run(s =>
      s.playProject(project)
    )

  }

  override def playPosition(boardConfig: BoardConfig, positions: Array[Int]) {

    //TODO play local position
    info(s"LocalPosition.playPosition() boardConfig=$boardConfig, positions=$positions")
    throw new NotImplementedError("LocalConnection.playPosition")

  }

  override def hasVideo(): Boolean = false

  override def ping(): Boolean = true

  override def getSounds: Seq[String] =
    serverService(s => s.getSounds, throw new RuntimeException("Can't get the sounds"))

}
