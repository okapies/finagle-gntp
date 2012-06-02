package com.github.okapies.finagle.gntp

import scala.collection._

trait Message {

  def headers: Map[String, String]

  def encryption: Option[Encryption]

  def authorization: Option[Authorization]

}
