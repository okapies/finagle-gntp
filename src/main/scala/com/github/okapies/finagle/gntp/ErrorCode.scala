package com.github.okapies.finagle.gntp

import scala.collection._

abstract class ErrorCode(val value: Int, val name: String)

object ErrorCode {

  case object RESERVED extends ErrorCode(100, "RESERVED")

  case object TIMED_OUT extends ErrorCode(200, "TIMED_OUT")

  case object NETWORK_FAILURE extends ErrorCode(201, "NETWORK_FAILURE")

  case object INVALID_REQUEST extends ErrorCode(300, "INVALID_REQUEST")

  case object UNKNOWN_PROTOCOL extends ErrorCode(301, "UNKNOWN_PROTOCOL")

  case object UNKNOWN_PROTOCOL_VERSION extends ErrorCode(302, "UNKNOWN_PROTOCOL_VERSION")

  case object REQUIRED_HEADER_MISSING extends ErrorCode(303, "REQUIRED_HEADER_MISSING")

  case object NOT_AUTHORIZED extends ErrorCode(400, "NOT_AUTHORIZED")

  case object UNKNOWN_APPLICATION extends ErrorCode(401, "UNKNOWN_APPLICATION")

  case object UNKNOWN_NOTIFICATION extends ErrorCode(402, "UNKNOWN_NOTIFICATION")

  case object ALREADY_PROCESSED extends ErrorCode(403, "ALREADY_PROCESSED")

  case object NOTIFICATION_DISABLED extends ErrorCode(404, "NOTIFICATION_DISABLED")

  case object INTERNAL_SERVER_ERROR extends ErrorCode(500, "INTERNAL_SERVER_ERROR")

  case class UnknownErrorCode(override val value: Int) extends ErrorCode(value, null)

  private val values = immutable.Map(
    (RESERVED.value, RESERVED),
    (TIMED_OUT.value, TIMED_OUT),
    (NETWORK_FAILURE.value, NETWORK_FAILURE),
    (INVALID_REQUEST.value, INVALID_REQUEST),
    (UNKNOWN_PROTOCOL.value, UNKNOWN_PROTOCOL),
    (UNKNOWN_PROTOCOL_VERSION.value, UNKNOWN_PROTOCOL_VERSION),
    (REQUIRED_HEADER_MISSING.value, REQUIRED_HEADER_MISSING),
    (NOT_AUTHORIZED.value, NOT_AUTHORIZED),
    (UNKNOWN_APPLICATION.value, UNKNOWN_APPLICATION),
    (UNKNOWN_NOTIFICATION.value, UNKNOWN_NOTIFICATION),
    (ALREADY_PROCESSED.value, ALREADY_PROCESSED),
    (NOTIFICATION_DISABLED.value, NOTIFICATION_DISABLED),
    (INTERNAL_SERVER_ERROR.value, INTERNAL_SERVER_ERROR)
  )

  def apply(value: Int) = values.getOrElse(value, UnknownErrorCode(value))

}
