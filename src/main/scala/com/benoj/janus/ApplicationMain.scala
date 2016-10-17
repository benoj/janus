package com.benoj.janus

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.pattern.ask
import com.benoj.janus.behavior.Attributes.Messages.UpdateAttribute
import com.benoj.janus.behavior.Watchable.Messages.AddWatchers
import com.benoj.janus.oraganisation.IdActor
import com.benoj.janus.suppliers.Actors.IdSupplier
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
  val system = ActorSystem("Janus")

  implicit val timeout = Timeout(30, TimeUnit.SECONDS)
  implicit val idSupplier: IdSupplier = IdSupplier(system.actorOf(Props(classOf[IdActor], "org"), "id-supplier"))

  val storyActor: ActorRef = system.actorOf(StoryActor.props("", ""), "story")

  val user: ActorRef = system.actorOf(Props[UserActor])

  storyActor ! AddWatchers(Seq(user))

  storyActor ! UpdateAttribute("name", "Get some work done")


  val taskCreated = storyActor ? CreateTask("task", "description")
  storyActor ? CreateTask("task", "description")
  storyActor ? CreateTask("task", "description")
  taskCreated.onSuccess {
    case CreatedTask(task) => storyActor ! ProgressUnit(task)
  }

  system.awaitTermination()
}