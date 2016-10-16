package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.benoj.janus.behaviour.Attributes.implicits._
import com.benoj.janus.behaviour.{Attributes, Watchable}

class TaskActor extends Actor with Attributes with Watchable with ActorLogging {

  private var assignee: Seq[ActorRef] = null

  log.info("Starting Task Actor")

  initAttributes(
    "name" -> "",
    "description" -> ""
  )

  override def receive: Receive = receiveAttributes orElse receiveWatchers

}
