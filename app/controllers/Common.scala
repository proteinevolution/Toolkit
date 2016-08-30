package controllers


import play.api.http.ContentTypes
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by zin on 03.08.16.
  */
private[controllers] trait Common
    extends Controller
    with ContentTypes
    with ReactiveMongoComponents {

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


  def userCollection = reactiveMongoApi.database.map(_.collection("users").as[BSONCollection](FailoverStrategy()))

}
