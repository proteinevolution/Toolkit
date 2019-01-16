package de.proteinevolution.models.database.users

import io.circe.{ Encoder, Json }
import reactivemongo.bson._

case class Location(country: String, countryCode: Option[String], region: Option[String], city: Option[String])

object Location {

  implicit val locationHandler: BSONHandler[BSONDocument, Location] = Macros.handler[Location]

  implicit val locationEncoder: Encoder[Location] = (loc: Location) =>
    Json.fromString(s"${loc.country} - ${loc.city.getOrElse("/")}")

}
