package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, Props}
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.Notification.Messages.NotificationMessage
import com.benoj.janus.behavior.Watchable.Messages.NotifyWatchers
import com.benoj.janus.behavior.{Assignee, Attributes, Watchable}

class TaskActor(name: String = "", description: String = "") extends Actor
  with Attributes
  with Watchable
  with Assignee
  with ActorLogging {

  log.info("Starting Task Actor")

  override def attributes = Seq("name" -> name, "description" -> description)

  override def receive: Receive = taskReceive orElse behaviorReceive

  private def taskReceive: Receive = {
    case NotificationMessage(message) =>
      log.info("Received Notification")
      message match {
        case _ => self ! NotifyWatchers(message)
      }
  }
}

object TaskActor{
  def props(name: String = "", description: String = "") = Props(new TaskActor(name, description))
}
