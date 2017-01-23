package controllers


import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoComponents
import play.api.mvc.Controller
import play.api.http.ContentTypes

/**
 *
 * Created by snam on 12.11.16.
 */


private[controllers] trait Common extends Controller with ContentTypes with ReactiveMongoComponents {

  var loggedOut = true


  protected def CheckBackendPath(implicit request: RequestHeader) : Boolean = {
    request.headers.get("referer").getOrElse("").matches("http://" + request.host + "/@/backend.*")
  }

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate", EXPIRES -> "0"
  )


}
