package com.github.okapies.finagle.gntp.protocol

import java.io.{OutputStreamWriter, Writer}

import org.jboss.netty.buffer.{ChannelBufferOutputStream, ChannelBuffers}
import org.jboss.netty.channel.{Channel, ChannelHandlerContext}
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder

import com.github.okapies.finagle.gntp._
import com.github.okapies.finagle.gntp.protocol.GntpConstants._

abstract class GntpMessageEncoder extends OneToOneEncoder {

  import com.github.okapies.finagle.gntp.protocol.GntpConstants.MessageFormat._

  protected var writer: Writer = null

  override def encode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef): AnyRef = {
    val buf = ChannelBuffers.dynamicBuffer
    val out = new ChannelBufferOutputStream(buf)
    this.writer = new OutputStreamWriter(out, MessageFormat.ENCODING)

    try {
      // write an encoded message.
      encodeMessage(msg.asInstanceOf[Request], Encryption(EncryptionAlgorithm.NONE), None)
    } finally {
      writer.close()
      writer = null
    }

    buf
  }

  def encodeMessage(
    request: Request,
    encryption: Encryption,
    authorization: Option[Authorization])

  protected def emptyLine() {
    writer.write(LINE_SEPARATOR);
  }

  protected def informationLine(
      messageType: String,
      encryption: Encryption,
      authorization: Option[Authorization]) {
    val infoLine = Some("GNTP/1.0 " + messageType + " " + encryption.algorithm.toString) ::
      encryption.iv.map(":" + _) ::
      authorization.map(
        auth => " " + auth.algorithm + ":" + auth.keyHash + "." + auth.salt) :: Nil

    writer.write(infoLine.flatten.mkString + LINE_SEPARATOR)
  }

  protected def header(name: String, value: String) {
    if (value != null) {
      writer.write(name + ": " + value + LINE_SEPARATOR)
    }
  }

  protected def header(name: String, value: Int) {
    writer.write(name + ": " + value + LINE_SEPARATOR)
  }

  protected def header(name: String, value: Boolean) {
    writer.write(name + ": " + value + LINE_SEPARATOR)
  }

  protected def iconHeader(name: String, icon: Icon) {
    icon match {
      case IconUri(uri) => header(name, uri.toString)
      case IconImage(image) => // TODO:
      case NoIcon =>
    }
  }

}
