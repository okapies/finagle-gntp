package com.github.okapies.finagle.gntp

import org.jboss.netty.buffer.ChannelBuffer

case class Resource(identifier: String, length: Long, data: ChannelBuffer)
