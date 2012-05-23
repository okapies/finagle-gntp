package com.github.okapies.finagle.gntp.util

import org.specs2.mutable._

import java.nio.charset.Charset

import org.jboss.netty.buffer.{ChannelBuffer, ChannelBuffers}

import com.twitter.naggati._

class RichStagesSpec extends Specification {

  import com.twitter.naggati.Stages._
  import com.github.okapies.finagle.gntp.util.RichStages._

  "indexOf()" should {

    "return index of first occurrence of the specified character." in {
      val buf = ChannelBuffers.wrappedBuffer("Hello, world!".getBytes)

      val pat1 = "H".getBytes
      indexOf(buf, pat1) must_== (0)

      val pat2 = "!".getBytes
      indexOf(buf, pat2) must_== (12)

      buf.readBytes(1) // "ello, world!"
      val pat3 = "e".getBytes
      indexOf(buf, pat3) must_== (0)

      buf.readBytes(11) // "!"
      val pat4 = "!".getBytes
      indexOf(buf, pat4) must_== (0)
    }

    "return index of first occurrence of the specified pattern." in {
      val buf = ChannelBuffers.wrappedBuffer("Hello, world!".getBytes)

      val pat1 = "Hello".getBytes
      indexOf(buf, pat1) must_== (0)

      val pat2 = "world!".getBytes
      indexOf(buf, pat2) must_== (7)

      buf.readBytes(1) // "ello, world!"
      val pat3 = "ello".getBytes
      indexOf(buf, pat3) must_== (0)

      buf.readBytes(6) // "world!"
      val pat4 = "world!".getBytes
      indexOf(buf, pat4) must_== (0)
    }

    "return negative value if the specified pattern never occurs." in {
      val buf = ChannelBuffers.wrappedBuffer("Hello, world!".getBytes)

      val pat1 = "_".getBytes
      indexOf(buf, pat1) must be lessThan(0)

      val pat2 = "__".getBytes
      indexOf(buf, pat2) must be lessThan(0)

      val pat3 = "__Hello, world!__".getBytes
      indexOf(buf, pat3) must be lessThan(0)

      buf.readBytes(1) // "ello, world!"
      val pat4 = "H".getBytes
      indexOf(buf, pat4) must be lessThan(0)

      val pat5 = "Hello".getBytes
      indexOf(buf, pat5) must be lessThan(0)

      buf.readBytes(11) // ""
      val pat6 = "world!".getBytes
      indexOf(buf, pat6) must be lessThan(0)
    }

  }

  "ensureMultiByteDelimiter()" should {

    "pass the number of bytes to a next processing step." in {
      val buf1 = ChannelBuffers.wrappedBuffer("Hello,\r\nworld!".getBytes)

      val delimiter = "\r\n".getBytes
      def stage1 = ensureMultiByteDelimiter(delimiter) { (n, buffer) =>
        emit(Int.box(n))
      }

      val Emit(n1) = stage1(buf1)
      n1 must_== (8)
    }

    "return Imcomplete stage if the delimiter is not present." in {
      val buf1 = ChannelBuffers.wrappedBuffer("Hello, world!".getBytes)

      val delimiter = "\r\n".getBytes
      def stage1 = ensureMultiByteDelimiter(delimiter) { (n, buffer) =>
        emit(Int.box(n))
      }

      stage1(buf1) must_== (Incomplete)
    }

  }

  "ensureMultiByteDelimiterDynamic()" should {

    "pass the number of bytes to a next processing step." in {
      val buf1 = ChannelBuffers.wrappedBuffer("Hello, world!".getBytes)

      var i = 0
      val delimiters = Array("ll".getBytes, "rl".getBytes)
      def stage1 = ensureMultiByteDelimiterDynamic(delimiters(i)) { (n, buffer) =>
        emit(Int.box(n))
      }

      val Emit(n1) = stage1(buf1)
      n1 must_== (4)
      i = 1
      val Emit(n2) = stage1(buf1)
      n2 must_== (11)
    }

  }

  "readToMultiByteDelimiter()" should {

    "pass a buffer to a next processing step." in {
      val buf1 = ChannelBuffers.wrappedBuffer("Hello,\r\nworld!".getBytes)

      val delimiter = "\r\n".getBytes
      def stage1 = readToMultiByteDelimiter(delimiter) { bytes => emit(bytes) }

      val GoToStage(stage11) = stage1(buf1)
      val Emit(bytes1) = stage11(buf1)
      bytes1 must_== ("Hello,\r\n".getBytes)
    }

    "return Imcomplete if the delimiter is not present." in {
      val buf1 = ChannelBuffers.wrappedBuffer("Hello, world!".getBytes)

      val delimiter = "\r\n".getBytes
      def stage1 = readToMultiByteDelimiter(delimiter) { bytes => emit(bytes) }

      val GoToStage(stage11) = stage1(buf1)
      stage11(buf1) must_== (Incomplete)
    }

  }

  "readToMultiByteDelimiterDynamic()" should {

    "pass a buffer to a next processing step." in {
      val buf1 = ChannelBuffers.wrappedBuffer("Hello, world!".getBytes)

      var i = 0
      val delimiters = Array("ll".getBytes, "rl".getBytes)
      def stage1 = readToMultiByteDelimiterDynamic(delimiters(i)) { bytes => emit(bytes) }

      val GoToStage(stage11) = stage1(buf1)
      val Emit(bytes1) = stage11(buf1)
      bytes1 must_== ("Hell".getBytes)
      i = 1
      val GoToStage(stage12) = stage1(buf1)
      val Emit(bytes2) = stage12(buf1)
      bytes2 must_== ("o, worl".getBytes)
    }

  }

}
