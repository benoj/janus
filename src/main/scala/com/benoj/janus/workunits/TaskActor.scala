package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, Props}
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.{Assignee, Attributes, Created, Watchable}


class TaskActor(name: String = "", description: String = "") extends Actor
  with Created
  with Attributes
  with Watchable
  with Assignee
  with ActorLogging {

  log.info("Starting Task Actor")

  override def attributes = Map("name" -> name, "description" -> description)

  override def postCreation: Receive = PartialFunction.empty
}

object TaskActor{
  def props(name: String = "", description: String = "") = Props(new TaskActor(name, description))
}
