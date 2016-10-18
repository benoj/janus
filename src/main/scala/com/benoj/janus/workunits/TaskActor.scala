package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, Props}
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.{Assignee, Attributes, Watchable}

class TaskActor(name: String = "", description: String = "") extends Actor
  with Attributes
  with Watchable
  with Assignee
  with ActorLogging {

  log.info("Starting Task Actor")

  override def attributes = Seq("name" -> name, "description" -> description)

  override def receive: Receive = behaviorReceive

}

object TaskActor{
  def props(name: String = "", description: String = "") = Props(new TaskActor(name, description))
}
