package com.github.okapies.finagle.gntp.protocol

import java.nio.charset.Charset

object GntpConstants {

  val DEFAULT_GNTP_PORT = 23053

  object MessageFormat {

    val SUPPORTED_VERSION = "1.0"

    val ENCODING = Charset.forName("UTF-8")

    val GROWL_RESOURCE_PREFIX = "x-growl-resource://"

    val LINE_SEPARATOR = "\r\n"

    val TERMINATOR = Array[Byte]('\r', '\n', '\r', '\n')

  }

  // Message types in request

  object RequestMessageType {

    val REGISTER = "REGISTER"

    val NOTIFY = "NOTIFY"

    val SUBSCRIBE = "SUBSCRIBE"

  }

  // Message types in response

  object ResponseMessageType {

    val OK = "-OK"

    val CALLBACK = "-CALLBACK"

    val ERROR = "-ERROR"

  }

}
