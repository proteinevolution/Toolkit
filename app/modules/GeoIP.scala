package modules

import com.sanoma.cda.geoip.MaxMindIpGeo
import com.typesafe.config.ConfigFactory
import models.database.Location
import play.api.mvc.RequestHeader

trait GeoIP {
  private val geoIp = MaxMindIpGeo(ConfigFactory.load().getString("maxmindDB"), 1000)

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