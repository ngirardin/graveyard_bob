package com.protogenefactory.ioiomaster.client.connections

import com.protogenefactory.ioiomaster.client.models.Project
import com.protogenefactory.ioiomaster.server.services.ServerService
import org.scaloid.common._

class LocalConnection(serverService: LocalServiceConnection[ServerService]) extends Connection with TagUtil {

  override def playProject(project: Project) {

    info(s"LocalConnection.playProject() project=${project.id}")

    serverService.run(s =>
      s.playProject(project)
    )

  }

  override def playStep(project: Project, stepIndex: Int) {

    info(s"LocalPosition.playPosition() project=${project.id}, stepIndex=$stepIndex")

    serverService.run(s =>
      s.playStep(project, stepIndex)
    )

  }

  override def hasVideo: Boolean = false

  override def ping(): Boolean = true

  override def getSounds: Seq[String] =
    serverService(s => s.getSounds, throw new RuntimeException("Can't get the sounds"))

}
