package com.github.okapies.finagle.gntp

import org.jboss.netty.buffer.ChannelBuffer

import protocol.GntpConstants.MessageFormat._

case class Resource(id: ResourceId, length: Long, data: ChannelBuffer)

object ResourceId {

  def fromUniqueId(uniqueId: String): Option[ResourceId] = uniqueId match {
    case _ if uniqueId.startsWith(GROWL_RESOURCE_PREFIX) => Some(ResourceId(uniqueId))
    case _ => None
  }

  def fromUniqueValue(uniqueValue: String) = ResourceId(GROWL_RESOURCE_PREFIX + uniqueValue)

}

case class ResourceId(uniqueId: String) {

  /**
   * ex. x-growl-resource://6027F6C0-64AF-11DD-9779-EEDA55D89593
   */
  def toUniqueId = uniqueId

  /**
   * ex. 6027F6C0-64AF-11DD-9779-EEDA55D89593
   */
  def toUniqueValue = uniqueId.substring(GROWL_RESOURCE_PREFIX.length)

}
