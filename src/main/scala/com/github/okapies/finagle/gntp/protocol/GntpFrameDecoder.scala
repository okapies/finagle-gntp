package com.github.okapies.finagle.gntp.protocol

import org.jboss.netty.buffer.{ChannelBuffer, ChannelBuffers}
import org.jboss.netty.channel.{Channel, ChannelHandlerContext}
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder

import com.github.okapies.finagle.gntp.protocol.GntpConstants._

class GntpFrameDecoder extends DelimiterBasedFrameDecoder(
    Int.MaxValue,
    ChannelBuffers.wrappedBuffer(MessageFormat.TERMINATOR)) {

  override def decode(
      ctx: ChannelHandlerContext,
      channel: Channel,
      buffer: ChannelBuffer): AnyRef =
    ctx.getAttachment match {
      case null => {
        // delegate to delimiter based decoder.
        super.decode(ctx, channel, buffer)
      }
      case (_, frameLength: AnyRef) => Int.unbox(frameLength) match {
        case len: Int if buffer.readableBytes < (len + MessageFormat.TERMINATOR.length) => {
          // there's not enough data.
          null
        }
        case len: Int => {
          // read data and skip the message terminator.
          val frame = buffer.readBytes(len)
          buffer.skipBytes(MessageFormat.TERMINATOR.length)

          frame
        }
      }
    }

  override def newCumulationBuffer(
      ctx: ChannelHandlerContext,
      minimumCapacity: Int): ChannelBuffer =
    super.newCumulationBuffer(ctx, minimumCapacity)

}
