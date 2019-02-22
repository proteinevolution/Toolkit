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
import reactivemongo.bson._

case class Location(country: String, countryCode: Option[String], region: Option[String], city: Option[String])

object Location {

  implicit val locationHandler: BSONHandler[BSONDocument, Location] = Macros.handler[Location]

  implicit val locationEncoder: Encoder[Location] = (loc: Location) =>
    Json.fromString(s"${loc.country} - ${loc.city.getOrElse("/")}")

}
