package com.benoj.janus

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.benoj.janus.behaviour.Attributes.Messages.UpdateAttribute
import com.benoj.janus.behaviour.Watchable.Messages.AddWatchers
import com.benoj.janus.workunits.TaskActor


class UserActor extends Actor with ActorLogging {
  override def receive: Receive = { case msg: Any => log.info(s"received $msg") }
}
object ApplicationMain extends App {
  import com.benoj.janus.behaviour.Attributes.implicits._

  val system = ActorSystem("Janus")
  val taskActor: ActorRef = system.actorOf(Props[TaskActor])

  taskActor ! AddWatchers(Seq(system.actorOf(Props[UserActor])))

  taskActor ! UpdateAttribute("name", "Get some work done")

  system.awaitTermination()
}