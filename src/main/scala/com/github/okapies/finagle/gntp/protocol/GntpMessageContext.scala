package com.github.okapies.finagle.gntp.protocol

import java.net.URI

import scala.collection._

import com.github.okapies.finagle.gntp._

private[protocol] class GntpMessageContext(

  var messageType: String = null,

  var encryption: Option[Encryption] = None,

  var authorization: Option[Authorization] = None,

  var headers: Map[String, String] = immutable.Map.empty,

  var notificationTypeCount: Int = 0,

  var notificationTypesHeaders: Seq[Map[String, String]] = Nil,

  var resourceIds: Set[ResourceId] = immutable.Set.empty,

  var resources: Map[ResourceId, Resource] = immutable.Map.empty

) {

  import ErrorCode._
  import util.GntpBoolean._
  import Header._
  import NotificationType._

  @throws(classOf[GntpProtocolException])
  def toRequiredHeader(name: String): String = toRequiredHeader(this.headers, name)

  @throws(classOf[GntpProtocolException])
  private[this] def toRequiredHeader(headers: Map[String, String], name: String): String =
    headers.get(name) match {
      case Some(value) => value
      case None => throw new GntpProtocolException(
        REQUIRED_HEADER_MISSING, "Required header is missing: " + name)
    }

  def toOptionalHeader(name: String): Option[String] = headers.get(name)

  @throws(classOf[GntpProtocolException])
  def toIcon(name: String): Option[Icon] = toIcon(this.headers, name)

  @throws(classOf[GntpProtocolException])
  private def toIcon(headers: Map[String, String], name: String): Option[Icon] =
    headers.get(name).flatMap {
      value =>
        ResourceId.fromUniqueId(value) match {
          case Some(resId) => resources.get(resId) match {
            case Some(res) => Some(IconImage(res))
            case None => throw new GntpProtocolException(
              INVALID_REQUEST,
              "The specified resource not found in the message: " + resId.toUniqueId)
          }
          case None => Some(IconUri(new URI(value)))
        }
    }

  @throws(classOf[GntpProtocolException])
  def toNotificationTypes: Map[String, NotificationType] =
    notificationTypesHeaders.map { typeHeaders =>
      val name = toRequiredHeader(typeHeaders, NOTIFICATION_NAME)
      (name, NotificationType(
        name,
        typeHeaders.get(NOTIFICATION_DISPLAY_NAME).getOrElse(DEFAULT_DISPLAY_NAME),
        typeHeaders.get(NOTIFICATION_ENABLED).map(parseBoolean _).getOrElse(DEFAULT_ENABLED),
        toIcon(typeHeaders, NOTIFICATION_ICON)))
    }.toMap

  def unhandledHeaders = headers.filter {
    case (name, value) =>
      if (name.startsWith(ORIGIN_HEADER_PREFIX) ||
        name.startsWith(CUSTOM_HEADER_PREFIX) ||
        name.startsWith(DATA_HEADER_PREFIX)) {
        true
      } else {
        false
      }
  }

}
