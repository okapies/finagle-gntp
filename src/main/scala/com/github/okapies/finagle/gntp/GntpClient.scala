package com.github.okapies.finagle.gntp

import com.twitter.conversions.time._
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder

import protocol.Gntp

object GntpClient {

  import protocol.GntpConstants._

  def apply(hostName: String, port: Int = DEFAULT_GNTP_PORT): Service[Request, Response] = ClientBuilder().
    codec(Gntp()).
    hosts(hostName + ":" + port).
    hostConnectionLimit(1).
    tcpConnectTimeout(1.second).
    retries(2).
    build()

}
