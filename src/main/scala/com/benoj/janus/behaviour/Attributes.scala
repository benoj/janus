package com.benoj.janus.behaviour

import akka.actor.{Actor, ActorLogging}

import scala.language.implicitConversions

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

trait Attributes {
  self: Actor with ActorLogging =>

  import com.benoj.janus.behaviour.Attributes.Messages._
  import com.benoj.janus.behaviour.Attributes._

  var attributes: Seq[TypedAttribute] = Seq.empty

  def initAttributes(init: TypedAttribute*) = {
    attributes = init
  }

  def receiveAttributes: Receive = {
    case UpdateAttribute(attributeName, newValue) => {
      attributes.find(_.name == attributeName) match {
        case None =>
          log.warning(s"Attempted to update attribute $attributeName but not found")
          sender() ! UnknownAttribute(attributeName)
        case Some(TypedAttribute(_, currentValue)) => currentValue match {
          case StringAttributeValue(value) => newValue match {
            case s@StringAttributeValue(_) => {
              log.info("Updating string attribute")
              StringAttribute(attributeName, s)
            }
          }
        }
      }
    }
  }
}

