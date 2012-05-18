package com.github.okapies.finagle.gntp.protocol

import java.io.InputStreamReader

import scala.collection._

import org.jboss.netty.buffer.{ChannelBufferInputStream, ChannelBuffer}
import org.jboss.netty.channel.{Channel, ChannelHandlerContext}
import org.jboss.netty.handler.codec.replay.ReplayingDecoder
import org.slf4j.LoggerFactory

import com.github.okapies.finagle.gntp.protocol.GntpMessageDecoderState._
import com.github.okapies.finagle.gntp._

abstract class GntpMessageDecoder
  extends ReplayingDecoder[GntpMessageDecoderState](READ_INFO_AND_HEADER, true) {

  import GntpMessageDecoder._

  import com.github.okapies.finagle.gntp.ErrorCode._
  import com.github.okapies.finagle.gntp.GntpHeader._
  import com.github.okapies.finagle.gntp.protocol.GntpConstants._
  import com.github.okapies.finagle.gntp.protocol.GntpConstants.MessageFormat._
  import com.github.okapies.finagle.gntp.protocol.GntpConstants.RequestMessageType._

  protected var messageType: String = null

  protected var encryption: Option[Encryption] = None

  protected var authorization: Option[Authorization] = None

  protected var headers: Map[String, String] = immutable.Map.empty

  protected var notificationTypeCount:Int = 0

  protected var notificationTypes: List[NotificationType] = Nil

  protected var resourceIds: Set[String] = immutable.Set.empty

  protected var resources: Map[String, Resource] = immutable.Map.empty

  @throws(classOf[Exception])
  override def decode(
      ctx: ChannelHandlerContext,
      channel: Channel,
      buffer: ChannelBuffer,
      state: GntpMessageDecoderState): AnyRef = state match {
    case READ_INFO_AND_HEADER => {
      val reader = newMessageReader(buffer)
      parseInformationLine(reader) match {
        case Some((msgType, enc, auth)) => {
          this.messageType = msgType
          this.encryption = enc
          this.authorization = auth

          this.headers = parseHeader(reader)
          this.notificationTypeCount =
            (messageType, headers.get(NOTIFICATIONS_COUNT)) match {
              case (REGISTER, Some(count)) => count.toInt
              case _ => 0
            }
          this.resourceIds = headers.values.collect {
            case value if value.startsWith(GROWL_RESOURCE_PREFIX) =>
              value.substring(GROWL_RESOURCE_PREFIX.length).trim
          }.toSet

          if (notificationTypeCount > 0) {
            checkpoint(READ_NOTIFICATION_TYPE)
            null
          } else if (resourceIds.size > 0) {
            checkpoint(READ_RESOURCE_HEADER)
            null
          } else {
            reset()
          }
        }
        case None => null // no message
      }
    }
    case READ_NOTIFICATION_TYPE => {
      val reader = newMessageReader(buffer)
      val notificationType = parseNotificationType(reader)
      notificationTypes = notificationType :: notificationTypes

      if (notificationTypes.size < notificationTypeCount) {
        checkpoint(READ_NOTIFICATION_TYPE)
        null
      } else if (resourceIds.size > 0) {
        checkpoint(READ_RESOURCE_HEADER)
        null
      } else {
        reset()
      }
    }
    case READ_RESOURCE_HEADER => {
      val reader = newMessageReader(buffer)
      val (id, length) = parseResourceHeader(reader)
      if (resourceIds.contains(id)) {
        ctx.setAttachment((id, length)) // set frame length.

        checkpoint(READ_RESOURCE_DATA)
        null
      } else {
        throw new GntpProtocolException(
          INVALID_REQUEST,
          "Received a resource with invalid id: " + id)
      }
    }
    case READ_RESOURCE_DATA => {
      val (id: String, length: Int) = ctx.getAttachment
      val data: ChannelBuffer = buffer.readBytes(length)
      resources = resources + ((id, Resource(id, length, data)))

      ctx.setAttachment(null) // reset frame length

      if (resources.size < resourceIds.size) {
        checkpoint(READ_RESOURCE_HEADER)
        null
      } else {
        reset()
      }
    }
    case _ =>
      throw new Error("Shouldn't reach heer.")
  }

  override def checkpoint(state: GntpMessageDecoderState) {
    super.checkpoint(state)
    log.debug("State updated: " + state)
  }

  private def reset(): AnyRef = {
    // decode message
    val message = messageType match {
      case null => null
      case _ => decodeMessage()
    }
    log.debug("Received a message: " + message)

    // clear fields
    this.messageType = null
    this.encryption = None
    this.authorization = None
    this.headers = immutable.Map.empty
    this.notificationTypeCount = 0
    this.notificationTypes = Nil
    this.resourceIds = immutable.Set.empty
    this.resources = immutable.Map.empty

    // reset checkpoint
    checkpoint(READ_INFO_AND_HEADER)

    message
  }

  protected def decodeMessage(): AnyRef

  @throws(classOf[GntpProtocolException])
  private def parseInformationLine(
      reader: MessageReader): Option[(String, Option[Encryption], Option[Authorization])] = {
    val firstLine = reader.readLine()
    firstLine match {
      case null => None
      case informationLineMatcher(
          version,
          messageType,
          encryptionAlgorithmID, ivValue,
          keyHashAlgorithmID, keyHash, salt) =>
        version match {
          case SUPPORTED_VERSION => {
            val enc = createEncryption(encryptionAlgorithmID, ivValue)
            val auth = createAuthorization(keyHashAlgorithmID, keyHash, salt)

            Some(messageType, enc, auth)
          }
          case _ =>
            throw new GntpProtocolException(
              UNKNOWN_PROTOCOL_VERSION,
              "Unknown protocol version: " + version)
        }
      case _ =>
        throw new GntpProtocolException(UNKNOWN_PROTOCOL, "Unknow protocol: " + firstLine)
    }
  }

  @throws(classOf[GntpProtocolException])
  private def createEncryption(encryptionAlgorithmID: String, ivValue: String) =
    try {
      val algorithm = EncryptionAlgorithm.withName(encryptionAlgorithmID)
      algorithm match {
        case EncryptionAlgorithm.NONE => None
        case _ => Some(new Encryption(algorithm, Option(ivValue)))
      }
    } catch {
      case e: NoSuchElementException =>
        throw new GntpProtocolException(
          UNKNOWN_PROTOCOL,
          "The encryption algorithm is not supported: " + encryptionAlgorithmID)
    }

  @throws(classOf[GntpProtocolException])
  private def createAuthorization(keyHashAlgorithmID: String, keyHash: String, salt: String) =
    try {
      keyHashAlgorithmID match {
        case null => None
        case _ => Some(new Authorization(
          AuthorizationAlgorithm.withName(keyHashAlgorithmID), keyHash, salt))
      }
    } catch {
      case e: NoSuchElementException =>
        throw new GntpProtocolException(
          UNKNOWN_PROTOCOL,
          "The authorization algorithm is not supported: " + keyHashAlgorithmID)
    }

  @throws(classOf[GntpProtocolException])
  private def parseHeader(reader: MessageReader): Map[String, String] =
    Stream.continually(reader.readLine()).takeWhile(_ != null).map {
      case line => GntpHeader.parse(line) match {
        case Some(pair) => pair
        case None => throw new GntpProtocolException(
          UNKNOWN_PROTOCOL,
          "A message has a header with invalid format: " + line)
      }
    }.foldLeft(Map.empty[String, String])(_ + _)

  private def parseNotificationType(reader: MessageReader): NotificationType = {
    val headers = parseHeader(reader)

    null
  }

  private def parseResourceHeader(reader: MessageReader): (String, Int) = {
    val headers = parseHeader(reader)

    null
  }

  private def newMessageReader(buffer: ChannelBuffer) =
    new MessageReader(
      new InputStreamReader(new ChannelBufferInputStream(buffer), ENCODING))

}

object GntpMessageDecoder {

  private val log = LoggerFactory.getLogger(classOf[GntpMessageDecoder])

  /**
   * GNTP information line, which is the first line of the message.
   *
   * Definition:
   * `"GNTP/&lt;version&gt; &lt;messagetype&gt; &lt;encryptionAlgorithmID&gt;[:&lt;ivValue&gt;][ &lt;keyHashAlgorithmID&gt;:&lt;keyHash&gt;.&lt;salt&gt;]"`
   */
  private val informationLineMatcher =
    """(?:GNTP/)(.\..)\s+(\S+)\s+([^\s:]+)(?::(\S+))?(?:\s+([^\s:]+):([^\s.]+)\.(\S+))?""".r

  private[protocol] def hasRequiredHeader(
      headers: Map[String, String], required: Set[String]): Option[Set[String]] = {
    required.filter(name => !headers.contains(name)) match {
      case missings if missings.size > 0 => Some(missings)
      case _ => None
    }
  }

}
