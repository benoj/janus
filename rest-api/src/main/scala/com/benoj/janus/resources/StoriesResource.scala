package com.benoj.janus.resources

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

object StoriesResource extends Directives {
  def route(projectId: String)(implicit mat: Materializer, ec: ExecutionContext): Route = pathPrefix("stories") {
    pathEnd {
      complete(StatusCodes.NotImplemented)
    } ~
      pathPrefix(Segment) { storyId =>
        pathEnd {
          complete(StatusCodes.NotImplemented)
        } ~ TaskResource.route(projectId, storyId)
      }
  }
}
