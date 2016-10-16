package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging}

trait BehaviorReceive { self: Actor with ActorLogging =>
  def behaviorReceive: Receive = {
    case _ => log.error(s"Unknown Message")
  }
}
