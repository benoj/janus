package com.benoj.janus.resources

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

object TaskResource extends Directives {
  def route(projectId: String, storyId: String)(implicit mat: Materializer, ec: ExecutionContext): Route = pathPrefix("tasks") {
    pathEnd {
      complete(StatusCodes.NotImplemented)
    } ~
      pathPrefix(Segment) { taskId =>
        pathEnd {
          complete(StatusCodes.NotImplemented)
        }
      }
  }
}
