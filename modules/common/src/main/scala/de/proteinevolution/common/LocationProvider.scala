package de.proteinevolution.common

import javax.inject.{ Inject, Singleton }
import com.google.inject.ImplementedBy
import com.tgf.pizza.geoip.MaxMindIpGeo
import de.proteinevolution.models.database.users.Location
import play.api.Configuration
import play.api.mvc.RequestHeader

@ImplementedBy(classOf[LocationProviderImpl])
sealed trait LocationProvider {

  def getLocation(ipAddress: String): Location

  def getLocation(request: RequestHeader): Location

}

@Singleton
class LocationProviderImpl @Inject()(config: Configuration) extends LocationProvider {

  private val geoIp = MaxMindIpGeo(config.get[String]("maxmindDB"), 1000)

  def getLocation(ipAddress: String): Location = {
    geoIp.getLocation(ipAddress) match {
      case Some(ipLocation) =>
        Location(ipLocation.countryName.getOrElse("Solar System"),
                 ipLocation.countryCode,
                 ipLocation.region,
                 ipLocation.city)
      case None =>
        Location("Solar System", None, None, None)
    }
  }

  def getLocation(request: RequestHeader): Location = {
    getLocation(request.remoteAddress)
  }
}
