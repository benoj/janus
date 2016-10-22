package com.benoj.janus.workunits

import akka.actor.{Actor, ActorLogging, Props}
import com.benoj.janus.behavior.Attributes.implicits._
import com.benoj.janus.behavior.{Assignee, Attributes, BehaviorReceive, Watchable}
import com.benoj.janus.workunits.Created.{Create, NotFound, `Exist?`}

object Created {
  case object Create
  case object `Exist?`
  case object NotFound
  case object Created
}


trait Created { self: Actor with ActorLogging with BehaviorReceive =>

  def postCreation: Receive = {
    case `Exist?` => Created
  }

  override def receive: Receive = {
    case Create =>
      log.info(s"Creating Default ${this.getClass.getSuperclass.getName}")
      context.become(postCreation orElse behaviorReceive)
      sender() ! Created
    case _ =>
      log.info(s"Attempting to send message with non existing actor.")
      sender() ! NotFound
  }

}

class TaskActor(name: String = "", description: String = "") extends Actor
  with Created
  with Attributes
  with Watchable
  with Assignee
  with ActorLogging {

  log.info("Starting Task Actor")

  override def attributes = Map("name" -> name, "description" -> description)

}

object TaskActor{
  def props(name: String = "", description: String = "") = Props(new TaskActor(name, description))
}
