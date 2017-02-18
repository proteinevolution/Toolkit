package modules

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import com.sanoma.cda.geoip.MaxMindIpGeo
import com.typesafe.config.ConfigFactory
import models.database.users.Location
import play.api.mvc.RequestHeader



@ImplementedBy(classOf[GeoIPLocationProvider])
trait LocationProvider {

  def getLocation(ipAddress: String): Location

  def getLocation(request: RequestHeader): Location
}


@Singleton
class GeoIPLocationProvider extends LocationProvider {

  // Expensive, this is why this class is a Singleton
  private val geoIp = MaxMindIpGeo(ConfigFactory.load().getString("maxmindDB"), 1000)

  def getLocation(ipAddress : String) : Location = {
    geoIp.getLocation(ipAddress) match {
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


