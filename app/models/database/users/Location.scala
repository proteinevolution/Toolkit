package models.database.users

import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
  * Created by astephens on 19.08.16.
  */
case class Location(country: String, countryCode: Option[String], region: Option[String], city: Option[String])

object Location {
  final val COUNTRY     = "country"
  final val COUNTRYCODE = "countryCode"
  final val REGION      = "region"
  final val CITY        = "city"

  implicit object Reader extends BSONDocumentReader[Location] {
    override def read(bson: BSONDocument): Location = Location(
      country = bson.getAs[String](COUNTRY).getOrElse("none"),
      countryCode = bson.getAs[String](COUNTRYCODE),
      region = bson.getAs[String](REGION),
      city = bson.getAs[String](CITY)
    )
  }

  implicit object Writer extends BSONDocumentWriter[Location] {
    override def write(location: Location): BSONDocument = BSONDocument(
      COUNTRY     -> location.country,
      COUNTRYCODE -> location.countryCode,
      REGION      -> location.region,
      CITY        -> location.city
    )
  }
}
