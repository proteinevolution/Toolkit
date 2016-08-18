package controllers

import models.database.User
import play.api.http.ContentTypes
import play.api.mvc._
import modules.GeoIP

/**
  * Created by zin on 03.08.16.
  */
private[controllers] trait Common
    extends Controller
    with ContentTypes {

  var loggedOut = true


  protected implicit final class PimpedResult(result: Result) {
    def fuccess = scala.concurrent.Future successful result
  }

  protected def CheckBackendPath(implicit request: RequestHeader) : Boolean = {
    request.headers.get("referer").getOrElse("").matches("http://" + request.host + "/@/backend.*")
  }

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate", EXPIRES -> "0"
  )

  val geoIP = new GeoIP("/ebio/abt1_share/toolkit_support1/data/GeoLite2-City.mmdb")
}
