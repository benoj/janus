package com.benoj.janus.behavior

import com.benoj.janus.PersistentLoggingActor
import com.benoj.janus.behavior.Created.{Create, NotFound, `Exist?`}

object Created {

  case class Create(id: String)

  case object `Exist?`

  case object NotFound

  case class Created(id: String)

}


trait Created extends CommandProcess {
  self: PersistentLoggingActor with JanusEventProcessing =>

  private def exists: Receive = {
    case `Exist?` => Created
  }

  def postCreation: Receive

  override def processCommand: Receive = createdProcessCommand orElse super.processCommand

  def createdProcessCommand: Receive = {
    case Create(id) =>
      log.info(s"Creating ${this.getClass.getName}")
      context.become(postCreation orElse exists orElse processCommand)
      log.info(s"Sender: ${sender()}")
      sender() ! Created.Created(id)
    case msg@_ =>
      log.info(s"Attempting to send message $msg with non existing actor ${this.self.path.name}")
      sender() ! NotFound
  }
}

