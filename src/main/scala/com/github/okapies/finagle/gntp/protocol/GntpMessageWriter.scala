package com.github.okapies.finagle.gntp.protocol

import java.io.{IOException, OutputStreamWriter, Writer}
import java.net.URI
import java.util.Date

import scala.collection._

import org.apache.commons.codec.binary.Hex
import org.jboss.netty.buffer.{ChannelBuffer, ChannelBufferOutputStream}

import com.github.okapies.finagle.gntp._

private[protocol] class GntpMessageWriter(buffer: ChannelBuffer) {

  import GntpMessageWriter._

  import GntpConstants.MessageFormat._
  import util.GntpDateFormat

  private val writer: Writer =
    new OutputStreamWriter(new ChannelBufferOutputStream(buffer), ENCODING)

  def toChannelBuffer = buffer

  @throws(classOf[IOException])
  def close() { writer.close() }

  def emptyLine() {
    writer.write(LINE_SEPARATOR)
  }

  @throws(classOf[IOException])
  def informationLine(
      messageType: String,
      encryption: Option[Encryption],
      authorization: Option[Authorization]) {
    val infoLine = new StringBuilder

    infoLine.append("GNTP/1.0 " + messageType + " ")
    infoLine.append(encryption match {
      case Some(enc) => enc.algorithm.toString +
        enc.iv.map(iv => ":" + Hex.encodeHex(iv, false))
      case None => "NONE"
    })
    infoLine.append(authorization match {
      case Some(auth) => " " + auth.algorithm.toString + ":" +
        Hex.encodeHex(auth.keyHash, false) + "." +
        Hex.encodeHex(auth.salt, false)
      case None => ""
    })

    writer.write(infoLine.toString + LINE_SEPARATOR)
  }

  @throws(classOf[IOException])
  def header(name: String, value: String) {
    if (value != null) {
      val line = separatorMatcher.replaceAllIn(value, "\n")
      writer.write(name + ": " + line + LINE_SEPARATOR)
    }
  }

  @throws(classOf[IOException])
  def header(name: String, value: Option[String]) {
    value.foreach { _ => header(name, value.get) }
  }

  @throws(classOf[IOException])
  def header(name: String, value: Int) {
    writer.write(name + ": " + value + LINE_SEPARATOR)
  }

  @throws(classOf[IOException])
  def header(name: String, value: Boolean) {
    writer.write(name + ": " + value + LINE_SEPARATOR)
  }

  @throws(classOf[IOException])
  def header(name: String, value: Date) {
    if (value != null) {
      writer.write(name + ": " + GntpDateFormat.toString(value) + LINE_SEPARATOR)
    }
  }

  @throws(classOf[IOException])
  def header(name: String, value: URI) {
    if (value != null) {
      writer.write(name + ": " + value.toString + LINE_SEPARATOR)
    }
  }

  @throws(classOf[IOException])
  def iconHeader(name: String, icon: Option[Icon]) {
    icon match {
      case Some(IconUri(uri)) => header(name, uri.toString)
      case Some(IconImage(image)) => // TODO:
      case None =>
    }
  }

  @throws(classOf[IOException])
  def headers(headers: Map[String, String]) {
    headers.foreach { case (name, value) => header(name, value) }
  }

}

private object GntpMessageWriter {

  import GntpConstants.MessageFormat._

  private[protocol] val separatorMatcher = LINE_SEPARATOR.r

}
