package com.github.okapies.finagle.gntp.protocol

import scala.collection.immutable

import com.github.okapies.finagle.gntp._
import com.github.okapies.finagle.gntp.ErrorCode._

class GntpResponseDecoder extends GntpMessageDecoder {

  import GntpConstants.RequestMessageType._
  import GntpConstants.ResponseMessageType._
  import GntpHeader._

  protected def requiredHeaders: Set[String] =
    messageType match {
      case OK => null
      case CALLBACK => {
        val context = headers.get(NOTIFICATION_CALLBACK_CONTEXT)
        val contextType = headers.get(NOTIFICATION_CALLBACK_CONTEXT_TYPE)
        val target = headers.get(NOTIFICATION_CALLBACK_TARGET)
        if (target.isDefined) {
          // URL Callbacks
          immutable.Set(NOTIFICATION_CALLBACK_TARGET)
        } else if (context.isDefined && contextType.isDefined) {
          // Socket Callbacks
          immutable.Set(
            NOTIFICATION_CALLBACK_CONTEXT,
            NOTIFICATION_CALLBACK_CONTEXT_TYPE,
            APPLICATION_NAME,
            NOTIFICATION_ID,
            NOTIFICATION_CALLBACK_RESULT,
            NOTIFICATION_CALLBACK_TIMESTAMP
          )
        } else {
          // It should fail because the message has no required headers.
          immutable.Set(
            NOTIFICATION_CALLBACK_CONTEXT,
            NOTIFICATION_CALLBACK_CONTEXT_TYPE,
            NOTIFICATION_CALLBACK_TARGET
          )
        }
      }
      case ERROR => immutable.Set.empty
      case _ => immutable.Set.empty
    }

  protected def decodeMessage(): AnyRef = {
    messageType match {
      case OK => createOk()
      case CALLBACK => createCallback()
      case ERROR => createError()
      case _ => // invalid message type
        throw new GntpProtocolException(
          UNKNOWN_PROTOCOL,
          "The message type is invalid: " + messageType)
    }
  }

  @throws(classOf[GntpProtocolException])
  private def createOk(): OkResponse = {
    val action = headers.get(RESPONSE_ACTION) match {
      case Some(action) => action
      case None => throw new GntpProtocolException(
        REQUIRED_HEADER_MISSING, RESPONSE_ACTION + " header is required.")
    }

    action match {
      case REGISTER => RegisterOkResponse(headers)
      case NOTIFY => headers.get(NOTIFICATION_ID) match {
        case Some(id) => NotifyOkResponse(id, headers)
        case None => throw new GntpProtocolException(
          REQUIRED_HEADER_MISSING,
          "Required header is missing: %s".format(NOTIFICATION_ID))
      }
      case SUBSCRIBE => headers.get(SUBSCRIPTION_TTL) match {
        case Some(ttl) => SubscribeOkResponse(ttl.toLong, headers)
        case None => throw new GntpProtocolException(
          REQUIRED_HEADER_MISSING,
          "Required header is missing: %s".format(SUBSCRIPTION_TTL))
      }
      case _ => throw new GntpProtocolException(
        REQUIRED_HEADER_MISSING, RESPONSE_ACTION + " header has invalid value.")
    }
  }

  private def createCallback(): CallbackResponse = {
    CallbackResponse(null, null, null, null, null, null, headers) // TODO:
  }

  private def createError(): ErrorResponse = {
    val code = headers.get(ERROR_CODE) match {
      case Some(code) => ErrorCode(code.toInt)
      case None => throw new GntpProtocolException(
        REQUIRED_HEADER_MISSING,
        "Required header is missing: %s".format(ERROR_CODE))
    }
    val desc = headers.get(ERROR_DESCRIPTION).getOrElse(null)

    ErrorResponse(code, desc, headers)
  }

}
