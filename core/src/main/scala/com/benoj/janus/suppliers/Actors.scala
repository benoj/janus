package com.benoj.janus.suppliers

import akka.actor.ActorRef

object Actors {
  case class IdSupplier(actor: ActorRef)
}
