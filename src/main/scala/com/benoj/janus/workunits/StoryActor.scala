package com.benoj.janus.workunits

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.Notification.Messages.NotificationMessage
import com.benoj.janus.behavior.Watchable.Messages.NotifyWatchers
import com.benoj.janus.behavior.{Assignee, Attributes, Watchable, WorkFlow}
import com.benoj.janus.workflow.WorkflowActor.Messages.AddWorkUnit
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage
import com.benoj.janus.workunits.StoryActor.Messages.{CreateTask, CreatedTask}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object StoryActor {
  object Messages {
    case class CreateTask(name: String = "", description: String = "")
    case class CreatedTask(actorRef: ActorRef)
  }

}

class StoryActor(name: String = "", description: String = "") extends Actor
  with Attributes
  with Watchable
  with Assignee
  with WorkFlow
  with ActorLogging {

  log.info("Starting Story Actor")

  initAttributes(
    "name" -> name,
    "description" -> description
  )

  override val stages = Seq(WorkflowStage("analysis"), WorkflowStage("doing"))

  val tasks: mutable.Map[UUID, ActorRef] = mutable.Map.empty

  override def receive: Receive = taskReceive orElse behaviorReceive

  private def taskReceive: Receive = {
    case CreateTask(taskName, taskDescription) =>
      val task: ActorRef = context.actorOf(Props(classOf[TaskActor], taskName, taskDescription))
      implicit val timeout = Timeout(10, TimeUnit.SECONDS)
      val future: Future[Any] = workFlow ? AddWorkUnit(task)
      val responder = sender()
      future.onComplete {
        case Success(_) =>
          tasks(UUID.randomUUID) = task
          self ! NotifyWatchers(s"Task ${UUID.randomUUID} added to story")
          responder ! CreatedTask(task)
        case Failure(e) =>
          log.error("Failed to create workflow")
      }
    case NotificationMessage(message) =>
      log.info("Received Notification")
      message match {
        case _ => self ! NotifyWatchers(message)
      }
  }

}
