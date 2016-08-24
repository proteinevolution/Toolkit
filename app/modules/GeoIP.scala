package modules

import com.sanoma.cda.geoip.MaxMindIpGeo
import models.database.Location
import play.api.mvc.RequestHeader

final class GeoIP(file: String) {
  private val geoIp = MaxMindIpGeo(file, 1000)

  def getLocation(ipAddress : String) : Location = {
    geoIp.getLocation.apply(ipAddress) match {
      case Some(ipLocation) =>
        Location(ipLocation.countryName.getOrElse("Solar System"), ipLocation.countryCode, ipLocation.region, ipLocation.city)
      case None =>
        Location("Solar System", None, None, None)
    }
  }

  def getLocation(request: RequestHeader) : Location = {
    getLocation(request.remoteAddress)
  }
}