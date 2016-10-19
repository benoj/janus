package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging}
import cats.data.Xor
import com.benoj.janus.behavior.Watchable.Messages.NotifyWatchers

import scala.collection._
import scala.language.implicitConversions

object Attributes {

  object implicits {
    implicit def stringToStringAttributeValue(value: String): StringAttributeValue = StringAttributeValue(value)
  }

  sealed abstract class AttributeValue(val value: Any)
  case class StringAttributeValue(override val value: String) extends AttributeValue(value)

  object Messages {
    case class UpdateAttribute(attributeName: String, attributeValue: AttributeValue)
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

trait Attributes extends BehaviorReceive {
  self: Actor with ActorLogging =>

  import com.benoj.janus.behavior.Attributes.Messages._
  import com.benoj.janus.behavior.Attributes._

  def attributes: Map[String, AttributeValue]

  private var state: AttributeState = AttributeState(attributes)

  override def behaviorReceive = receiveAttributes orElse super.behaviorReceive

  private def receiveAttributes: Receive = {
    case UpdateAttribute(attributeName, newValue) => state.update(attributeName, newValue) match {
        case Xor.Right(updatedState) =>
          log.info("Updating string attribute")
          this.self ! NotifyWatchers(s"Attribute $attributeName Updated $newValue")
          state = updatedState
        case Xor.Left(error) =>
          log.warning(s"Error updating attribute $error")
          sender() ! error
    }
  }
}

