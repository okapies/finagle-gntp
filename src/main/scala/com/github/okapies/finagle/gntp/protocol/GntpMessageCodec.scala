package com.github.okapies.finagle.gntp.protocol

import scala.collection._

import org.apache.commons.codec.binary.Hex
import org.jboss.netty.buffer.ChannelBuffers

import com.twitter.naggati._

import com.github.okapies.finagle.gntp._
import com.github.okapies.finagle.gntp.Header._

private object GntpMessageCodec {

  import GntpConstants.MessageFormat._

  private val CRLF = LINE_SEPARATOR.getBytes

  /**
   * GNTP information line, which is the first line of the message.
   *
   * Definition:
   * `"GNTP/&lt;version&gt; &lt;messagetype&gt; &lt;encryptionAlgorithmID&gt;[:&lt;ivValue&gt;][ &lt;keyHashAlgorithmID&gt;:&lt;keyHash&gt;.&lt;salt&gt;]"`
   */
  private val informationLineMatcher =
    """(?:GNTP/)(.\..)\s+(\S+)\s+([^\s:]+)(?::(\S+))?(?:\s+([^\s:]+):([^\s.]+)\.(\S+))?\s*""".r

}

trait GntpMessageCodec[A <: Message] {

  import GntpMessageCodec._

  import ErrorCode._
  import GntpConstants.MessageFormat._
  import GntpConstants.RequestMessageType.REGISTER

  import com.twitter.naggati.Stages._
  import com.github.okapies.finagle.gntp.util.RichStages._

  def encode: Encoder[A]

  protected def decode(ctx: GntpMessageContext): AnyRef

  @throws(classOf[GntpProtocolException])
  def decode: Stage = readGntpLine {
    case line if line.isEmpty => // skip if the line is empty.
      decode
    case informationLineMatcher(
        version,
        messageType,
        encryptionAlgorithmID, ivValue,
        keyHashAlgorithmID, keyHash, salt) =>
      validateVersion(version)

      val ctx = new GntpMessageContext
      ctx.messageType = messageType
      ctx.encryption = createEncryption(encryptionAlgorithmID, ivValue)
      ctx.authorization = createAuthorization(keyHashAlgorithmID, keyHash, salt)

      decodeHeader(ctx)
    case line =>
      throw new GntpProtocolException(UNKNOWN_PROTOCOL, "Unknow protocol: " + line)
  }

  @throws(classOf[GntpProtocolException])
  private def validateVersion(version: String) {
    if (version != SUPPORTED_VERSION) {
      throw new GntpProtocolException(
        UNKNOWN_PROTOCOL_VERSION, "Unknown protocol version: " + version)
    }
  }

  @throws(classOf[GntpProtocolException])
  private def createEncryption(encryptionAlgorithmID: String, ivValue: String) =
    try {
      encryptionAlgorithmID match {
        case "NONE" => None
        case _ => Some(new Encryption(
          EncryptionAlgorithm.withName(encryptionAlgorithmID),
          Option(Hex.decodeHex(ivValue.toCharArray))))
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
          AuthorizationAlgorithm.withName(keyHashAlgorithmID),
          Hex.decodeHex(keyHash.toCharArray),
          Hex.decodeHex(salt.toCharArray)))
      }
    } catch {
      case e: NoSuchElementException =>
        throw new GntpProtocolException(
          UNKNOWN_PROTOCOL,
          "The authorization algorithm is not supported: " + keyHashAlgorithmID)
    }

  @throws(classOf[GntpProtocolException])
  private def decodeHeader(ctx: GntpMessageContext): NextStep = readHeaders { headers =>
    ctx.headers = headers
    ctx.notificationTypeCount =
      (ctx.messageType, headers.get(NOTIFICATIONS_COUNT)) match {
        case (REGISTER, Some(count)) => count.toInt
        case _ => 0
      }
    ctx.resourceIds = headers.values.collect { // collect <uniqueid> from headers
      case value if value.startsWith(GROWL_RESOURCE_PREFIX) =>
        ResourceId.fromUniqueId(value).get
    }.toSet

    if (ctx.notificationTypeCount > 0) {
      decodeNotificationType(ctx)
    } else if (ctx.resourceIds.size > 0) {
      decodeResourceHeader(ctx)
    } else {
      emit(decode(ctx))
    }
  }

  private def decodeNotificationType(ctx: GntpMessageContext): NextStep = readHeaders { headers =>
    ctx.notificationTypesHeaders = headers +: ctx.notificationTypesHeaders

    if (ctx.notificationTypesHeaders.size < ctx.notificationTypeCount) {
      decodeNotificationType(ctx)
    } else if (ctx.resourceIds.size > 0) {
      decodeResourceHeader(ctx)
    } else {
      emit(decode(ctx))
    }
  }

  private def decodeResourceHeader(ctx: GntpMessageContext): NextStep = readHeader {
    case Some((RESOURCE_IDENTIFIER, id)) => readHeader {
      case Some((RESOURCE_LENGTH, len)) => readGntpLine {
        case "" => // empty <CRLF> line
          decodeResourceData(ctx, id, len.toInt)
        case _ => throw new GntpProtocolException(
          INVALID_REQUEST,
          "The Length header should be followed by an empty <CRLF> line.")
      }
      case _ => throw new GntpProtocolException(
        REQUIRED_HEADER_MISSING, "The Length header is missing in binary section.")
    }
    case _ => throw new GntpProtocolException(
      REQUIRED_HEADER_MISSING, "The Identifier header is missing in binary section.")
  }

  @throws(classOf[GntpProtocolException])
  private def decodeResourceData(ctx: GntpMessageContext, id: String, length: Int): NextStep =
    readBytes(length + 4) { bytes => // read binary data with <CRLF><CRLF>
      val buf = ChannelBuffers.wrappedBuffer(bytes.slice(0, length))

      val resId = ResourceId.fromUniqueValue(id)
      if (ctx.resourceIds.contains(resId)) {
        ctx.resources = ctx.resources + ((resId, Resource(resId, length, buf)))

        if (ctx.resources.size < ctx.resourceIds.size) {
          emit(decode(ctx))
        } else {
          decodeResourceHeader(ctx)
        }
      } else {
        throw new GntpProtocolException(
          INVALID_REQUEST,
          "An unknown resource found in binary section: " + resId.toUniqueId)
      }
    }

  private def readGntpLine(process: String => NextStep) =
    readToMultiByteDelimiter(CRLF) { bytes =>
      process(new String(bytes.slice(0, bytes.length - CRLF.length), ENCODING))
    }

  /**
   * @return return a name and a value of the header, or None if the line is empty.
   */
  private def readHeader(process: Option[(String, String)] => NextStep) = readGntpLine {
    case line if line.isEmpty => // empty <CRLF> line
      process(None)
    case line =>
      val header = Header.parse(line) // value will be trimmed.
      header match {
        case Some(_) => process(header)
        case None => throw new GntpProtocolException(
          INVALID_REQUEST, "A message has a header with invalid format: " + line)
      }
  }

  private def readHeaders(process: Map[String, String] => NextStep): Stage = {
    def readHeadersImpl(headers: Map[String, String]): Stage = readHeader {
      case Some(header) => // read next header
        readHeadersImpl(headers + header)
      case None => // empty <CRLF> line
        process(headers)
    }

    readHeadersImpl(immutable.Map.empty)
  }

}
