package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import com.benoj.janus.PersistentLoggingActor
import com.benoj.janus.workflow.WorkflowActor
import com.benoj.janus.workflow.WorkflowActor.Commands.{ProgressUnit, RegressUnit}
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage

import scala.concurrent.ExecutionContext

trait WorkFlow extends JanusEventProcessing { self: PersistentLoggingActor =>

  implicit val executionContext: ExecutionContext
  implicit val timeout: Timeout

  protected[this] def stages: Seq[WorkflowStage]

  def workFlow: ActorRef = {
    val name = s"workflow-${this.self.path.name}"
    context.child(name) match {
      case None => context.actorOf(Props(classOf[WorkflowActor], stages), name)
      case Some(actorRef) => actorRef
    }
  }

  def workflowReceive: Receive = {
    case msg@ProgressUnit(unit) => workFlow forward msg
    case msg@RegressUnit(unit) => workFlow forward msg
  }

  override def processCommand: Receive = workflowReceive orElse super.processCommand


}
