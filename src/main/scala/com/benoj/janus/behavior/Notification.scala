package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging}
import com.benoj.janus.behavior.Notification.Messages.NotificationMessage
object Notification {
  object Messages {

    case class NotificationMessage(message: Any)

  }
}