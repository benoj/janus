package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.benoj.janus.behavior.Watchable.WatcherState


object Watchable {

  object Messages {
    case class AddWatchers(watchers: Set[ActorRef])
    case class RemoveWatchers(watcher: Set[ActorRef])
    case class NotifyWatchers(message: Any)
  }

  case class WatcherState(watchers: Set[ActorRef] = Set.empty) {
    def add(newWatchers: Set[ActorRef]) = copy(watchers ++ newWatchers)
    def remove(watchersToDelete: Set[ActorRef]) = copy(watchers -- watchersToDelete)
  }
}

trait Watchable extends BehaviorReceive {
  self: Actor with ActorLogging =>

  import com.benoj.janus.behavior.Watchable.Messages._

  private var state = WatcherState()

  override def behaviorReceive: Receive = receiveWatchers orElse super.behaviorReceive

  def notifyWatchers(message: Any) = state.watchers.foreach { watcher =>
    log.info(s"Notifying watcher $watcher")
    watcher ! message
  }

  private def receiveWatchers: Receive = {
    case AddWatchers(watchersToAdd) =>
      log.info("Adding Watcher")
      state = state.add(watchersToAdd)
    case RemoveWatchers(watchersToRemove) =>
      log.info("Removing Watcher")
      state = state.remove(watchersToRemove)
    case NotifyWatchers(message) => notifyWatchers(message)
  }

}