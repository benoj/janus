package com.benoj.janus

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.benoj.janus.resources.ProjectResource

object Bootstrap extends App  {

  implicit val system = ActorSystem("akka-rest-api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val log = Logging.getLogger(system, this)

  val binding = Http().bindAndHandle(handler = ProjectResource.route, interface = "0.0.0.0", port = 8080)


  log.info(s"Bound to port 8080")
  binding onFailure {
    case ex: Exception ⇒
      log.error(s"Failed to bind!", ex)
  }
  sys.addShutdownHook(system.terminate())
}
