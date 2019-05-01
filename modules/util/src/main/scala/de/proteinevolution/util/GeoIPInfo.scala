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

import java.net.InetAddress

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CityResponse

import scala.concurrent.ExecutionContext
import scala.util.Try

final class GeoIPInfo private (reader: DatabaseReader) {

  def getLocation(ip: String)(implicit ec: ExecutionContext): Option[CityResponse] =
    Try(reader.city(InetAddress.getByName(ip))).toOption

}

object GeoIPInfo {

  def apply(path: String): GeoIPInfo = {
    val in  = getClass.getResourceAsStream(path)
    val ref = new DatabaseReader.Builder(in).build()
    new GeoIPInfo(ref)
  }

}
