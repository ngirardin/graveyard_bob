package com.protogenefactory.ioiomaster.client.connections

import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project}

trait Connection {

  def playProject(project: Project)

  def playPosition(boardConfig: BoardConfig, positions: Array[Int])

}
