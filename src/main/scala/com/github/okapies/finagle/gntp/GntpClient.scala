package com.github.okapies.finagle.gntp

import com.twitter.conversions.time._
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder

import protocol.Gntp

object GntpClient {

  def apply(hostName: String): Service[Request, Response] = ClientBuilder().
    codec(Gntp()).
    hosts(hostName + ":23053").
    hostConnectionLimit(1).
    tcpConnectTimeout(1.second).
    retries(2).
    build()

}
