package com.github.okapies.finagle.gntp.protocol

import scala.collection._

import com.github.okapies.finagle.gntp.ErrorCode

class GntpException(message: String, cause: Throwable) extends Exception(message, cause) {

  def this() = this(null, null);

  def this(message: String) = this(message, null);

  def this(cause: Throwable) = this(null, cause);

}

class GntpProtocolException(val code: ErrorCode, message: String, cause: Throwable)
  extends GntpException(message, cause) {

  def this(code: ErrorCode) = this(code, null, null);

  def this(code: ErrorCode, message: String) = this(code, message, null);

  def this(code: ErrorCode, cause: Throwable) = this(code, null, cause);

}

class GntpServiceException(val code: ErrorCode, description: String, headers: Map[String, String])
  extends GntpException(description, null) {

  def this(code: ErrorCode) = this(code, null, null);

  def this(code: ErrorCode, description: String) = this(code, description, null);

}
