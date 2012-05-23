package com.github.okapies.finagle.gntp.util

import scala.annotation.tailrec

import org.jboss.netty.buffer.ChannelBuffer

import com.twitter.naggati.{Incomplete, NextStep}
import com.twitter.naggati.Stages._

object RichStages {

  /**
   * Read bytes until a delimiter is present. The number of bytes up to and including the delimiter
   * is passed to the next processing step. `getDelimiter` is called each time new data arrives.
   */
  def ensureMultiByteDelimiterDynamic(getDelimiter: => Array[Byte])(
      process: (Int, ChannelBuffer) => NextStep) = proxy {
    ensureMultiByteDelimiter(getDelimiter)(process)
  }

  /**
   * Read bytes until a delimiter is present. The number of bytes up to and including the delimiter
   * is passed to the next processing step.
   */
  def ensureMultiByteDelimiter(delimiter: Array[Byte])(
      process: (Int, ChannelBuffer) => NextStep) = stage { buffer =>
    val n = indexOf(buffer, delimiter)
    if (n < 0) {
      Incomplete
    } else {
      process(n + delimiter.length, buffer)
    }
  }

  /**
   * Read bytes until a delimiter is present, and pass a buffer containing the bytes up to and
   * including the delimiter to the next processing step. `getDelimiter` is called each time new
   * data arrives.
   */
  def readToMultiByteDelimiterDynamic(getDelimiter: => Array[Byte])(
      process: Array[Byte] => NextStep) = proxy {
    readToMultiByteDelimiter(getDelimiter)(process)
  }

  /**
   * Read bytes until a delimiter is present, and pass a buffer containing the bytes up to and
   * including the delimiter to the next processing step.
   */
  def readToMultiByteDelimiter(delimiter: Array[Byte])(
      process: (Array[Byte]) => NextStep) = stage { buffer =>
    ensureMultiByteDelimiter(delimiter) { (n, buffer) =>
      val byteBuffer = new Array[Byte](n)
      buffer.readBytes(byteBuffer)
      process(byteBuffer)
    }
  }

  /**
   * brute-force string matching.
   */
  private[util] def indexOf(buffer: ChannelBuffer, pattern: Array[Byte]): Int =
    indexOfImpl(buffer, pattern, buffer.readerIndex, buffer.writerIndex - pattern.length)

  @tailrec
  private[this] def indexOfImpl(buf: ChannelBuffer, pat: Array[Byte], from: Int, to: Int): Int =
    if (from <= to) {
      if (doMatch(buf, pat, from, 0) != pat.length) {
        indexOfImpl(buf, pat, from + 1, to)
      } else {
        from - buf.readerIndex
      }
    } else {
      -1
    }

  @tailrec
  private[this] def doMatch(buf: ChannelBuffer, pat: Array[Byte], i: Int, j: Int): Int =
    if (j < pat.length && buf.getByte(i) == pat(j)) {
      doMatch(buf, pat, i + 1, j + 1)
    } else {
      j
    }

}
