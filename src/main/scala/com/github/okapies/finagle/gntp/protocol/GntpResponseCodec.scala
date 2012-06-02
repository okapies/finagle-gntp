package com.github.okapies.finagle.gntp.protocol

import scala.collection._

import com.twitter.naggati.Encoder

import com.github.okapies.finagle.gntp._
import com.twitter.logging.Logger

object GntpResponseCodec extends GntpMessageCodec[Response] {

  private val log = Logger("finagle-gntp")

  import ErrorCode._
  import GntpConstants.RequestMessageType._
  import GntpConstants.ResponseMessageType._
  import Header._
  import util.GntpDateFormat._

  val encode: Encoder[Response] = new GntpResponseEncoder

  @throws(classOf[GntpProtocolException])
  protected def decode(ctx: GntpMessageContext): AnyRef = ctx.messageType match {
    case OK => decodeOk(ctx)
    case CALLBACK => decodeCallback(ctx)
    case ERROR => decodeError(ctx)
    case _ => // invalid message type
      throw new GntpProtocolException(
        UNKNOWN_PROTOCOL,
        "The message type is invalid: " + ctx.messageType)
  }

  @throws(classOf[GntpProtocolException])
  private def decodeOk(ctx: GntpMessageContext): OkResponse = {
    val action = ctx.toRequiredHeader(RESPONSE_ACTION)

    action match {
      case REGISTER =>
        RegisterOkResponse(ctx.unhandledHeaders, ctx.encryption, ctx.authorization)
      case NOTIFY =>
        // NOTE: Notification-ID header is REQUIRED in NOTIFY response,
        // but Growl for Windows v2.0+ and Growl v1.3+ will ignore it.
        // ref. http://code.google.com/p/growl/issues/detail?id=381
        val id = ctx.toOptionalHeader(NOTIFICATION_ID).getOrElse("")
        NotifyOkResponse(id, ctx.unhandledHeaders, ctx.encryption, ctx.authorization)
      case SUBSCRIBE =>
        val ttl = ctx.toRequiredHeader(SUBSCRIPTION_TTL).toInt
        SubscribeOkResponse(ttl, ctx.unhandledHeaders, ctx.encryption, ctx.authorization)
      case _ => throw new GntpProtocolException(
        INVALID_REQUEST, RESPONSE_ACTION + " header has invalid value.")
    }
  }

  @throws(classOf[GntpProtocolException])
  private def decodeCallback(ctx: GntpMessageContext): CallbackResponse = {
    // required
    val appName = ctx.toRequiredHeader(APPLICATION_NAME)
    val id = ctx.toRequiredHeader(NOTIFICATION_ID)
    val result = CallbackResult.withName(ctx.toRequiredHeader(NOTIFICATION_CALLBACK_RESULT))
    val timestamp = toDate(ctx.toRequiredHeader(NOTIFICATION_CALLBACK_TIMESTAMP))
    val cbCtx = ctx.toRequiredHeader(NOTIFICATION_CALLBACK_CONTEXT)
    val cbCtxType = ctx.toRequiredHeader(NOTIFICATION_CALLBACK_CONTEXT_TYPE)

    CallbackResponse(
      appName,
      id,
      result, timestamp, cbCtx, cbCtxType,
      ctx.unhandledHeaders,
      ctx.encryption, ctx.authorization)
  }

  @throws(classOf[GntpServiceException])
  private def decodeError(ctx: GntpMessageContext): ErrorResponse = {
    val code = ErrorCode(ctx.toRequiredHeader(ERROR_CODE).toInt)
    val desc = ctx.toOptionalHeader(ERROR_DESCRIPTION).getOrElse("")

    ErrorResponse(code, desc, ctx.unhandledHeaders, ctx.encryption, ctx.authorization)
  }

}
