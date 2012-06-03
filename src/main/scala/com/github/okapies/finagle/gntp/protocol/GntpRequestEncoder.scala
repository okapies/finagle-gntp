package com.github.okapies.finagle.gntp.protocol

import org.jboss.netty.buffer.{ChannelBuffer, ChannelBuffers}

import com.twitter.naggati.Encoder

import com.github.okapies.finagle.gntp._

class GntpRequestEncoder extends Encoder[Request] {

  import GntpConstants.RequestMessageType._
  import Header._

  def encode(request: Request): Option[ChannelBuffer] = {
    val w = new GntpMessageWriter(ChannelBuffers.dynamicBuffer)

    request match {
      case register: Register => encodeRegister(w, register)
      case notify: Notify => encodeNotify(w, notify)
      case subscribe: Subscribe => encodeSubscribe(w, subscribe)
    }

    // other headers (generic, custom and data)
    w.headers(request.headers)

    // TODO: binary sections

    // terminator
    w.emptyLine()

    Some(w.toChannelBuffer)
  }

  private def encodeRegister(w: GntpMessageWriter, register: Register) {
    w.informationLine(REGISTER, register.encryption, register.authorization)

    val app = register.application
    w.header(APPLICATION_NAME, app.name) // required
    w.iconHeader(APPLICATION_ICON, app.icon)

    val types = app.notificationTypes
    w.header(NOTIFICATIONS_COUNT, types.size) // required
    types.foreach { case (name, t) =>
      w.emptyLine()
      w.header(NOTIFICATION_NAME, name) // required
      w.header(NOTIFICATION_DISPLAY_NAME, t.displayName)
      w.header(NOTIFICATION_ENABLED, t.enabled)
      w.iconHeader(NOTIFICATION_ICON, t.icon)
    }
  }

  private def encodeNotify(w: GntpMessageWriter, notify: Notify) {
    w.informationLine(NOTIFY, notify.encryption, notify.authorization)

    w.header(APPLICATION_NAME, notify.applicationName) // required
    w.header(NOTIFICATION_NAME, notify.name) // required
    w.header(NOTIFICATION_ID, notify.id)
    w.header(NOTIFICATION_TITLE, notify.title) // required
    w.header(NOTIFICATION_TEXT, notify.text)

    w.header(NOTIFICATION_STICKY, notify.sticky)
    w.header(NOTIFICATION_PRIORITY, notify.priority.id)
    w.iconHeader(NOTIFICATION_ICON, notify.icon)
    w.header(NOTIFICATION_COALESCING_ID, notify.coalescingId)

    notify.callback match {
      case Some(SocketCallback(context, contextType)) => {
        w.header(NOTIFICATION_CALLBACK_CONTEXT, context)
        w.header(NOTIFICATION_CALLBACK_CONTEXT_TYPE, contextType)
      }
      case Some(UrlCallback(target)) => {
        w.header(NOTIFICATION_CALLBACK_TARGET, target)
      }
      case None =>
    }
  }

  private def encodeSubscribe(w: GntpMessageWriter, subscribe: Subscribe) {
    w.informationLine(SUBSCRIBE, subscribe.encryption, subscribe.authorization)

    w.header(SUBSCRIBER_ID, subscribe.id) // required
    w.header(SUBSCRIBER_NAME, subscribe.name) // required

    subscribe.port match {
      case port if port >= 0 =>
        w.header(SUBSCRIBER_PORT, subscribe.port)
      case _ =>
    }
  }

}
