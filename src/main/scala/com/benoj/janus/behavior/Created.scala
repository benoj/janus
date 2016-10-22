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


  private def exists: Receive = {
    case `Exist?` => Created
  }

  def postCreation: Receive

  override def receive: Receive = {
    case Create(id) =>
      log.info(s"Creating ${this.getClass.getName}")
      context.become(postCreation orElse exists orElse behaviorReceive)
      log.info(s"${sender()}")
      sender() ! Created.Created(id)
    case msg@_ =>
      log.info(s"Attempting to send message $msg with non existing actor ${this.self.path.name}")
      sender() ! NotFound
  }

}

