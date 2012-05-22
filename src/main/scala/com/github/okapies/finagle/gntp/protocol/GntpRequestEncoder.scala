package com.github.okapies.finagle.gntp.protocol

import java.net.URI
import java.util.Date

import scala.collection._

import com.github.okapies.finagle.gntp._

class GntpRequestEncoder extends GntpMessageEncoder {

  import ErrorCode._
  import GntpConstants.RequestMessageType._
  import Header._

  def encodeMessage(
      request: Request, encryption: Encryption, authorization: Option[Authorization]) {
    try {
      request match {
        case register: Register => {
          informationLine(REGISTER, encryption, authorization)
          encodeRegister(register)
        }
        case notify: Notify => {
          informationLine(NOTIFY, encryption, authorization)
          encodeNotify(notify)
        }
        case subscribe: Subscribe => {
          informationLine(SUBSCRIBE, encryption, authorization)
          encodeSubscribe(subscribe)
        }
      }

      // custom headers
      request.customHeaders foreach {
        case (name, value) if name.startsWith("X-") => {
          value match {
            case str: String => header(name, str)
            case i: java.lang.Integer => header(name, i)
            case b: java.lang.Boolean => header(name, b)
            case date: Date => header(name, date)
            case uri: URI => header(name, uri)
            case _ => throw new IllegalArgumentException(
              "The value is unsupported type: %s".format(name))
          }
        }
        case _ => throw new GntpProtocolException(
          INVALID_REQUEST, "Custom headers must start with 'X-'.")
      }
    } finally {
      // terminator
      emptyLine()
    }
  }

  private def encodeRegister(register: Register) {
    header(APPLICATION_NAME, register.application.name) // required
    iconHeader(APPLICATION_ICON, register.application.icon)

    header(NOTIFICATIONS_COUNT, register.notificationTypes.size) // required
    register.notificationTypes.foreach { t =>
      emptyLine()
      header(NOTIFICATION_NAME, t.name) // required
      header(NOTIFICATION_DISPLAY_NAME, t.displayName)
      header(NOTIFICATION_ENABLED, t.enabled)
      iconHeader(NOTIFICATION_ICON, t.icon)
    }
  }

  private def encodeNotify(notify: Notify) {
    header(APPLICATION_NAME, notify.applicationName) // required
    header(NOTIFICATION_NAME, notify.name) // required
    header(NOTIFICATION_ID, notify.id)
    header(NOTIFICATION_TITLE, notify.title) // required
    header(NOTIFICATION_TEXT, notify.text)

    header(NOTIFICATION_STICKY, notify.sticky)
    header(NOTIFICATION_PRIORITY, notify.priority.id)
    iconHeader(NOTIFICATION_ICON, notify.icon)
    header(NOTIFICATION_COALESCING_ID, notify.coalescingId)

    notify.callback match {
      case SocketCallback(context, contextType) => {
        header(NOTIFICATION_CALLBACK_CONTEXT, context)
        header(NOTIFICATION_CALLBACK_CONTEXT_TYPE, contextType)
      }
      case UrlCallback(target) => {
        header(NOTIFICATION_CALLBACK_TARGET, target)
      }
      case _ =>
    }
  }

  private def encodeSubscribe(subscribe: Subscribe) {
    header(SUBSCRIBER_ID, subscribe.id) // required
    header(SUBSCRIBER_NAME, subscribe.name) // required

    subscribe.port match {
      case port if port >= 0 =>
        header(SUBSCRIBER_PORT, subscribe.port)
      case _ =>
    }
  }

}
