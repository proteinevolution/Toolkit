package de.proteinevolution.models.database.users

import reactivemongo.bson._

case class Location(
    country: String,
    countryCode: Option[String],
    region: Option[String],
    city: Option[String]
)

object Location {

  implicit val locationHandler: BSONHandler[BSONDocument, Location] =
    Macros.handler[Location]

}
