package com.benoj.janus

import akka.actor._
import akka.pattern.ask
import com.benoj.janus.behavior.Attributes.Messages.UpdateAttribute
import com.benoj.janus.behavior.Watchable.Messages.AddWatchers
import com.benoj.janus.workunits.StoryActor
import com.benoj.janus.workunits.StoryActor.Messages.CreateTask

import scala.concurrent.ExecutionContext.Implicits.global

class UserActor extends Actor with ActorLogging {

  log.info("User actor created")

  override def receive: Receive = {
    case msg@_ => log.info(s"$msg")
  }

}

object ApplicationMain extends App {
  import com.benoj.janus.behavior.Attributes.implicits._

  import akka.util.Timeout
  import scala.concurrent.duration._
  implicit val timeout = Timeout(100.days)

  val system = ActorSystem("Janus")
  val storyActor: ActorRef = system.actorOf(Props(classOf[StoryActor],"",""),"story")

  val user: ActorRef = system.actorOf(Props[UserActor])

  storyActor ! AddWatchers(Seq(user))

  storyActor ! UpdateAttribute("name", "Get some work done")

  ask(storyActor,CreateTask("task", "description")) onSuccess {
    case s@_ => println(s"Task Created $s")
  }

  system.awaitTermination()
}