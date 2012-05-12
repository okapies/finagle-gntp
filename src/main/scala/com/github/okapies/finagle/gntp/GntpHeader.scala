package com.github.okapies.finagle.gntp

object GntpHeader {

  // Response-* header

  val RESPONSE_ACTION = "Response-Action"

  // Application-* headers

  val APPLICATION_NAME = "Application-Name"

  val APPLICATION_ICON = "Application-Icon"

  // Notification-* headers

  val NOTIFICATIONS_COUNT = "Notifications-Count"

  val NOTIFICATION_NAME = "Notification-Name"

  val NOTIFICATION_DISPLAY_NAME = "Notification-Display-Name"

  val NOTIFICATION_ENABLED = "Notification-Enabled"

  val NOTIFICATION_ICON = "Notification-Icon"

  val NOTIFICATION_ID = "Notification-ID"

  val NOTIFICATION_TITLE = "Notification-Title"

  val NOTIFICATION_TEXT = "Notification-Text"

  val NOTIFICATION_STICKY = "Notification-Sticky"

  val NOTIFICATION_PRIORITY = "Notification-Priority"

  val NOTIFICATION_COALESCING_ID = "Notification-Coalescing-ID"

  val NOTIFICATION_CALLBACK_RESULT = "Notification-Callback-Result"

  val NOTIFICATION_CALLBACK_TIMESTAMP = "Notification-Callback-Timestamp"

  val NOTIFICATION_CALLBACK_CONTEXT = "Notification-Callback-Context"

  val NOTIFICATION_CALLBACK_CONTEXT_TYPE = "Notification-Callback-Context-Type"

  val NOTIFICATION_CALLBACK_TARGET = "Notification-Callback-Target"

  // Binary section headers

  val RESOURCE_IDENTIFIER = "Identifier"

  val RESOURCE_LENGTH = "Length"

  // Generic headers

  val ORIGIN_MACHINE_NAME = "Origin-Machine-Name"

  val ORIGIN_SOFTWARE_NAME = "Origin-Software-Name"

  val ORIGIN_SOFTWARE_VERSION = "Origin-Software-Version"

  val ORIGIN_PLATFORM_NAME = "Origin-Platform-Name"

  val ORIGIN_PLATFORM_VERSION = "Origin-Platform-Version"

  // ERROR response headers

  val ERROR_CODE = "Error-Code"

  val ERROR_DESCRIPTION = "Error-Description"

  // Received header

  val RECEIVED = "Received"

  // SUBSCRIBE headers

  val SUBSCRIBER_ID = "Subscriber-ID"

  val SUBSCRIBER_NAME = "Subscriber-Name"

  val SUBSCRIBER_PORT = "Subscriber-Port"

  val SUBSCRIPTION_TTL = "Subscription-TTL"

  private val headerMatcher = """([^\r\n:]+):\s+((?:[\s\S]*\Z)|(?:.+))""".r

  def parse(line: String): Option[(String, String)] = line match {
    case headerMatcher(name, value) => Some((name, value))
    case _ => None
  }

}
