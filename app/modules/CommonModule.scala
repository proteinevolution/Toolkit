package modules

import models.database.CMS.FeaturedArticle
import models.database.jobs.{FrontendJob, Job, JobAnnotation}
import models.database.statistics.{ClusterLoadEvent, JobEventLog, ToolStatistic}
import models.database.users.User
import play.api.libs.json.JsValue
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import play.api.Logger

import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by zin on 03.08.16.
  */
trait CommonModule extends ReactiveMongoComponents {


  private final def selectjobID(jobID: String) = BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))

  /* Collections */
  protected lazy val hashCollection :Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobhashes"))

  // jobCollection is now a value with the Index structure ensured
  protected lazy val jobCollection : Future[BSONCollection] = {

      reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs")).map { collection =>
        collection.indexesManager.ensure(Index(Seq("jobID" -> IndexType.Text), background = true, unique = true))
        collection
      }
  }
  // articleCollection is now a value with the Index structure ensured
  protected lazy val articleCollection : Future[BSONCollection] = {
    reactiveMongoApi.database.map( _.collection[BSONCollection]("articles")).map { collection =>
      collection.indexesManager.ensure(Index(Seq("dateCreated" -> IndexType.Descending), background = true, unique = true))
      collection
    }
  }

  protected def getArticle(articleID: String): Future[Option[FeaturedArticle]] = articleCollection.flatMap {
    val selector = BSONDocument("articleID" -> BSONDocument("$eq" -> articleID))
    _.find(selector).one[FeaturedArticle]
  }
  protected def getArticles(numArticles: Int): Future[List[FeaturedArticle]] = articleCollection.flatMap {
    val selector = BSONDocument("dateCreated" -> -1)
    _.find(BSONDocument()).sort(selector).cursor[FeaturedArticle]().collect[List](numArticles, Cursor.FailOnError[List[FeaturedArticle]]())
  }
  protected def writeArticleDatabase(featuredArticle: FeaturedArticle) : Future[WriteResult] = articleCollection.flatMap(_.insert(featuredArticle))



  protected lazy val frontendJobCollection : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("frontendjobs"))
  protected lazy val jobAnnotationCollection :Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobannotations"))
  // ResultfilesCollection
  protected lazy val resultCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("results"))

  /* Accesssors */

  protected def addFrontendJob(frontendJob: FrontendJob) : Future[WriteResult] = frontendJobCollection.flatMap(_.insert(frontendJob))


  protected def result2Job(jobID: String, key: String, result: JsValue): Future[Option[BSONDocument]] = {
    val bson = reactivemongo.play.json.BSONFormats.toBSON(result).get
    modifyResult(BSONDocument("jobID" -> jobID), BSONDocument("$set" -> BSONDocument(key -> bson)))
  }

  // Able to fetch one field for the respective result
  protected def getResult(jobID: String): Future[Option[JsValue]] = {
    val selector = BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))
    resultCollection.map(_.find(selector).cursor[BSONDocument]()).flatMap(_.headOption).map {
      case Some(bsonDoc) => Some(reactivemongo.play.json.BSONFormats.toJSON(bsonDoc))
      case None => Logger.info("Could not find JSON file.")  ; None
    }
  }


  protected def findJob(selector : BSONDocument) : Future[Option[Job]] = jobCollection.flatMap(_.find(selector).one[Job])

  protected def findJobAnnotation(selector : BSONDocument) : Future[Option[JobAnnotation]] = jobAnnotationCollection.flatMap(_.find(selector).one[JobAnnotation])

  protected def findJobs(selector : BSONDocument) : Future[scala.List[Job]] = {
    jobCollection.map(_.find(selector).cursor[Job]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }


  protected def findJobSGE(jobID: String) : Future[Job] = {

    val selector = BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))
    val projection = BSONDocument("clusterData" -> 1)
    val job = jobCollection.flatMap(_.find(selector, projection).one[Job]).map {
      case Some(x) => x
    }

    job

  }

  protected def countJobs(selector : BSONDocument) : Future[Int] = {
    jobCollection.flatMap(_.count(Some(selector)))
  }

  /**
    * Finds the first / last job with the matching sort
    */
  protected def findSortedJob(selector : BSONDocument, sort : BSONDocument) : Future[Option[Job]] = {
    jobCollection.flatMap(_.find(selector).sort(sort).one[Job])
  }

  protected def selectJob(jobID: String): Future[Option[Job]] = {

    jobCollection
      .map(_.find(BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))).cursor[Job]())
      .flatMap(_.headOption)
  }



  /**
    * Updates Job in the database with, if a job with the same jobID is already present, otherwise creates the Job
    *
    * @param job
    * @return
    */
  protected def upsertJob(job: Job) : Future[Option[Job]] =  {
    jobCollection.flatMap(_.findAndUpdate(selectjobID(job.jobID), update = job, upsert = true, fetchNewObject = true).map(_.result[Job]))
  }

  protected def insertJob(job : Job) : Future[Option[Job]] = {
    Logger.info("MongoDB Received Job insert: " + job.toString())
    jobCollection.flatMap(_.insert(job).map{ a =>
      Logger.info("MongoDB could not insert Job:\n" + a.writeErrors.mkString(", "))
      if(a.ok) {
        Logger.info("MongoDB inserted job Successfully:\n" + job.toString())
        Some(job)
      } else {
        None
      }
    })
  }

  protected def upsertAnnotation(notes: JobAnnotation) : Future[Option[JobAnnotation]] =  {

    jobAnnotationCollection.flatMap(_.findAndUpdate(selectjobID(notes.jobID), update = notes, upsert = true).map(_.result[JobAnnotation]))
  }

  protected def modifyAnnotation(selector : BSONDocument, modifier : BSONDocument) : Future[Option[Job]] = {
    jobAnnotationCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  // Modifies result in database
  protected def modifyResult(selector: BSONDocument, modifier: BSONDocument) : Future[Option[BSONDocument]] = {
    resultCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true, upsert = true).map(_.result))
  }


  // Modifies a single Job in the database and returns it
  protected def modifyJob(selector : BSONDocument, modifier : BSONDocument) : Future[Option[Job]] = {
    jobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }
  // Updates multiple Jobs in the database but does not return them
  protected def updateJobs(selector : BSONDocument, modifier : BSONDocument) : Future[UpdateWriteResult] = {
    jobCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  protected def modifyFrontendJob(selector : BSONDocument, modifier : BSONDocument) : Future[Option[Job]] = {
    frontendJobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }


  // Job event Log DB Access
  protected def eventLogCollection : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobevents"))

  protected def addJobLog(jobEventLog : JobEventLog) : Future[WriteResult] = eventLogCollection.flatMap(_.insert(jobEventLog))

  protected def findJobEventLogs(selector : BSONDocument) : Future[scala.List[JobEventLog]] = {
    eventLogCollection.map(_.find(selector).cursor[JobEventLog]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[JobEventLog]]()))
  }

  // Statistics DB access
  protected def statisticsCollection : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("statistics"))

  protected def getStatistics : Future[scala.List[ToolStatistic]] = {
    statisticsCollection.map(_.find(BSONDocument.empty).cursor[ToolStatistic]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[ToolStatistic]]()))
  }

  protected def addStatistic(toolStatistic : ToolStatistic) : Future[WriteResult] = statisticsCollection.flatMap(_.insert(toolStatistic))

  protected def upsertStatistics(toolStatistic : ToolStatistic) : Future[Option[ToolStatistic]] = {
    statisticsCollection.flatMap(_.findAndUpdate(selector = BSONDocument(ToolStatistic.IDDB -> toolStatistic.toolID),
                                                 update   = toolStatistic, upsert = true).map(_.result[ToolStatistic]))
  }

  protected def increaseJobCount(toolName : String, failed : Boolean = false) : Future[WriteResult] = {
    statisticsCollection.flatMap(_.update(BSONDocument(ToolStatistic.TOOLNAME -> toolName),
                                          BSONDocument("$inc"                 ->
                                          BSONDocument({if (failed) {ToolStatistic.CURRENTFAILED}
                                                        else        {ToolStatistic.CURRENT}}
                                                                              -> 1))))
  }

  protected def loadStatisticsCollection : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("loadStatistics"))

  protected def upsertLoadStatistic(clusterLoadEvent: ClusterLoadEvent) : Future[Option[ClusterLoadEvent]] =
    loadStatisticsCollection.flatMap(_.findAndUpdate(selector = BSONDocument(ClusterLoadEvent.IDDB -> clusterLoadEvent.id),
                                                     update   = clusterLoadEvent,
                                                     upsert   = true,
                                                     fetchNewObject = true).map(_.result[ClusterLoadEvent]))

  // User DB access
  protected def userCollection : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

  protected def addUser(user : User) : Future[WriteResult] = userCollection.flatMap(_.insert(user))

  protected def findUser(selector : BSONDocument) : Future[Option[User]] = userCollection.flatMap(_.find(selector).one[User])

  protected def findUsers(selector : BSONDocument) : Future[scala.List[User]] = {
    userCollection.map(_.find(selector).cursor[User]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[User]]()))
  }

  protected def modifyUser(selector : BSONDocument, modifier : BSONDocument) : Future[Option[User]] = {
    userCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[User]))
  }

  protected def upsertUser(user : User) : Future[Option[User]] = {
    userCollection.flatMap(_.findAndUpdate(selector = BSONDocument(User.IDDB -> user.userID),
                                           update   = user,
                                           upsert   = true,
                                           fetchNewObject = true).map(_.result[User]))
  }

  //protected def removeUser(selector : BSONDocument) : Future[WriteResult] = userCollection.flatMap(_.remove(selector))
}
