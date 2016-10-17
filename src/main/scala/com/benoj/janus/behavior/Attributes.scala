package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging}
import com.benoj.janus.behavior.Notification.Messages.NotificationMessage

import scala.language.implicitConversions
import scala.collection._

object Attributes {

  sealed abstract class AttributeValue(val value: Any)

  case class StringAttributeValue(override val value: String) extends AttributeValue(value)

  sealed case class AttributeName(name: String)

  object implicits {
    implicit def stringToAttributeName(name: String): AttributeName = AttributeName(name)
    implicit def stringToStringAttributeValue(value: String): StringAttributeValue = StringAttributeValue(value)
    implicit def tupleToStringAttribute(tup: (String, String)): StringAttribute =  StringAttribute(tup._1, tup._2)

  }
  abstract class TypedAttribute(val name: AttributeName, val value: AttributeValue)

  object TypedAttribute {
    def unapply(arg: TypedAttribute): Option[(AttributeName, AttributeValue)] = Some((arg.name, arg.value))
  }

  case class StringAttribute(override val name: AttributeName, override val value: StringAttributeValue) extends TypedAttribute(name, value)


  object Messages {

    case class UpdateAttribute(attributeName: AttributeName, attributeValue: AttributeValue)

    case class UnknownAttribute(attributeName: AttributeName)

  }

}

trait Attributes  extends BehaviorReceive{
  self: Actor with ActorLogging =>

  import com.benoj.janus.behavior.Attributes.Messages._
  import com.benoj.janus.behavior.Attributes._

  def attributes: Seq[TypedAttribute]

  val attributeMap: mutable.Map[AttributeName, AttributeValue] = mutable.Map(attributes.map(attribute => attribute.name -> attribute.value):_*)

  override def behaviorReceive = receiveAttributes orElse super.behaviorReceive

  private def receiveAttributes: Receive = {
    case UpdateAttribute(attributeName, newValue) =>
      attributeMap.get(attributeName) match {
        case None =>
          log.warning(s"Attempted to update attribute $attributeName but not found")
          sender() ! UnknownAttribute(attributeName)
        case Some(currentValue: AttributeValue) => currentValue match {
          case StringAttributeValue(value) => newValue match {
            case value@StringAttributeValue(_) =>
              log.info("Updating string attribute")
              this.self ! NotificationMessage("Updated")
              attributeMap(attributeName) = value
          }
        }
      }
  }
}

