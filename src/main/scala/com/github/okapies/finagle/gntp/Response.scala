package com.github.okapies.finagle.gntp

import java.util.Date

import scala.collection._

/**
 * Response
 */
sealed trait Response extends Message

sealed trait SuccessfulResponse extends Response

/**
 * OK response
 */
sealed trait OkResponse extends SuccessfulResponse

case class RegisterOkResponse(

  headers: Map[String, String] = immutable.Map.empty,

  encryption: Option[Encryption] = None,

  authorization: Option[Authorization] = None

) extends OkResponse

case class NotifyOkResponse(

  id: String,

  headers: Map[String, String] = immutable.Map.empty,

  encryption: Option[Encryption] = None,

  authorization: Option[Authorization] = None

) extends OkResponse

case class SubscribeOkResponse(

  ttl: Int,

  headers: Map[String, String] = immutable.Map.empty,

  encryption: Option[Encryption] = None,

  authorization: Option[Authorization] = None

) extends OkResponse

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

  headers: Map[String, String] = immutable.Map.empty,

  encryption: Option[Encryption] = None,

  authorization: Option[Authorization] = None

) extends SuccessfulResponse

/**
 * ERROR response
 */
case class ErrorResponse(

  code: ErrorCode,

  description: String,

  headers: Map[String, String] = immutable.Map.empty,

  encryption: Option[Encryption] = None,

  authorization: Option[Authorization] = None

) extends Response
