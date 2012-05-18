package com.github.okapies.finagle.gntp.protocol

import scala.collection._

import com.github.okapies.finagle.gntp._

class GntpRequestEncoder extends GntpMessageEncoder {

  import GntpConstants.RequestMessageType._
  import GntpHeader._

  def encodeMessage(
      request: Request, encryption: Encryption, authorization: Option[Authorization]) {
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
  }

  private def encodeRegister(register: Register) {
    header(APPLICATION_NAME, register.application.name)
    iconHeader(APPLICATION_ICON, register.application.icon)

    header(NOTIFICATIONS_COUNT, register.notificationTypes.size)
    register.notificationTypes.foreach { t =>
      emptyLine()
      header(NOTIFICATION_NAME, t.name)
      header(NOTIFICATION_DISPLAY_NAME, t.displayName)
      header(NOTIFICATION_ENABLED, t.enabled)
      iconHeader(NOTIFICATION_ICON, t.icon)
    }

    // terminator
    emptyLine()
  }

  private def encodeNotify(notify: Notify) {
  }

  private def encodeSubscribe(subscribe: Subscribe) {
    // TODO:
  }

}
