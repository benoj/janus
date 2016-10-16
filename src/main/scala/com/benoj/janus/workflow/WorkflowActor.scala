package com.benoj.janus.workflow

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.benoj.janus.workflow.WorkflowActor.Messages._
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage

import scala.collection.mutable

object WorkflowActor {
  case class WorkflowStage(name: String, workUnits: mutable.Buffer[ActorRef] = mutable.Buffer.empty)

  object Messages {
    case class AddWorkUnit(unit: ActorRef)
    case class DeleteWorkUnit(unit: ActorRef)
    case class ProgressUnit(unit: ActorRef)
    case class RegressUnit(unit: ActorRef)
  }
}

class WorkflowActor(initialStages: Seq[WorkflowStage]) extends Actor with ActorLogging {

  log.info("Created Workflow")
  private val stages: mutable.Buffer[WorkflowStage] = initialStages.toBuffer

  override def receive: Receive = {
    case AddWorkUnit(workUnit) =>
      log.info("Adding unit to workflow")
      stages.head.workUnits.append(workUnit)
      sender() ! "OK"
    case DeleteWorkUnit(workUnit) => stages.find(_.workUnits.contains(workUnit)) match {
      case Some(stage) =>
        log.info("Removing unit")
        stage.workUnits.remove(stages.indexOf(stage))
      case None =>
    }
    case ProgressUnit(workUnit) =>
      log.info("Progressing Unit")
      stages.find(_.workUnits.contains(workUnit)) match {
        case None =>
        case Some(stage) =>
          val currentStage =  stages.indexOf(stage)
          stages.length match {
            case i if i > currentStage =>
              stages(currentStage + 1).workUnits.append(workUnit)
              stage.workUnits.remove(stage.workUnits.indexOf(workUnit))
            case _ =>
          }
      }
    case RegressUnit(workUnit) =>
      log.info("Regressing Unit")
      stages.find(_.workUnits.contains(workUnit)) match {
        case None =>
        case Some(stage) =>
          val currentStage =  stages.indexOf(stage)
          if(currentStage > 0) {
            stages(currentStage - 1).workUnits.append(workUnit)
            stages(currentStage).workUnits.remove(currentStage)
          }
      }
  }
}