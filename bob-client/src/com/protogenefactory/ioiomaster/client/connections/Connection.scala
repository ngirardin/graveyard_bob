package com.protogenefactory.ioiomaster.client.connections

trait Connection extends Playable {

  def ping(): Boolean

}
