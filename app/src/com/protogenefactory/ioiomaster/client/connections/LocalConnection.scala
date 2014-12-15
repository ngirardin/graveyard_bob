package com.protogenefactory.ioiomaster.client.connections

import com.protogenefactory.ioiomaster.client.models.Project

import com.protogenefactory.ioiomaster.server.services.ServerService
import org.scaloid.common._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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

  override def ping(): Future[Boolean] = Future(true)

}
