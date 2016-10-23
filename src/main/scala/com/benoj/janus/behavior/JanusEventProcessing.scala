package com.benoj.janus.behavior

import akka.actor.Actor._
import akka.persistence.RecoveryCompleted
import cats.data.Xor
import com.benoj.janus.Events.{JanusEvent, _}
import com.benoj.janus.PersistentLoggingActor


trait  CommandProcess {
  def processCommand: Receive = PartialFunction.empty
}

trait JanusEventProcessing extends CommandProcess {self: PersistentLoggingActor =>
  override def persistenceId: String = this.self.path.name

  def processEvent: ReceiveEvent = PartialFunction.empty

  override def receiveRecover: Receive = {
    case event: JanusEvent => processEvent(event)
    case RecoveryCompleted => log.info("Recovered Actor")
  }

  def processEventAndNotifySender(event: JanusEvent) = processEvent(event) match {
    case Xor.Right(_) =>
      sender() ! "OK"
    case Xor.Left(e) =>
      sender() ! e
  }

  override def receiveCommand: Receive = processCommand orElse super.processCommand

}
