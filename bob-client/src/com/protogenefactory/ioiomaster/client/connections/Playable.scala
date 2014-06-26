package com.protogenefactory.ioiomaster.client.connections

import com.protogenefactory.ioiomaster.client.models.{BoardConfig, Project}

trait Playable {

  def playProject(project: Project)

  def playPosition(boardConfig: BoardConfig, positions: Array[Int])

  def getSounds: Seq[String]

}
