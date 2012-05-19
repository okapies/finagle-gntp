package com.github.okapies.finagle.gntp

import java.awt.image.RenderedImage
import java.net.URI
import java.util.Date

import scala.collection._

/**
 * Request
 */
sealed trait Request { def customHeaders: Map[String, Any] }

/**
 * REGISTER request
 */
case class Register(

  application: Application,

  notificationTypes: List[NotificationType],

  customHeaders: Map[String, Any] = immutable.Map.empty

) extends Request

/**
 * Application that is registering.
 */
case class Application(

  name: String,

  icon: Icon = NoIcon

)

/**
 * NotificationType being registered with an register.
 */
case class NotificationType(

  name: String,

  displayName: String = null,

  icon: Icon = NoIcon,

  enabled: Boolean = true

)

/**
 * NOTIFY request
 */
case class Notify(

  applicationName: String,

  name: String,

  id: String = null,

  title: String,

  text: String = null,

  icon: Icon = NoIcon,

  sticky: Boolean = false,

  priority: Notify.Priority.Value = Notify.Priority.NORMAL,

  coalescingId: String = null,

  callback: Callback = NoCallback,

  customHeaders: Map[String, Any] = immutable.Map.empty

) extends Request

object Notify {

  object Priority extends Enumeration {

    type Priority = Value

    val LOWEST = Value(-2)

    val LOW = Value(-1)

    val NORMAL = Value(0)

    val HIGH = Value(1)

    val HIGHEST = Value(2)

  }

  def apply(
      application: Application,
      notificationType: NotificationType,
      id: String,
      title: String,
      text: String): Notify =
    Notify(
      applicationName = application.name,
      name = notificationType.name,
      id = id,
      title = title,
      text = text
    )

}

/**
 * Subscribe request
 */
case class Subscribe(

  id: String,

  name: String,

  port: Int = -1,

  customHeaders: Map[String, Any] = immutable.Map.empty

) extends Request

/**
 * Icon
 */
sealed trait Icon

case object NoIcon extends Icon

case class IconImage(image: RenderedImage) extends Icon

case class IconUri(uri: URI) extends Icon

object IconUri {

  def apply(str: String): IconUri = IconUri(new URI(str))

}

/**
 * Callback
 */
sealed trait Callback

case object NoCallback extends Callback

case class SocketCallback(
  context: String,
  contextType: String
) extends Callback

case class UrlCallback(target: URI) extends Callback

object UrlCallback {

  def apply(str: String): UrlCallback = UrlCallback(new URI(str))

}
