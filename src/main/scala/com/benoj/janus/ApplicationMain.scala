package com.benoj.janus

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import com.benoj.janus.behavior.Attributes.Messages.UpdateAttribute
import com.benoj.janus.behavior.Created.{Create, Created}
import com.benoj.janus.behavior.Watchable.Messages.AddWatchers
import com.benoj.janus.workflow.WorkflowActor.Messages.ProgressUnit
import com.benoj.janus.workunits.ProjectActor
import com.benoj.janus.workunits.ProjectActor.Messages.{CreateNewStoryInBacklog, UpdateStory}
import com.benoj.janus.workunits.StoryActor.Messages.CreateTask
import com.benoj.janus.behavior.Attributes.implicits._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class UserActor extends Actor with ActorLogging {

  log.info("User actor created")

  override def receive: Receive = {
    case msg@_ => log.info(s"User recieved notification $msg")
  }

}

object ApplicationMain extends App {

  import akka.util.Timeout
  

  val system = ActorSystem("Janus")

  val log = Logging.getLogger(system, this)

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  val projectActor: ActorRef = system.actorOf(ProjectActor.props("Open Source Task Manager"), "janus")
  projectActor ? Create("janus") onSuccess {
    case _ => projectActor ? CreateNewStoryInBacklog("Start Stuff", "get stuff started") onSuccess {
      case Created(id) =>
              val user: ActorRef = system.actorOf(Props[UserActor])

              projectActor ? UpdateStory(id, AddWatchers(Set(user)))
              projectActor ? UpdateStory(id, UpdateAttribute("name", "Get some work done"))

        projectActor ? UpdateStory(id, CreateTask("task1", "description")) onComplete  {
          case Success(Created(taskId)) =>
            projectActor ? UpdateStory(id, ProgressUnit(taskId)) onComplete {
              case Success(_) => log.info("Progress")
              case Failure(e) => log.error(e, s"Nooop")
            }
          case e@_ => log.error(s"Failed to update Story $e")
        }
    }
  }



  Await.ready(system.whenTerminated, Duration.Inf)
}