package de.proteinevolution.models.util

import java.time.format.DateTimeFormatter
import java.time.{ Instant, ZoneId, ZonedDateTime }

import reactivemongo.bson.BSONDateTime

object ZonedDateTimeHelper {

  final val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss O")
  final val defaultZoneID: ZoneId                = ZoneId.systemDefault()
  def getZDT(bsonDateTime: BSONDateTime): ZonedDateTime = {
    Instant.ofEpochMilli(bsonDateTime.value).atZone(defaultZoneID)
  }

}
