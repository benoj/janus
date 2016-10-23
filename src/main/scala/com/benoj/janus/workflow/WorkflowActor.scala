package com.benoj.janus.workflow

import akka.actor.Actor.Receive
import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted}
import cats.data.Xor
import com.benoj.janus.Events._
import com.benoj.janus.PersistentLoggingActor
import com.benoj.janus.behavior.Created.NotFound
import com.benoj.janus.behavior.JanusEventProcessing
import com.benoj.janus.workflow.WorkflowActor.Commands._
import com.benoj.janus.workflow.WorkflowActor.Events.{WorkUnitAdded, WorkUnitProgressed, WorkUnitRegressed, WorkUnitRemoved}
import com.benoj.janus.workflow.WorkflowActor.{WorkflowStage, WorkflowState}


object WorkflowActor {

  object Commands {

    case class AddWorkUnit(id: String)

    case class DeleteWorkUnit(id: String)

    case class ProgressUnit(id: String)

    case class RegressUnit(id: String)

  }

  object Events {

    case class WorkUnitAdded(id: String) extends JanusEvent

    case class WorkUnitRemoved(id: String) extends JanusEvent

    case class WorkUnitProgressed(id: String) extends JanusEvent

    case class WorkUnitRegressed(id: String) extends JanusEvent

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

    def contains(workUnit: String) = workflowStages.exists(_.workUnits.contains(workUnit))

  }

}






class WorkflowActor(initialStages: Seq[WorkflowStage]) extends PersistentLoggingActor
  with JanusEventProcessing {

  log.info("Created Workflow")
  private var state = WorkflowState(initialStages)

  override def processEvent: ReceiveEvent = {
    case WorkUnitAdded(id: String) => Xor.Right {
      state = state.add(id)
    }
    case WorkUnitRemoved(id: String) => state.remove(id) match {
      case Xor.Left(e) => Xor.Left(Failed(e))
      case Xor.Right(s) => Xor.Right {
        state = s
      }
    }
    case WorkUnitProgressed(id: String) => state.progress(id) match {
      case Xor.Left(e) => Xor.Left(Failed(e))
      case Xor.Right(s) => Xor.Right {
        state = s
      }
    }
    case WorkUnitRegressed(id: String) => state.regress(id) match {
      case Xor.Left(e) => Xor.Left(Failed(e))
      case Xor.Right(s) => Xor.Right {
        state = s
      }
    }

  }

  override def processCommand: Receive = {
    case AddWorkUnit(id) =>
      log.info(s"Adding $id to workflow")
      persist(WorkUnitAdded(id))(processEventAndNotifySender)

    case DeleteWorkUnit(id) =>
      log.info(s"Removing $id")
      if (state.contains(id)) persist(WorkUnitRemoved(id))(processEventAndNotifySender) else log.error("Unknown Id")

    case ProgressUnit(id) =>
      log.info(s"Progressing $id")
      if (state.contains(id)) persist(WorkUnitProgressed(id))(processEventAndNotifySender) else log.error("Unknown Id")

    case RegressUnit(id) =>
      log.info(s"Progressing $id")
      if (state.contains(id)) persist(WorkUnitRegressed(id))(processEventAndNotifySender) else log.error("Unknown Id")

  }

}