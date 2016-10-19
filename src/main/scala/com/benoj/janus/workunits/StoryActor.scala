package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.{Assignee, Attributes, Watchable, WorkFlow}
import com.benoj.janus.oraganisation.IdActor.Messages.{GetNextId, Id}
import com.benoj.janus.suppliers.Actors.IdSupplier
import com.benoj.janus.workflow.WorkflowActor.Messages.AddWorkUnit
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage
import com.benoj.janus.workunits.StoryActor.Messages.{CreateTask, CreatedTask}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class StoryActor(name: String = "", description: String = "")
                (implicit val timeout: Timeout, val executionContext: ExecutionContext, idSupplier: IdSupplier) extends Actor
  with Attributes
  with Watchable
  with Assignee
  with WorkFlow
  with ActorLogging {

  log.info("Starting Story Actor")

  override def attributes = Map("name" -> name, "description" -> description)

  override val stages = Seq(WorkflowStage("analysis"), WorkflowStage("doing"))

  private val tasks: mutable.Map[Id, ActorRef] = mutable.Map.empty

  override def receive: Receive = taskReceive orElse behaviorReceive

  private def taskReceive: Receive = {
    case CreateTask(taskName, taskDescription) =>
      log.info("Story received create task")
      val responder = sender()
      idSupplier.actor ? GetNextId onSuccess {
        case id@Id(_) =>
          val task: ActorRef = context.actorOf(TaskActor.props(taskName, taskDescription), s"task-${id.id}")
          workFlow ? AddWorkUnit(id) onComplete  {
            case Success(_) =>
              tasks(id) = task
              responder ! CreatedTask(id)
              notifyWatchers(s"Task $id added to story")
            case Failure(e) => log.error(e, "add work unit failed")
          }
      }
  }

}


object StoryActor {

  def props(name: String = "", description: String = "")
           (implicit timeout: Timeout, executionContext: ExecutionContext,idSupplier: IdSupplier) = Props(new StoryActor(name, description))

  object Messages {

    case class CreateTask(name: String = "", description: String = "")

    case class CreatedTask(id: Id)

  }

}