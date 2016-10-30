package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef}
import cats.data.Xor
import com.benoj.janus.Events.{JanusEvent, ReceiveEvent}
import com.benoj.janus.PersistentLoggingActor
import com.benoj.janus.behavior.Assignee.Commands.UpdateAssignee
import com.benoj.janus.behavior.Assignee.Events.AssigneeUpdated


object Assignee {
  object Commands {
    case class UpdateAssignee(assignee: ActorRef)
  }

  object Events {
    case class AssigneeUpdated(assignee: ActorRef) extends JanusEvent
  }
}

trait Assignee extends JanusEventProcessing { self: PersistentLoggingActor =>
  private var assignee: Option[ActorRef] = None

  override def processEvent: ReceiveEvent = {
    case AssigneeUpdated(actorRef) =>
      log.info(s"updating assignee")
      Xor.Right { assignee = Some(actorRef) }
  }

  override def processCommand: Receive = assigneeProcessCommand orElse super.processCommand

  def assigneeProcessCommand: Receive = {
    case UpdateAssignee(actorRef) => processEventAndNotifySender(AssigneeUpdated(actorRef))
  }
}
