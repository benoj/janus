package com.benoj.janus.oraganisation

import akka.actor.Actor


class IdActor(organisationPrefix: String) extends Actor {
  import com.benoj.janus.oraganisation.IdActor.Messages.{GetNextId, Id}

  private var currentCount: Long = 0

  override def receive: Receive = {
    case GetNextId =>
      sender() ! Id(s"$organisationPrefix-$currentCount")
      currentCount += 1
  }
}

object IdActor {
  object Messages {
    case object GetNextId
    case class Id(id: String)
  }
}