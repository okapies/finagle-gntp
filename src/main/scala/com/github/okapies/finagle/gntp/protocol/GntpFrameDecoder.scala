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
          // read data and the message terminator.
          buffer.readBytes(len + MessageFormat.TERMINATOR.length).slice(0, len)
        }
      }
    }

  override def newCumulationBuffer(
      ctx: ChannelHandlerContext,
      minimumCapacity: Int): ChannelBuffer =
    ctx.getAttachment match {
      case null =>
        super.newCumulationBuffer(ctx, minimumCapacity)
      case (_, frameLength: AnyRef) => Int.unbox(frameLength) match {
        case len: Int => {
          val factory = ctx.getChannel.getConfig.getBufferFactory
          ChannelBuffers.dynamicBuffer(factory.getDefaultOrder, len, factory)
        }
      }
    }

}
