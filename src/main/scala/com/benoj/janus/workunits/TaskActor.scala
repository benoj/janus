package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging}
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.{Assignee, Attributes, Watchable}

class TaskActor(name: String = "", description: String = "") extends Actor with Attributes with Watchable with Assignee with ActorLogging {

  log.info("Starting Task Actor")

  initAttributes(
    "name" -> name,
    "description" -> description
  )

  override def receive: Receive = behaviorReceive

}
