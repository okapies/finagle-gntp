package com.github.okapies.finagle.gntp.protocol

import java.net.URI

import scala.collection._

import com.twitter.naggati.Encoder

import com.github.okapies.finagle.gntp._

object GntpRequestCodec extends GntpMessageCodec[Request] {

  import ErrorCode._
  import GntpConstants._
  import GntpConstants.RequestMessageType._
  import Header._

  val encode: Encoder[Request] = new GntpRequestEncoder

  protected def decode(ctx: GntpMessageContext): AnyRef = ctx.messageType match {
    case REGISTER => decodeRegister(ctx)
    case NOTIFY => decodeNotify(ctx)
    case SUBSCRIBE => decodeSubscribe(ctx)
    case _ => // invalid message type
      throw new GntpProtocolException(
        ErrorCode.UNKNOWN_PROTOCOL,
        "The message type is invalid: " + ctx.messageType)
  }

  private def decodeRegister(ctx: GntpMessageContext): Request = {
    import NotificationType._

    // required
    val appName = ctx.toRequiredHeader(APPLICATION_NAME)
    val types = ctx.toNotificationTypes

    val appIcon = ctx.toIcon(APPLICATION_ICON)

    Register(
      Application(appName, types, appIcon),
      ctx.unhandledHeaders, ctx.encryption, ctx.authorization)
  }

  private def decodeNotify(ctx: GntpMessageContext): Request = {
    import Notify._

    // required
    val appName = ctx.toRequiredHeader(APPLICATION_NAME)
    val name = ctx.toRequiredHeader(NOTIFICATION_NAME)
    val title = ctx.toRequiredHeader(NOTIFICATION_TITLE)

    // optional
    val id = ctx.toOptionalHeader(NOTIFICATION_ID)
    val text = ctx.toOptionalHeader(NOTIFICATION_TEXT).getOrElse(DEFAULT_TEXT)
    val icon = ctx.toIcon(NOTIFICATION_ICON)
    val sticky = ctx.toOptionalHeader(NOTIFICATION_STICKY).map {
      _.toBoolean
    }.getOrElse(DEFAULT_STICKY)
    val priority = ctx.toOptionalHeader(NOTIFICATION_PRIORITY).map {
      v => Priority(v.toInt)
    }.getOrElse(DEFAULT_PRIORITY)

    val coalescingId = ctx.toOptionalHeader(NOTIFICATION_COALESCING_ID)
    val callback = ctx.toOptionalHeader(NOTIFICATION_CALLBACK_TARGET) match {
      case Some(target) => Some(UrlCallback(new URI(target)))
      case None => ctx.toOptionalHeader(NOTIFICATION_CALLBACK_CONTEXT) match {
        case Some(cbCtx) =>
          val cbCtxType = ctx.toRequiredHeader(NOTIFICATION_CALLBACK_CONTEXT_TYPE)
          Some(SocketCallback(cbCtx, cbCtxType))
        case None => None
      }
    }

    Notify(
      appName, name,
      id,
      title, text, icon, sticky, priority,
      coalescingId, callback,
      ctx.unhandledHeaders, ctx.encryption, ctx.authorization)
  }

  private def decodeSubscribe(ctx: GntpMessageContext): Request = {
    // required
    val id = ctx.toRequiredHeader(SUBSCRIBER_ID)
    val name = ctx.toRequiredHeader(SUBSCRIBER_NAME)

    // optional
    val port = ctx.toOptionalHeader(SUBSCRIBER_PORT).map {
      _.toInt
    }.getOrElse(DEFAULT_GNTP_PORT)

    Subscribe(id, name, port, ctx.unhandledHeaders, ctx.encryption, ctx.authorization)
  }

}
