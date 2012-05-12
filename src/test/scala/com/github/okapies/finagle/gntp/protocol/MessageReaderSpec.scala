package com.github.okapies.finagle.gntp.protocol

import org.specs2.mutable._

import java.io.{ByteArrayInputStream, InputStreamReader}

class MessageReaderSpec extends Specification {

  import GntpConstants._

  "MessageReader.readLine()" can {

    "return single line." in {
      val content = "Hello, world!"
      val in = new ByteArrayInputStream(content.getBytes(MessageFormat.ENCODING))
      val reader = new MessageReader(new InputStreamReader(in, MessageFormat.ENCODING))

      reader.readLine() must_== ("Hello, world!")
      reader.readLine() must beNull
    }

    "return multiple lines." in {
      val content = "Hello, world!\r\nHello, Scala!\r\nHello, GNTP!"
      val in = new ByteArrayInputStream(content.getBytes(MessageFormat.ENCODING))
      val reader = new MessageReader(new InputStreamReader(in, MessageFormat.ENCODING))

      reader.readLine() must_== ("Hello, world!")
      reader.readLine() must_== ("Hello, Scala!")
      reader.readLine() must_== ("Hello, GNTP!")
      reader.readLine() must beNull
    }

  }

  "MessageReader.readLine()" should {

    "return null if the end of the stream has been reached." in {
      val content = ""
      val in = new ByteArrayInputStream(content.getBytes(MessageFormat.ENCODING))
      val reader = new MessageReader(new InputStreamReader(in, MessageFormat.ENCODING))

      reader.readLine() must beNull
    }

    "not consider a line to be terminated by a '\\n'." in {
      val content1 = "Hello, world!\n"
      val in1 = new ByteArrayInputStream(content1.getBytes(MessageFormat.ENCODING))
      val reader1 = new MessageReader(new InputStreamReader(in1, MessageFormat.ENCODING))

      reader1.readLine() must_== ("Hello, world!\n")
      reader1.readLine() must beNull

      val content2 = "Hello, world!\nHello, Scala!"
      val in2 = new ByteArrayInputStream(content2.getBytes(MessageFormat.ENCODING))
      val reader2 = new MessageReader(new InputStreamReader(in2, MessageFormat.ENCODING))

      reader2.readLine() must_== ("Hello, world!\nHello, Scala!")
      reader2.readLine() must beNull
    }

    "not consider a line to be terminated by a '\\r' which isn't followed immediately by a '\\n'." in {
      val content1 = "Hello, world!\r"
      val in1 = new ByteArrayInputStream(content1.getBytes(MessageFormat.ENCODING))
      val reader1 = new MessageReader(new InputStreamReader(in1, MessageFormat.ENCODING))

      reader1.readLine() must_== ("Hello, world!\r")
      reader1.readLine() must beNull

      val content2 = "Hello, world!\rHello, Scala!\r\r\nHello, GNTP!"
      val in2 = new ByteArrayInputStream(content2.getBytes(MessageFormat.ENCODING))
      val reader2 = new MessageReader(new InputStreamReader(in2, MessageFormat.ENCODING))

      reader2.readLine() must_== ("Hello, world!\rHello, Scala!\r")
      reader2.readLine() must_== ("Hello, GNTP!")
      reader2.readLine() must beNull
    }

  }

}
