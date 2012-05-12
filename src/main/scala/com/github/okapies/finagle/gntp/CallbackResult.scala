package com.github.okapies.finagle.gntp

import scala.collection._

sealed trait CallbackResult { def name: String }

object CallbackResult {

  case object CLICK extends CallbackResult { val name = "CLICK" }

  case object CLOSE extends CallbackResult { val name = "CLOSE" }

  case object TIMEOUT extends CallbackResult { val name = "TIMEOUT" }

  private[CallbackResult] val names = Map(
    (CLICK.name, CLICK),
    ("CLICKED", CLICK),
    (CLOSE.name, CLOSE),
    ("CLOSED", CLOSE),
    (TIMEOUT.name, TIMEOUT),
    ("TIMEDOUT", TIMEOUT)
  )

  def withName(s: String): CallbackResult = names(s)

}
