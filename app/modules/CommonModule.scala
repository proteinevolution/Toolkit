package modules

import models.database.{Job, User}
import play.api.http.ContentTypes
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by zin on 03.08.16.
  */
trait CommonModule extends ReactiveMongoComponents {


  protected def hashCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobhashes"))

  // Job DB access
  protected def jobCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs"))

  protected def addJob(job: Job) = jobCollection.flatMap(_.insert(job))

  protected def findJob(selector : BSONDocument) = jobCollection.flatMap(_.find(selector).one[Job])
  protected def findJobs(selector : BSONDocument) = {
    jobCollection.map(_.find(selector).cursor[Job]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }

  protected def modifyJob(selector : BSONDocument, modifier : BSONDocument) = {
    jobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  protected def updateJob(selector : BSONDocument, modifier : BSONDocument) = {
    jobCollection.flatMap(_.update(selector, modifier, multi = true))
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
