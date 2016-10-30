package com.benoj.janus.resources

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server._
import akka.stream.Materializer
import akka.util.Timeout

import scala.concurrent.ExecutionContext





object ProjectResource extends Directives {

  import scala.concurrent.duration._

  implicit private val timeout = Timeout(30.seconds)

  def route(implicit mat: Materializer, ec: ExecutionContext): Route = {
    pathPrefix("projects") {
      pathEndOrSingleSlash {
        get {
          complete(StatusCodes.NotImplemented)
        }
      } ~ pathPrefix(Segment) { projectid =>
        pathEnd {
          complete(StatusCodes.NotImplemented)
        } ~
          StoriesResource.route(projectid)
      }
    }
  }
}
