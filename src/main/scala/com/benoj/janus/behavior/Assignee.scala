package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.benoj.janus.behavior.Assignee.Messages.UpdateAssignee


object Assignee {
  object Messages {
    case class UpdateAssignee(assignee: ActorRef)
  }
}

trait Assignee extends BehaviorReceive{ self: Actor with ActorLogging =>
  private[this] var assignee: Option[ActorRef] = None

  override def behaviorReceive: Receive = assigneeReceive orElse super.behaviorReceive

  private def assigneeReceive: Receive = {
    case UpdateAssignee(actorRef) =>
      log.info(s"updating assignee")
      assignee = Some(actorRef)
  }
}
