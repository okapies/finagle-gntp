package com.github.okapies.finagle.gntp

import java.util.Date

import scala.collection._

/**
 * Response
 */
sealed trait Response { def headers: Map[String, String] }

object Response {

  def unapply(response: Response) = Some(response.headers)

}

/**
 * OK response
 */
trait OkResponse extends Response

object OkResponse {

  def unapply(response: OkResponse) = Some(response.headers)

}

case class RegisterOkResponse(
  headers: Map[String, String]) extends OkResponse

case class NotifyOkResponse(
  notificationId: String,
  headers: Map[String, String]) extends OkResponse

case class SubscribeOkResponse(
  ttl: Long,
  headers: Map[String, String]) extends OkResponse

/**
 * ERROR response
 */
case class ErrorResponse(
  code: ErrorCode,
  description: String,
  headers: Map[String, String]) extends Response

/**
 * CALLBACK response
 */
case class CallbackResponse(
  applicationName: String,
  notificationId: String,
  result: CallbackResult,
  timestamp: Date,
  context: String,
  contextType: String,
  headers: Map[String, String]) extends Response
