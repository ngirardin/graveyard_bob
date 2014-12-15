package com.protogenefactory.ioiomaster.client.connections

import com.protogenefactory.ioiomaster.client.models.Project

import scala.concurrent.Future

trait Connection extends Playable {

  def ping(): Future[Boolean]

  def playProject(project: Project)

  def playStep(project: Project, stepIndex: Int)

}
