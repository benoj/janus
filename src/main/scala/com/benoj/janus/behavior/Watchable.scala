package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef}
import scala.collection.mutable

object Watchable {

  object Messages {

    case class AddWatchers(watchers: Seq[ActorRef])

    case class RemoveWatchers(watcher: Seq[ActorRef])

    case class NotifyWatchers(message: Any)

  }

}

trait Watchable extends BehaviorReceive {
  self: Actor with ActorLogging =>

  import com.benoj.janus.behavior.Watchable.Messages._

  private val watchers: mutable.Buffer[ActorRef] = mutable.Buffer.empty

  override def behaviorReceive: Receive = receiveWatchers orElse super.behaviorReceive

  def notifyWatchers(message: Any) = watchers.foreach { watcher =>
    log.info(s"Notifying watcher $watcher")
    watcher ! message
  }

  private def receiveWatchers: Receive = {
    case AddWatchers(watchersToAdd) =>
      log.info("Adding Watcher")
      watchers ++= watchersToAdd
    case RemoveWatchers(watchersToRemove) =>
      log.info("Removing Watcher")
      watchers --= watchers.filter(watchersToRemove.contains(_))
    case NotifyWatchers(message) => notifyWatchers(message)
  }

}