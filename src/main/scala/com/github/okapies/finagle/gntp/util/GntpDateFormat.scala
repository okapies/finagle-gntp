package com.github.okapies.finagle.gntp.util

import java.util.Date
import java.text.SimpleDateFormat

object GntpDateFormat {

  private val format = new SimpleDateFormat("yyyy-MM-dd mm:hh:ssZ")

  def toDate(s: String): Date = format.parse(s)

  def toString(date: Date): String = format.format(date)

}
