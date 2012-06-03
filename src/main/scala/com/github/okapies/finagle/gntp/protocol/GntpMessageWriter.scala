package com.github.okapies.finagle.gntp.protocol

import java.net.URI
import java.util.Date

import scala.collection._

import org.apache.commons.codec.binary.Hex
import org.jboss.netty.buffer.{ChannelBuffer, ChannelBufferOutputStream}

import com.github.okapies.finagle.gntp._

private[protocol] class GntpMessageWriter(buffer: ChannelBuffer) {

  import GntpMessageWriter._

  import GntpConstants.MessageFormat._
  import Header._
  import util.GntpDateFormat

  def toChannelBuffer = buffer

  def emptyLine() {
    buffer.writeBytes(LINE_SEPARATOR.getBytes(ENCODING))
  }

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

    buffer.writeBytes((infoLine.toString + LINE_SEPARATOR).getBytes(ENCODING))
  }

  def header(name: String, value: String) {
    if (value != null) {
      val line =
        name + ": " + separatorMatcher.replaceAllIn(value, "\n") + LINE_SEPARATOR
      buffer.writeBytes(line.getBytes(ENCODING))
    }
  }

  def header(name: String, value: Option[String]) {
    value.foreach { _ => header(name, value.get) }
  }

  def header(name: String, value: Int) {
    val line = name + ": " + value + LINE_SEPARATOR
    buffer.writeBytes(line.getBytes(ENCODING))
  }

  def header(name: String, value: Long) {
    val line = name + ": " + value + LINE_SEPARATOR
    buffer.writeBytes(line.getBytes(ENCODING))
  }

  def header(name: String, value: Boolean) {
    val line = name + ": " + value.toString.capitalize + LINE_SEPARATOR
    buffer.writeBytes(line.getBytes(ENCODING))
  }

  def header(name: String, value: Date) {
    if (value != null) {
      val line = name + ": " + GntpDateFormat.toString(value) + LINE_SEPARATOR
      buffer.writeBytes(line.getBytes(ENCODING))
    }
  }

  def header(name: String, value: URI) {
    if (value != null) {
      val line = name + ": " + value.toString + LINE_SEPARATOR
      buffer.writeBytes(line.getBytes(ENCODING))
    }
  }

  def iconHeader(name: String, icon: Option[Icon]) {
    icon match {
      case Some(IconUri(uri)) => header(name, uri.toString)
      case Some(IconImage(image)) => // TODO:
      case None =>
    }
  }

  def headers(headers: Map[String, String]) {
    headers.foreach { case (name, value) => header(name, value) }
  }

  def resources(resources: Map[ResourceId, Resource]) {
    resources.foreach { case (_, res) =>
      header(RESOURCE_IDENTIFIER, res.id.toUniqueValue)
      header(RESOURCE_LENGTH, res.length)
      emptyLine()

      buffer.writeBytes(res.data)
      emptyLine()
      emptyLine()
    }
  }

}

private object GntpMessageWriter {

  import GntpConstants.MessageFormat._

  private[protocol] val separatorMatcher = LINE_SEPARATOR.r

}
