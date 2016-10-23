package com.benoj.janus

import akka.actor.ActorLogging
import akka.persistence.PersistentActor

trait PersistentLoggingActor extends PersistentActor with ActorLogging
