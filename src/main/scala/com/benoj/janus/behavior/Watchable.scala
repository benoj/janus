package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef}

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

  private[this] var watchers: Seq[ActorRef] = Seq()

  override def behaviorReceive: Receive = receiveWatchers orElse super.behaviorReceive

  private def receiveWatchers: Receive = {
    case AddWatchers(watchersToAdd) =>
      log.info("Adding Watcher")
      watchers = watchers ++ watchersToAdd
    case RemoveWatchers(watchersToRemove) =>
      log.info("Removing Watcher")
      watchers = watchers.filter(watchersToRemove.contains(_))
    case NotifyWatchers(message) =>
      watchers.foreach((watcher) => {
        log.info(s"Notifying watcher $watcher")
        watcher ! message
      })
  }

}