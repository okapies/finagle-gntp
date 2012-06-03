package com.github.okapies.finagle.gntp

import scala.collection._

case class Authorization(

  algorithm: AuthorizationAlgorithm,

  keyHash: Array[Byte],

  salt: Array[Byte]

)

trait AuthorizationAlgorithm { def name: String }

object AuthorizationAlgorithm {

  case object MD5 extends AuthorizationAlgorithm { val name = "MD5" }

  case object SHA1 extends AuthorizationAlgorithm { val name = "SHA1" }

  case object SHA256 extends AuthorizationAlgorithm { val name = "SHA256" }

  case object SHA512 extends AuthorizationAlgorithm { val name = "SHA512" }

  private val names = immutable.Map(
    (MD5.name, MD5),
    (SHA1.name, SHA1),
    (SHA256.name, SHA256),
    (SHA512.name, SHA512)
  )

  def withName(s: String): AuthorizationAlgorithm = names(s)

}
