package com.benoj.janus.organisation

import akka.actor.Actor
import com.benoj.janus.behavior.Created.NotFound
import com.benoj.janus.organisation.ProjectsActor.{Conflict, CreateProject, GetProject, GetProjects}
import com.benoj.janus.workunits.ProjectActor

object ProjectsActor {
  case object GetProjects
  case class GetProject(name: String)
  case class CreateProject(name: String, description: String)

  case object Conflict
}
class ProjectsActor extends Actor {
  override def receive: Receive = {
    case GetProjects =>
     sender() ! context.children.filter(child => child.path.name.startsWith("project-"))
    case GetProject(name: String) =>
      context.child(s"project-$name") match {
        case None => sender() ! NotFound
        case Some(project) => sender() ! project
      }
    case CreateProject(name, description) =>  context.child(s"project-$name") match {
      case Some(_) => sender() ! Conflict
      case None => sender() ! context.actorOf(ProjectActor.props(description), s"project-$name")
    }
  }
}
