package com.benoj.janus.workflow

import akka.actor.{Actor, ActorLogging}
import cats.data.Xor
import com.benoj.janus.behavior.Created.NotFound
import com.benoj.janus.workflow.WorkflowActor.Messages._
import com.benoj.janus.workflow.WorkflowActor.{WorkflowStage, WorkflowState}

object WorkflowActor {

  object Messages {

    case class AddWorkUnit(id: String)

    case class DeleteWorkUnit(id: String)

    case class ProgressUnit(id: String)

    case class RegressUnit(id: String)

  }

  case class WorkflowStage(name: String, workUnits: Set[String] = Set.empty) {
    def add(workUnit: String) = copy(name, workUnits + workUnit)

    def remove(workUnit: String) = copy(name, workUnits - workUnit)
  }

  case class WorkflowState(workflowStages: Seq[WorkflowStage]) {
    def add(workUnit: String): WorkflowState = copy(workflowStages.tail.+:(workflowStages.head.add(workUnit)))

    def remove(workUnit: String): Xor[NotFound.type, WorkflowState] = Xor.fromOption(workflowStages
      .find(_.workUnits.contains(workUnit)).map { stage =>
      workflowStages.splitAt(workflowStages.indexOf(stage)) match {
        case (before, current :: after) => copy(before ++ after.+:(current.remove(workUnit)))
        case _ => this
      }
    }, NotFound)

    def progress(workUnit: String): Xor[NotFound.type, WorkflowState] = Xor.fromOption(workflowStages
      .find(_.workUnits.contains(workUnit)).map { stage =>
      workflowStages.splitAt(workflowStages.indexOf(stage)) match {
        case (before, current :: (next :: after)) =>
          copy(before.:+(current.remove(workUnit)) ++ after.+:(next.add(workUnit)))
        case _ => this
      }
    }, NotFound)

    def regress(workUnit: String): Xor[NotFound.type, WorkflowState] = Xor.fromOption(workflowStages
      .find(_.workUnits.contains(workUnit)).map { stage =>
      workflowStages.splitAt(workflowStages.indexOf(stage)) match {
        case (start@_ :: previous :: Nil, current :: after) =>
          copy((start :+ previous.add(workUnit)) ++ after.+:(current.remove(workUnit)))
        case _ => this
      }
    }, NotFound)

  }

}

class WorkflowActor(initialStages: Seq[WorkflowStage]) extends Actor with ActorLogging {

  log.info("Created Workflow")
  private var state = WorkflowState(initialStages)

  override def receive: Receive = {
    case AddWorkUnit(id) =>
      log.info(s"Adding $id to workflow")
      state = state.add(id)
    case DeleteWorkUnit(id) =>
      state.remove(id) match {
        case Xor.Left(_) => log.error("Unknown Id")
        case Xor.Right(s) =>
          log.info(s"Removing $id")
          state = s
      }
    case ProgressUnit(id) =>
      state.progress(id) match {
        case Xor.Left(_) => log.error("Unknown Id")
        case Xor.Right(s) =>
          log.info(s"Progressing $id")
          state = s
          sender() ! "OK"
      }
    case RegressUnit(id) =>
      state.regress(id) match {
        case Xor.Left(_) => log.error("Unknown Id")
        case Xor.Right(s) =>
          log.info(s"Regressing $id")
          state = s
          sender() ! "OK"
      }
  }
}