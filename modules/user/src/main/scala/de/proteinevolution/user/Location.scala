/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.user

import io.circe.{ Encoder, Json }
import reactivemongo.api.bson._

import com.maxmind.geoip2.model.CityResponse

case class Location(country: String, countryCode: Option[String], region: Option[String], city: Option[String])

object Location {

  implicit val locationHandler: BSONDocumentHandler[Location] = Macros.handler[Location]

  implicit val locationEncoder: Encoder[Location] = (loc: Location) =>
    Json.fromString(s"${loc.country} - ${loc.city.getOrElse("/")}")

  def apply(omni: CityResponse): Location = new Location(
    if (omni.getCountry != null) Option(omni.getCountry.getIsoCode).getOrElse("Solar System") else "Solar System",
    if (omni.getCountry != null) Option(omni.getCountry.getName) else None,
    if (omni.getMostSpecificSubdivision != null) Option(omni.getMostSpecificSubdivision.getName) else None,
    if (omni.getCity != null) Option(omni.getCity.getName) else None
  )

}
