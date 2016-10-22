package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import com.benoj.janus.workflow.WorkflowActor
import com.benoj.janus.workflow.WorkflowActor.Messages.{ProgressUnit, RegressUnit}
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage

import scala.concurrent.ExecutionContext

trait WorkFlow extends BehaviorReceive{ self: Actor with ActorLogging =>

  implicit val executionContext: ExecutionContext
  implicit val timeout: Timeout

  protected[this] def stages: Seq[WorkflowStage]

  lazy val workFlow: ActorRef = context.actorOf(Props(classOf[WorkflowActor], stages))

  override def behaviorReceive: Receive = workflowReceive orElse super.behaviorReceive

  def workflowReceive: Receive = {
    case msg@ProgressUnit(unit) =>workFlow forward msg
    case msg@RegressUnit(unit) => workFlow forward msg
  }

}
