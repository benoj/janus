package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.Created.Create
import com.benoj.janus.behavior._
import com.benoj.janus.organisation.IdActor.Messages.{GetNextId, Id}
import com.benoj.janus.suppliers.Actors.IdSupplier
import com.benoj.janus.workflow.WorkflowActor.Commands.AddWorkUnit
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage
import com.benoj.janus.workunits.StoryActor.Messages.CreateTask

import scala.concurrent.ExecutionContext
import scala.util.Success

class StoryActor(name: String = "", description: String = "")
                (implicit val timeout: Timeout, val executionContext: ExecutionContext, idSupplier: IdSupplier) extends Actor
  with Created
  with Attributes
  with Watchable
  with Assignee
  with WorkFlow
  with ActorLogging {

  log.info("Starting Story Actor")

  override def attributes = Map("name" -> name, "description" -> description)

  override val stages = Seq(WorkflowStage("analysis"), WorkflowStage("doing"))

  override def postCreation = {
    case CreateTask(taskName, taskDescription) =>
      log.info("Story received create task")
      val responder = sender()
      idSupplier.actor ? GetNextId onComplete {
        case Success(Id(id)) => workFlow ? AddWorkUnit(id) onSuccess {
          case _ => context.actorOf(TaskActor.props(taskName, taskDescription), s"task-$id") tell(Create(id), responder)
        }
        case e@_ =>
          log.error(s"Unable to get next ID. Cause $e")
      }
  }
}


object StoryActor {

  def props(name: String = "", description: String = "")
           (implicit timeout: Timeout, executionContext: ExecutionContext, idSupplier: IdSupplier) = Props(new StoryActor(name, description))

  object Messages {

    case class CreateTask(name: String = "", description: String = "")

  }

}