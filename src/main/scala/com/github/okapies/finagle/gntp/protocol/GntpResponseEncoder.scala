package com.github.okapies.finagle.gntp.protocol

import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}

import com.twitter.naggati.Encoder

import com.github.okapies.finagle.gntp._

class GntpResponseEncoder extends Encoder[Response] {

  import GntpConstants.RequestMessageType._
  import GntpConstants.ResponseMessageType._
  import Header._

  def encode(response: Response): Option[ChannelBuffer] = {
    val w = new GntpMessageWriter(ChannelBuffers.dynamicBuffer)

    response match {
      case register: RegisterOkResponse => encodeRegisterOkResponse(w, register)
      case notify: NotifyOkResponse => encodeNotifyOkResponse(w, notify)
      case subscribe: SubscribeOkResponse => encodeSubscribeOkResponse(w, subscribe)
      case callback: CallbackResponse => encodeCallbackResponse(w, callback)
      case error: ErrorResponse => encodeErrorResponse(w, error)
    }

    // other informational headers (generic, custom and data)
    w.headers(response.headers)

    // TODO: binary sections

    // terminator
    w.emptyLine()

    Some(w.toChannelBuffer)
  }

  private def encodeRegisterOkResponse(w: GntpMessageWriter, register: RegisterOkResponse) {
    w.informationLine(OK, register.encryption, register.authorization)

    w.header(RESPONSE_ACTION, REGISTER)
  }

  private def encodeNotifyOkResponse(w: GntpMessageWriter, notify: NotifyOkResponse) {
    w.informationLine(OK, notify.encryption, notify.authorization)

    w.header(RESPONSE_ACTION, NOTIFY)
    w.header(NOTIFICATION_ID, notify.id)
  }

  private def encodeSubscribeOkResponse(w: GntpMessageWriter, subscribe: SubscribeOkResponse) {
    w.informationLine(OK, subscribe.encryption, subscribe.authorization)

    w.header(RESPONSE_ACTION, SUBSCRIBE)
    w.header(SUBSCRIPTION_TTL, subscribe.ttl)
  }

  private def encodeCallbackResponse(w: GntpMessageWriter, callback: CallbackResponse) {
    w.informationLine(CALLBACK, callback.encryption, callback.authorization)

    w.header(APPLICATION_NAME, callback.applicationName)
    w.header(NOTIFICATION_ID, callback.notificationId)
    w.header(NOTIFICATION_CALLBACK_RESULT, callback.result.name)
    w.header(NOTIFICATION_CALLBACK_TIMESTAMP, callback.timestamp)
    w.header(NOTIFICATION_CALLBACK_CONTEXT, callback.context)
    w.header(NOTIFICATION_CALLBACK_CONTEXT_TYPE, callback.contextType)
  }

  private def encodeErrorResponse(w: GntpMessageWriter, error: ErrorResponse) {
    w.informationLine(ERROR, error.encryption, error.authorization)

    w.header(ERROR_CODE, error.code.value)
    w.header(ERROR_DESCRIPTION, error.description)
  }

}
