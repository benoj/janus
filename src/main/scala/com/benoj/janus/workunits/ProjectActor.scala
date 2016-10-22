package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.Created.Create
import com.benoj.janus.behavior.{Attributes, Created, WorkFlow}
import com.benoj.janus.organisation.IdActor
import com.benoj.janus.organisation.IdActor.Messages.{GetNextId, Id}
import com.benoj.janus.suppliers.Actors.IdSupplier
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage
import com.benoj.janus.workunits.ProjectActor.Messages.{CreateNewStoryInBacklog, StoryNotFound, UpdateStory}

import scala.concurrent.ExecutionContext

class ProjectActor(description: String)(implicit val timeout: Timeout, val executionContext: ExecutionContext) extends Actor
  with Created
  with Attributes
  with WorkFlow
  with ActorLogging {

  log.info(s"Creating project ${self.path.name}")
  implicit val idSupplier: IdSupplier = IdSupplier(context.actorOf(Props(classOf[IdActor], self.path.name), s"project-${self.path.name}-id-supplier"))

  override def attributes = Map("name" -> self.path.name, "description" -> description)

  override def stages = Seq(WorkflowStage("analysis"), WorkflowStage("doing"))

  override def postCreation: Receive = {
    case UpdateStory(id, msg) =>
      log.info(s"Update project story $id with $msg")
      context.child(id) match {
        case None =>
          sender() ! StoryNotFound
        case Some(story) =>
          story forward msg
      }
    case CreateNewStoryInBacklog(storyName, storyDescription) =>
      val respondTo = sender()
      idSupplier.actor ? GetNextId onSuccess {
        case id@Id(_) =>
          log.info(s"Adding story $id to backlog")
          val name: String = s"story-${id.id}"
          val story: ActorRef = context.actorOf(StoryActor.props(storyName, storyDescription), name)
          story tell(Create(name), respondTo)
      }
  }

}

object ProjectActor {
  def props(description: String = "")(implicit timeout: Timeout, executionContext: ExecutionContext) = Props(new ProjectActor(description))

  object Messages {

    case class CreateNewStoryInBacklog(name: String, description: String)

    case class UpdateStory(id: String, msg: Any)

    case object StoryNotFound

  }

}
