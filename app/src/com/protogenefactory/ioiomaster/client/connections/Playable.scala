package com.protogenefactory.ioiomaster.client.connections

import com.protogenefactory.ioiomaster.client.models.Project

trait Playable {

  def playProject(project: Project)

  def playStep(project: Project, stepIndex: Int)

  def getSounds: Seq[String]

}
