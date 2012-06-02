package com.github.okapies.finagle.gntp.protocol

import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.{ChannelEvent, Channels, ChannelPipelineFactory, MessageEvent}
import org.jboss.netty.handler.logging.LoggingHandler
import org.jboss.netty.logging.InternalLogLevel

import com.twitter.finagle.{Codec, CodecFactory}
import com.twitter.naggati.{Codec => NaggatiCodec}

import com.github.okapies.finagle.gntp.{Request, Response}

class Gntp extends CodecFactory[Request, Response] {

  def client: Client = Function.const {
    new Codec[Request, Response] {
      def pipelineFactory: ChannelPipelineFactory = new ChannelPipelineFactory {
        def getPipeline = {
          val pipeline = Channels.pipeline()
          pipeline.addLast("logging-handler", Gntp.loggingHandler)
          pipeline.addLast("client-codec",
            new NaggatiCodec(GntpResponseCodec.decode, GntpRequestCodec.encode))

          pipeline
        }
      }
    }
  }

  def server: Server =  Function.const {
    new Codec[Request, Response] {
      def pipelineFactory: ChannelPipelineFactory = new ChannelPipelineFactory {
        def getPipeline = {
          val pipeline = Channels.pipeline()
          pipeline.addLast("logging-handler", Gntp.loggingHandler)
          pipeline.addLast("server-codec",
            new NaggatiCodec(GntpRequestCodec.decode, GntpResponseCodec.encode))

          pipeline
        }
      }
    }
  }

}

object Gntp {

  import GntpConstants.MessageFormat._

  private val loggingHandler = new LoggingHandler(InternalLogLevel.DEBUG, false) {
    override def log(e: ChannelEvent) {
      if (getLogger.isEnabled(getLevel)) {
        val buf = e match {
          case me: MessageEvent => me.getMessage match {
            case buf: ChannelBuffer => Some(buf)
            case _ => None
          }
          case _ => None
        }
        buf match {
          case Some(b) =>
            getLogger.log(getLevel, e.toString + " - (STRING:>>>\n" + b.toString(ENCODING) + "<<<")
          case None => super.log(e)
        }
      }
    }
  }

  def apply() = new Gntp

  def get() = apply()

}
