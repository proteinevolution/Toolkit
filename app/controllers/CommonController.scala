package controllers

import play.api.mvc._
import play.api.http.ContentTypes

private[controllers] trait CommonController extends AbstractController with ContentTypes {

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    EXPIRES       -> "0"
  )

}
