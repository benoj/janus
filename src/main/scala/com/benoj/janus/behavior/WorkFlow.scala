package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.benoj.janus.workflow.WorkflowActor
import com.benoj.janus.workflow.WorkflowActor.Messages.{ProgressUnit, RegressUnit}
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage


trait WorkFlow extends BehaviorReceive{ self: Actor with ActorLogging =>

  var workFlow: ActorRef = null

  def initWorkflow(stages: WorkflowStage*) = {
    log.info("Initializing workflow")
    workFlow = context.actorOf(Props(classOf[WorkflowActor], stages))
  }

  override def behaviorReceive: Receive = workflowReceive orElse super.behaviorReceive


  def workflowReceive: Receive = {
    case msg@ProgressUnit(unit: ActorRef) => workFlow ! msg
    case msg@RegressUnit(unit: ActorRef) => workFlow ! msg
  }

}
