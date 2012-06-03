package com.github.okapies.finagle.gntp.util

object GntpBoolean {

  def parseBoolean(s: String): Boolean =
    s.toLowerCase match {
      case "yes" | "true" => true
      case "no" | "false" => false
      case _ => throw new NumberFormatException("For input string: \""+s+"\"")
    }

}
