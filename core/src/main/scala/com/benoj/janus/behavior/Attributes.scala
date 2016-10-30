package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging}
import cats.data.Xor
import com.benoj.janus.Events._
import com.benoj.janus.PersistentLoggingActor
import com.benoj.janus.behavior.Attributes.Events.AttributeUpdated
import com.benoj.janus.behavior.Watchable.Commands.NotifyWatchers

import scala.collection._
import scala.language.implicitConversions

object Attributes {

  object implicits {
    implicit def stringToStringAttributeValue(value: String): StringAttributeValue = StringAttributeValue(value)
  }

  sealed abstract class AttributeValue(val value: Any)

  case class StringAttributeValue(override val value: String) extends AttributeValue(value)

  object Commands {

    case class UpdateAttribute(attributeName: String, attributeValue: AttributeValue)

  }

  object Events {

    case class AttributeUpdated(attributeName: String, attributeValue: AttributeValue) extends JanusEvent

  }

  abstract class FailedToUpdateAttribute

  case object IncompatableTypeUpdate extends FailedToUpdateAttribute

  case class UnknownAttribute(attributeName: String) extends FailedToUpdateAttribute

  case class AttributeState(attributes: Map[String, AttributeValue] = Map.empty) {

    def update(key: String, updateValue: AttributeValue): Xor[FailedToUpdateAttribute, AttributeState] = {
      attributes.get(key) match {
        case None => Xor.left(UnknownAttribute(key))
        case Some(currentValue) => (currentValue, updateValue) match {
          case (_: StringAttributeValue, u: StringAttributeValue) => Xor.right(copy(attributes.updated(key, u)))
          case _ => Xor.left(IncompatableTypeUpdate)
        }
      }
    }
  }

}

trait Attributes extends JanusEventProcessing {
  self: PersistentLoggingActor =>

  import com.benoj.janus.behavior.Attributes.Commands._
  import com.benoj.janus.behavior.Attributes._

  def attributes: Map[String, AttributeValue]

  private var state: AttributeState = AttributeState(attributes)

  override def processEvent: ReceiveEvent = {
    case AttributeUpdated(attributeName, newValue) => state.update(attributeName, newValue) match {
      case Xor.Right(updatedState) =>
        Xor.Right {
          state = updatedState
        }
      case Xor.Left(e) =>
        log.error(s"Error updating attribute $e")
        Xor.Left(Failed(e))
    }
  }

  override def processCommand: Receive = attributeProcessCommand orElse super.processCommand

  def attributeProcessCommand: Receive = {
    case UpdateAttribute(attributeName, newValue) =>
      processEventAndNotifySender(AttributeUpdated(attributeName, newValue))
      this.self ! NotifyWatchers(s"Attribute $attributeName Updated $newValue")

  }
}

