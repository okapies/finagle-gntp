package com.github.okapies.finagle.gntp.protocol

import scala.collection.immutable

import com.github.okapies.finagle.gntp.{ErrorCode, GntpHeader, Request}

class GntpRequestDecoder extends GntpMessageDecoder {

  import GntpConstants.RequestMessageType._
  import GntpHeader._

  protected def requiredHeaders: Set[String] =
    messageType match {
      case REGISTER =>
        immutable.Set(APPLICATION_NAME, NOTIFICATIONS_COUNT, NOTIFICATION_NAME)
      case NOTIFY =>
        immutable.Set(APPLICATION_NAME, NOTIFICATION_NAME, NOTIFICATION_TITLE)
      case SUBSCRIBE =>
        immutable.Set(SUBSCRIBER_ID, SUBSCRIBER_NAME)
      case _ =>
        immutable.Set.empty
    }

  protected def decodeMessage(): AnyRef = messageType match {
    case REGISTER => createRegister()
    case NOTIFY => createNotify()
    case SUBSCRIBE => createSubscribe()
    case _ => // invalid message type
      throw new GntpProtocolException(
        ErrorCode.UNKNOWN_PROTOCOL,
        "The message type is invalid: " + messageType)
  }

  private def createRegister(): Request = {
    null // TODO:
  }

  private def createNotify(): Request = {
    null // TODO:
  }

  private def createSubscribe(): Request = {
    null // TODO:
  }

}
