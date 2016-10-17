package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.benoj.janus.workflow.WorkflowActor
import com.benoj.janus.workflow.WorkflowActor.Messages.{ProgressUnit, RegressUnit}
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage


trait WorkFlow extends BehaviorReceive{ self: Actor with ActorLogging =>

  protected[this] def stages: Seq[WorkflowStage]

  lazy val workFlow: ActorRef = context.actorOf(Props(classOf[WorkflowActor], stages))

  override def behaviorReceive: Receive = workflowReceive orElse super.behaviorReceive

  def workflowReceive: Receive = {
    case msg@ProgressUnit(unit) => workFlow ! msg
    case msg@RegressUnit(unit) => workFlow ! msg
  }

}
