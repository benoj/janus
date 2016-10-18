package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.{Attributes, WorkFlow}
import com.benoj.janus.oraganisation.IdActor
import com.benoj.janus.oraganisation.IdActor.Messages.{GetNextId, Id}
import com.benoj.janus.suppliers.Actors.IdSupplier
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage
import com.benoj.janus.workunits.ProjectActor.Messages.{CreateNewStoryInBacklog, CreatedStory, StoryNotFound, UpdateStory}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

class ProjectActor(name: String, description: String)(implicit val timeout: Timeout, val executionContext: ExecutionContext) extends Actor
  with Attributes
  with WorkFlow
  with ActorLogging {

  implicit val idSupplier: IdSupplier = IdSupplier(context.actorOf(Props(classOf[IdActor], name), s"project-$name-id-supplier"))

  override def attributes = Seq("name" -> name, "description" -> description)

  override def stages = Seq(WorkflowStage("analysis"), WorkflowStage("doing"))

  private val backlog: mutable.Map[Id, ActorRef] = mutable.Map.empty

  override def receive: Receive = projectReceive orElse behaviorReceive

  def projectReceive: Receive = {
    case UpdateStory(id, msg) =>
      log.info(s"Update project story $id with $msg")
      backlog.get(id) match {
        case None => sender() ! StoryNotFound
        case Some(story) => story forward msg
      }
    case CreateNewStoryInBacklog(storyName, storyDescription) =>
      val respondTo = sender()
      idSupplier.actor ? GetNextId onSuccess {
        case id@Id(_) =>
          log.info(s"Adding story $id to backlog")
          backlog(id) = context.actorOf(StoryActor.props(storyName, storyDescription), s"story-${id.id}")
          respondTo ! CreatedStory(id)
      }
  }

}

object ProjectActor {
  def props(name: String = "", description: String = "")(implicit timeout: Timeout, executionContext: ExecutionContext) = Props(new ProjectActor(name, description))

  object Messages {

    case class CreatedStory(id: Id)

    case class CreateNewStoryInBacklog(name: String, description: String)

    case class UpdateStory(id: Id, msg: Any)

    case object StoryNotFound

  }

}
