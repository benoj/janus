package com.benoj.janus

import akka.actor._
import akka.pattern.ask
import com.benoj.janus.behavior.Attributes.Messages.UpdateAttribute
import com.benoj.janus.behavior.Watchable.Messages.AddWatchers
import com.benoj.janus.workflow.WorkflowActor.Messages.ProgressUnit
import com.benoj.janus.workunits.StoryActor
import com.benoj.janus.workunits.StoryActor.Messages.{CreateTask, CreatedTask}

import scala.concurrent.ExecutionContext.Implicits.global

class UserActor extends Actor with ActorLogging {

  log.info("User actor created")

  override def receive: Receive = {
    case msg@_ => log.info(s"$msg")
  }

}

object ApplicationMain extends App {

  import akka.util.Timeout
  import com.benoj.janus.behavior.Attributes.implicits._

  import scala.concurrent.duration._


  val system = ActorSystem("Janus")
  val storyActor: ActorRef = system.actorOf(Props(classOf[StoryActor], "", ""), "story")

  val user: ActorRef = system.actorOf(Props[UserActor])

  storyActor ! AddWatchers(Seq(user))

  storyActor ! UpdateAttribute("name", "Get some work done")

  implicit val timeout = Timeout(100.days)

  val taskCreated = storyActor ? CreateTask("task", "description")
  taskCreated.onSuccess {
    case CreatedTask(task) => storyActor ! ProgressUnit(task)
  }

  system.awaitTermination()
}