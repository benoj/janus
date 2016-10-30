package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging, ActorRef}
import cats.data.Xor
import com.benoj.janus.Events.JanusEvent
import com.benoj.janus.PersistentLoggingActor
import com.benoj.janus.behavior.Watchable.Events.{WatchersAdded, WatchersRemoved}
import com.benoj.janus.behavior.Watchable.WatcherState


object Watchable {

  object Commands {

    case class AddWatchers(watchers: Set[ActorRef])

    case class RemoveWatchers(watcher: Set[ActorRef])

    case class NotifyWatchers(message: Any)

  }

  object Events {

    case class WatchersAdded(watchers: Set[ActorRef]) extends JanusEvent

    case class WatchersRemoved(watcher: Set[ActorRef]) extends JanusEvent

  }

  case class WatcherState(watchers: Set[ActorRef] = Set.empty) {
    def add(newWatchers: Set[ActorRef]) = copy(watchers ++ newWatchers)

    def remove(watchersToDelete: Set[ActorRef]) = copy(watchers -- watchersToDelete)
  }

}

trait Watchable extends JanusEventProcessing {
  self: PersistentLoggingActor =>

  import com.benoj.janus.behavior.Watchable.Commands._

  private var state = WatcherState()

  def notifyWatchers(message: Any) = state.watchers.foreach { watcher =>
    log.info(s"Notifying watcher $watcher")
    watcher ! message
  }

  override def processCommand = watchableProcessCommand orElse super.processCommand

  private def watchableProcessCommand: Receive = {
    case AddWatchers(watchersToAdd) =>
      log.info("Adding Watcher")
      processEventAndNotifySender(WatchersAdded(watchersToAdd))
    case RemoveWatchers(watchersToRemove) =>
      log.info("Removing Watcher")
      Xor.Right {
        state = state.remove(watchersToRemove)
      }
    case NotifyWatchers(message) =>
      notifyWatchers(message)

  }

  override def processEvent = {
    case WatchersAdded(watchersToAdd) =>
      Xor.Right {
        state = state.add(watchersToAdd)
      }
    case WatchersRemoved(watchersToRemove) =>
      Xor.Right {
        state = state.remove(watchersToRemove)
      }
  }

}