package util

import java.time._
import java.time.format._
import java.time.temporal._

object Time {
  /**
    * Checks the time difference in seconds between two given times as strings, the strings must be formatted as
    * "year-month-day hour:minute:second" ("yyyy-mm-dd hh:mm:ss
    */
  def timeDifference(time1: String, time2: String): Long = {
    /** Incase no time defined, return biggest value */
    if (time1.contains("0000-00-00 00:00:00") || time2.contains("0000-00-00 00:00:00")) return Long.MaxValue

    /** Remove square brackets and parse time from String */
    val t1 = LocalDateTime.parse(time1.replaceAll("""\[|\]""", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("Europe/London"))
    val t2 = LocalDateTime.parse(time2.replaceAll("""\[|\]""", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("Europe/London"))

    /** Compare two dates, can change SECONDS to any other if needed */
    return t1.until(t2, ChronoUnit.SECONDS)
  }

  /**
    * Converts a long in milliseconds to a string formatted as "hour:minute:seconds" ("hh:mm:ss")
    */
  def toHHMMSS(secs: Long): String = {
    return "%02d:%02d:%02d".format((secs % 86400) / 3600, (secs % 3600) / 60, secs % 60)
  }

  /**
    * Gets the current timestamp as a string formatted as yearMONTHday_HOURminuteSECOND
    */
  def getCurrentTimestamp(): String = {
    return LocalDateTime.now.format(DateTimeFormatter.ofPattern("YYYYMMdd_HHmmss"))
  }
}