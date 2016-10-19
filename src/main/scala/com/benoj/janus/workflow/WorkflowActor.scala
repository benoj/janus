package com.benoj.janus.workflow

import akka.actor.{Actor, ActorLogging}
import com.benoj.janus.organisation.IdActor.Messages.Id
import com.benoj.janus.workflow.WorkflowActor.Messages._
import com.benoj.janus.workflow.WorkflowActor.WorkflowStage

import scala.collection.mutable

object WorkflowActor {
  case class WorkflowStage(name: String, workUnits: mutable.Buffer[Id] = mutable.Buffer.empty)

  object Messages {
    case class AddWorkUnit(id: Id)
    case class DeleteWorkUnit(id: Id)
    case class ProgressUnit(id: Id)
    case class RegressUnit(id: Id)
  }
}

class WorkflowActor(initialStages: Seq[WorkflowStage]) extends Actor with ActorLogging {

  log.info("Created Workflow")
  private val stages: mutable.Buffer[WorkflowStage] = initialStages.toBuffer

  override def receive: Receive = {
    case AddWorkUnit(id) =>
      log.info(s"Adding $id to workflow")
      stages.head.workUnits.append(id)
      sender() ! "OK"
    case DeleteWorkUnit(id) => stages.find(_.workUnits.contains(id)) match {
      case Some(stage) =>
        log.info(s"Removing $id")
        stage.workUnits.remove(stages.indexOf(stage))
      case None =>
    }
    case ProgressUnit(id) =>
      log.info(s"Progressing $id")
      val responder = sender()
      stages.find(_.workUnits.contains(id)) match {
        case None =>
        case Some(stage) =>
          val currentStage =  stages.indexOf(stage)
          stages.length match {
            case i if i > currentStage =>
              stages(currentStage + 1).workUnits.append(id)
              stage.workUnits.remove(stage.workUnits.indexOf(id))
              responder ! "OK"
            case _ =>
          }
      }
    case RegressUnit(id) =>
      log.info(s"Regressing $id")
      stages.find(_.workUnits.contains(id)) match {
        case None =>
        case Some(stage) =>
          val currentStage =  stages.indexOf(stage)
          if(currentStage > 0) {
            stages(currentStage - 1).workUnits.append(id)
            stages(currentStage).workUnits.remove(currentStage)
          }
      }
  }
}