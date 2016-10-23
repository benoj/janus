package com.benoj.janus

import cats.data.Xor

object Events {

  trait JanusEvent

  case class Failed(msg: Any)

  type ReceiveEvent = PartialFunction[JanusEvent, Failed Xor Unit]

}
