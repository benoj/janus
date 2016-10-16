package com.benoj.janus.behaviour

import akka.actor.{Actor, ActorLogging, ActorRef}

object Watchable {

  object Messages {

    case class AddWatchers(watchers: Seq[ActorRef])

    case class RemoveWatchers(watcher: Seq[ActorRef])

  }

}

trait Watchable {
  self: Actor with ActorLogging =>
  import com.benoj.janus.behaviour.Watchable.Messages._

  private[this] var watchers: Seq[ActorRef] = Seq()

  def addWatcher(watchersToAdd: Seq[ActorRef]): Unit = {
    log.info("Adding Watcher")
    watchers = watchers ++ watchersToAdd
  }

  def removeWatchers(watchersToRemove: Seq[ActorRef]): Unit = {
    log.info("Removing Watcher")
    watchers = watchers.filter(watchersToRemove.contains(_))
  }

  def receiveWatchers: Receive = {
    case AddWatchers(watchersToAdd) => addWatcher(watchersToAdd)
    case RemoveWatchers(watchersToRemove) => removeWatchers(watchersToRemove)
  }

}