package controllers

import play.modules.reactivemongo.ReactiveMongoComponents
import play.api.mvc._
import play.api.http.ContentTypes

private[controllers] trait CommonController extends AbstractController with ContentTypes with ReactiveMongoComponents {

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    EXPIRES       -> "0"
  )

}