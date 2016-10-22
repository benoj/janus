package com.benoj.janus.behavior

import akka.actor.{Actor, ActorLogging}
import com.benoj.janus.behavior.Created.{Create, NotFound, `Exist?`}

object Created {
  case class Create(id: String)
  case object `Exist?`
  case object NotFound
  case class Created(id: String)
}


trait Created { self: Actor with ActorLogging with BehaviorReceive =>

  def postCreation: Receive = {
    case `Exist?` => Created
  }

  override def receive: Receive = {
    case Create(id) =>
      log.info(s"Creating ${this.getClass.getName}")
      context.become(postCreation orElse behaviorReceive)
      log.info(s"${sender()}")
      sender() ! Created.Created(id)
    case _ =>
      log.info(s"Attempting to send message with non existing actor.")
      sender() ! NotFound
  }

}

