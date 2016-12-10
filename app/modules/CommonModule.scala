package modules

import models.database.{FrontendJob, Job, User}
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by zin on 03.08.16.
  */
trait CommonModule extends ReactiveMongoComponents {


  /* Collections */
  protected def hashCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobhashes"))

  // Provide index for JobID
  protected def jobCollection = {

    val x = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs"))
    // Establish a text index of the jobID, ensure uniqueness
    x.map(_.indexesManager.ensure(Index(Seq("jobID" -> IndexType.Text), background = true, unique = true)))
    x
  }



  /* Accessors */
  protected def frontendJobCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("frontendjobs"))

  protected def addJob(job: Job) = jobCollection.flatMap(_.insert(job))

  protected def addFrontendJob(frontendJob: FrontendJob) = frontendJobCollection.flatMap(_.insert(frontendJob))

  protected def findJob(selector : BSONDocument) = jobCollection.flatMap(_.find(selector).one[Job])

  protected def findFrontendJob(selector : BSONDocument) = frontendJobCollection.flatMap(_.find(selector).one[Job])

  protected def findJobs(selector : BSONDocument) = {
    jobCollection.map(_.find(selector).cursor[Job]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }

  protected def selectJob(jobID: String): Future[Option[Job]] = {

    jobCollection
      .map(_.find(BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))).cursor[Job]())
      .flatMap(_.headOption)
  }

  protected def modifyJob(selector : BSONDocument, modifier : BSONDocument) = {
    jobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  protected def modifyFrontendJob(selector : BSONDocument, modifier : BSONDocument) = {
    frontendJobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  protected def updateJob(selector : BSONDocument, modifier : BSONDocument) = {
    jobCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  protected def updateFrontendJob(selector : BSONDocument, modifier : BSONDocument) = {
    frontendJobCollection.flatMap(_.update(selector, modifier, multi = true))
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
