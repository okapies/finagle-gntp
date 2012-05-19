package com.github.okapies.finagle.gntp.protocol

import scala.collection.immutable

import com.github.okapies.finagle.gntp._
import com.github.okapies.finagle.gntp.ErrorCode._

class GntpResponseDecoder extends GntpMessageDecoder {

  import GntpResponseDecoder._

  import GntpConstants.RequestMessageType._
  import GntpConstants.ResponseMessageType._
  import GntpHeader._

  import util.GntpDateFormat._

  @throws(classOf[GntpException])
  protected def decodeMessage(): AnyRef = {
    messageType match {
      case OK => createOk()
      case CALLBACK => createCallback()
      case ERROR => createError() // throws GntpServiceException
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

  @throws(classOf[GntpProtocolException])
  private def createCallback(): CallbackResponse =
    requiredCallbackHeaders.filter(h => !headers.contains(h)) match {
      case missings if missings.size > 0 =>
        throw new GntpProtocolException(
          REQUIRED_HEADER_MISSING,
          "Required headers are missing: " + missings.mkString(", "))
      case _ => {
        val context = headers(NOTIFICATION_CALLBACK_CONTEXT)
        val contextType = headers(NOTIFICATION_CALLBACK_CONTEXT_TYPE)

        val applicationName = headers(APPLICATION_NAME)
        val notificationId = headers(NOTIFICATION_ID)
        val result = CallbackResult.withName(headers(NOTIFICATION_CALLBACK_RESULT))
        val timestamp = toDate(headers(NOTIFICATION_CALLBACK_TIMESTAMP))

        CallbackResponse(
          applicationName, notificationId, result, timestamp, context, contextType, headers)
      }
    }

  @throws(classOf[GntpServiceException])
  private def createError(): AnyRef = {
    val code = headers.get(ERROR_CODE) match {
      case Some(code) => ErrorCode(code.toInt)
      case None => throw new GntpProtocolException(
        REQUIRED_HEADER_MISSING,
        "Required header is missing: %s".format(ERROR_CODE))
    }
    val desc = headers.get(ERROR_DESCRIPTION).getOrElse(null)

    throw new GntpServiceException(code, desc, headers)
  }

}

object GntpResponseDecoder {

  import GntpHeader._

  private val requiredCallbackHeaders = immutable.Set(
    NOTIFICATION_CALLBACK_CONTEXT,
    NOTIFICATION_CALLBACK_CONTEXT_TYPE,
    APPLICATION_NAME,
    NOTIFICATION_ID,
    NOTIFICATION_CALLBACK_RESULT,
    NOTIFICATION_CALLBACK_TIMESTAMP
  )

}
