package com.protogenefactory.ioiomaster.client.connections

trait Connection extends Playable {

  def hasVideo: Boolean

  def ping(): Boolean

}
