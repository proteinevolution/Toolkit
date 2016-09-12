package controllers


import models.database.{Job, User}
import play.api.http.ContentTypes
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
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


  protected def hashCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobhashes"))

  // Job DB access
  protected def jobCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs"))

  protected def findJob(selector : BSONDocument) = userCollection.flatMap(_.find(selector).one[Job])

  protected def modifyJob(selector : BSONDocument, modifier : BSONDocument) = {
    jobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  protected def removeJob(selector : BSONDocument) = jobCollection.flatMap(_.remove(selector))


  // User DB access
  protected def userCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

  protected def addUser(user : User) = userCollection.flatMap(_.insert(user))

  protected def findUser(selector : BSONDocument) = userCollection.flatMap(_.find(selector).one[User])

  protected def modifyUser(selector : BSONDocument, modifier : BSONDocument) = {
    userCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[User]))
  }
  protected def removeUser(selector : BSONDocument) = userCollection.flatMap(_.remove(selector))
}
