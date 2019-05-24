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

package de.proteinevolution.util

import com.google.inject.ImplementedBy
import de.proteinevolution.user.Location
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.mvc.RequestHeader
import scala.util.Try

import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[LocationProviderImpl])
sealed trait LocationProvider {

  def getLocation(ipAddress: String): Location

  def getLocation(request: RequestHeader): Location

}

@Singleton
class LocationProviderImpl @Inject()(config: Configuration)(implicit ec: ExecutionContext) extends LocationProvider {

  private[this] val geoIp = GeoIPInfo(config.get[String]("maxmind_db"))

  def getLocation(ipAddress: String): Location = {
    geoIp.getLocation(ipAddress) match {
      case Some(cityResponse) =>
        Location(
          Try(cityResponse.getCountry.getName).getOrElse("Solar System"),
          Try(cityResponse.getCountry.getIsoCode).toOption,
          Try(cityResponse.getLeastSpecificSubdivision.getName).toOption,
          Try(cityResponse.getCity.getName).toOption
        )
      case None =>
        Location("Solar System", None, None, None)
    }
  }

  def getLocation(request: RequestHeader): Location = getLocation(request.remoteAddress)

}
