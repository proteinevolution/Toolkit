package controllers


import play.modules.reactivemongo.ReactiveMongoComponents
import play.api.mvc._
import play.api.http.ContentTypes

/**
  *
  * Created by snam on 12.11.16.
  */
private[controllers] trait Common extends AbstractController with ContentTypes with ReactiveMongoComponents {


  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    EXPIRES       -> "0"
  )


}
// Exceptions
case class FileException(message: String) extends Exception(message)


