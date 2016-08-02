package modules

//TODO import plugin from https://github.com/Sanoma-CDA/maxmind-geoip2-scala
import scala.concurrent.duration._

/**
  * Created by zin on 02.08.16.
  */


final class GeoIP(file: String, cacheTtl: Duration) {


}

case class Location(
                     country: String,
                     region: Option[String],
                     city: Option[String]) {


}

object Location {


}