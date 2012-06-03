package com.github.okapies.finagle.gntp

import scala.collection._

case class Encryption(

  algorithm: EncryptionAlgorithm,

  iv: Option[Array[Byte]] = None

)

trait EncryptionAlgorithm { def name: String }

object EncryptionAlgorithm {

  case object AES extends EncryptionAlgorithm { val name = "AES" }

  case object DES extends EncryptionAlgorithm { val name = "DES" }

  case object TripleDES extends EncryptionAlgorithm { val name = "3DES" }

  private val names = immutable.Map(
    (AES.name, AES),
    (DES.name, DES),
    (TripleDES.name, TripleDES)
  )

  def withName(s: String): EncryptionAlgorithm = names(s)

}
