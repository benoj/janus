package com.benoj.janus.workunits

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.Notification.Messages.NotificationMessage
import com.benoj.janus.behavior.Watchable.Messages.NotifyWatchers
import com.benoj.janus.behavior.{Assignee, Attributes, Watchable}
import com.benoj.janus.workunits.StoryActor.Messages.{CreateTask, CreatedTask}

import scala.collection.mutable


object StoryActor {
  object Messages {
    case class CreateTask(name: String = "", description: String = "")
    case class CreatedTask(uuid: UUID)
  }
}

class StoryActor(name: String = "", description: String = "") extends Actor with Attributes with Watchable with Assignee with ActorLogging {

  log.info("Starting Story Actor")

  initAttributes(
    "name" -> name,
    "description" -> description
  )

  val tasks: mutable.Map[UUID, ActorRef] = mutable.Map.empty

  override def receive: Receive = taskReceive orElse behaviorReceive

  private def taskReceive: Receive = {
    case CreateTask(taskName,taskDescription) =>
      val uuid = UUID.randomUUID
      tasks(uuid) = context.actorOf(Props(classOf[TaskActor], taskName, taskDescription))
      self ! NotifyWatchers(s"Task $uuid added to story")
      sender() ! CreatedTask(uuid)
    case NotificationMessage(message) =>
      log.info("Received Notification")
      message match {
        case _ => self ! NotifyWatchers(message)
      }
  }

}
