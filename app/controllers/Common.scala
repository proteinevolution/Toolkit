package controllers

import play.api.http.ContentTypes
import play.api.mvc._

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

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate", EXPIRES -> "0"
  )
}
