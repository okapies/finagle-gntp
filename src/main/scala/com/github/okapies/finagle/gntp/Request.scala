package com.github.okapies.finagle.gntp

import java.net.URI

import scala.collection._
import scala.runtime.ScalaRunTime

import com.github.okapies.finagle.gntp.protocol.GntpConstants._

/**
 * Request
 */
sealed trait Request extends Message

/**
 * REGISTER request
 */
case class Register(

  application: Application,

  headers: Map[String, String] = immutable.Map.empty,

  encryption: Option[Encryption] = None,

  authorization: Option[Authorization] = None

) extends Request

/**
 * Application that is registering.
 */
case class Application(

  name: String,

  notificationTypes: Map[String, NotificationType],

  icon: Option[Icon] = None

)

/**
 * NotificationType being registered with an register.
 */
class NotificationType private(

  val name: String,

  val displayName: String,

  val enabled: Boolean,

  val icon: Option[Icon]

) extends Product with Serializable {

  def copy(
      name: String = this.name,
      displayName: String = this.displayName,
      enabled: Boolean = this.enabled,
      icon: Option[Icon] = this.icon) =
    NotificationType.apply(name, displayName, enabled, icon)

  override def productPrefix = "NotificationType"

  def productArity = 4

  def productElement(n: Int): Any = n match {
    case 0 => this.name
    case 1 => this.displayName
    case 2 => this.enabled
    case 3 => this.icon
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  def canEqual(that: Any) = that.isInstanceOf[NotificationType]

  override def equals(that: Any) = ScalaRunTime._equals(this, that)

  override def hashCode() = ScalaRunTime._hashCode(this)

  override def toString = ScalaRunTime._toString(this)

}

object NotificationType {

  val DEFAULT_DISPLAY_NAME: String = null

  val DEFAULT_ENABLED = false

  /**
   * An `NotificationType` factory.
   *
   * @param name the name (type) of the notification being registered
   * @param displayName the name of the notification that is displayed to the user
   *                    (defaults to the same value as `name` if this is not specified)
   * @param enabled indicates if the notification should be enabled by default
   *                (defaults to `false`)
   * @param icon the default icon to use for notifications of this type
   * @return the notification type
   */
  def apply(
      name: String,
      displayName: String = DEFAULT_DISPLAY_NAME,
      enabled: Boolean = DEFAULT_ENABLED,
      icon: Option[Icon] = None) = displayName match {
    case null => new NotificationType(name, name, enabled, icon)
    case _ => new NotificationType(name, displayName, enabled, icon)
  }

  def unapply(n: NotificationType) = Some((n.name, n.displayName, n.enabled, n.icon))

}

/**
 * NOTIFY request
 */
case class Notify(

  applicationName: String,

  name: String,

  id: Option[String] = None,

  title: String,

  text: String = Notify.DEFAULT_TEXT,

  icon: Option[Icon] = None,

  sticky: Boolean = Notify.DEFAULT_STICKY,

  priority: Notify.Priority.Value = Notify.DEFAULT_PRIORITY,

  coalescingId: Option[String] = None,

  callback: Option[Callback] = None,

  headers: Map[String, String] = immutable.Map.empty,

  encryption: Option[Encryption] = None,

  authorization: Option[Authorization] = None

) extends Request

object Notify {

  val DEFAULT_TEXT = ""

  val DEFAULT_STICKY = false

  val DEFAULT_PRIORITY = Notify.Priority.NORMAL

  object Priority extends Enumeration {

    type Priority = Value

    val LOWEST = Value(-2)

    val LOW = Value(-1)

    val NORMAL = Value(0)

    val HIGH = Value(1)

    val HIGHEST = Value(2)

  }

}

/**
 * Subscribe request
 */
case class Subscribe(

  id: String,

  name: String,

  port: Int = DEFAULT_GNTP_PORT,

  headers: Map[String, String] = immutable.Map.empty,

  encryption: Option[Encryption] = None,

  authorization: Option[Authorization] = None

) extends Request

/**
 * Icon
 */
sealed trait Icon

case class IconImage(image: Resource) extends Icon

case class IconUri(uri: URI) extends Icon

object IconUri {

  def apply(str: String): IconUri = IconUri(new URI(str))

}

/**
 * Callback
 */
sealed trait Callback

case class SocketCallback(context: String, contextType: String) extends Callback

case class UrlCallback(target: URI) extends Callback

object UrlCallback {

  def apply(str: String): UrlCallback = UrlCallback(new URI(str))

}
