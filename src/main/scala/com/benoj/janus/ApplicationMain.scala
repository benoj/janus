package com.benoj.janus

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.pattern.ask
import com.benoj.janus.behavior.Attributes.Messages.UpdateAttribute
import com.benoj.janus.behavior.Watchable.Messages.AddWatchers
import com.benoj.janus.workflow.WorkflowActor.Messages.ProgressUnit
import com.benoj.janus.workunits.ProjectActor
import com.benoj.janus.workunits.ProjectActor.Messages.{CreateNewStoryInBacklog, CreatedStory, UpdateStory}
import com.benoj.janus.workunits.StoryActor.Messages.{CreateTask, CreatedTask}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import com.benoj.janus.behavior.Attributes.implicits._

class UserActor extends Actor with ActorLogging {

  log.info("User actor created")

  override def receive: Receive = {
    case msg@_ => log.info(s"User recieved notification $msg")
  }

}

object ApplicationMain extends App {

  import akka.util.Timeout
  val system = ActorSystem("Janus")

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  val projectActor: ActorRef = system.actorOf(ProjectActor.props("Janus", "Open Source Task Manager"), "project")



  projectActor ? CreateNewStoryInBacklog("Start Stuff","get stuff started") onSuccess {
    case CreatedStory(id) =>
      val user: ActorRef = system.actorOf(Props[UserActor])

      projectActor ? UpdateStory(id, AddWatchers(Seq(user)))
      projectActor ? UpdateStory(id, UpdateAttribute("name", "Get some work done"))

      projectActor ? UpdateStory(id,CreateTask("task1", "description")) onSuccess {
       case CreatedTask(taskId) =>
         projectActor ? UpdateStory(id,ProgressUnit(taskId)) onComplete {
           case Success(_) => println("Progress")
           case Failure(e) => println(e,"Progress")
         }
     }
  }







  system.awaitTermination()
}