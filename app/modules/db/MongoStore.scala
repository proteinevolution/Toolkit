package modules.db

import javax.inject.Inject
import javax.inject.Singleton

import models.database.CMS.FeaturedArticle
import models.database.jobs.{ FrontendJob, Job, JobAnnotation }
import models.database.statistics.{ ClusterLoadEvent, JobEventLog, ToolStatistic }
import models.database.users.{ User, UserData }
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.JsValue
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{ UpdateWriteResult, WriteResult }
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.{ BSONDateTime, BSONDocument }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

/**
  * Created by zin on 03.08.16.
  *
  * TODO may need to break this into multiple classes in the future  *
  *
  */
@Singleton
final class MongoStore @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends ReactiveMongoComponents {

  def selectjobID(jobID: String) = BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))

  /* Collections */
  lazy val hashCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobhashes"))

  // jobCollection is now a value with the Index structure ensured
  lazy val jobCollection: Future[BSONCollection] = {

    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs")).map { collection =>
      collection.indexesManager.ensure(Index(Seq("jobID" -> IndexType.Text), background = true, unique = true))
      collection
    }
  }
  // articleCollection is now a value with the Index structure ensured
  lazy val articleCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("articles")).map { collection =>
      collection.indexesManager
        .ensure(Index(Seq("dateCreated" -> IndexType.Descending), background = true, unique = true))
      collection
    }
  }

  def getArticle(articleID: String): Future[Option[FeaturedArticle]] = articleCollection.flatMap {
    val selector = BSONDocument("articleID" -> BSONDocument("$eq" -> articleID))
    _.find(selector).one[FeaturedArticle]
  }
  def getArticles(numArticles: Int): Future[List[FeaturedArticle]] = articleCollection.flatMap {
    val selector = BSONDocument("dateCreated" -> -1)
    _.find(BSONDocument())
      .sort(selector)
      .cursor[FeaturedArticle]()
      .collect[List](numArticles, Cursor.FailOnError[List[FeaturedArticle]]())
  }
  def writeArticleDatabase(featuredArticle: FeaturedArticle): Future[WriteResult] =
    articleCollection.flatMap(_.insert(featuredArticle))

  lazy val frontendJobCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("frontendjobs"))
  lazy val jobAnnotationCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobannotations"))
  // ResultfilesCollection
  lazy val resultCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("results"))

  /* Accesssors */

  def addFrontendJob(frontendJob: FrontendJob): Future[WriteResult] =
    frontendJobCollection.flatMap(_.insert(frontendJob))

  def result2Job(jobID: String, x: BSONDocument): Future[Option[BSONDocument]] = {
    modifyResult(BSONDocument("jobID" -> jobID), BSONDocument("$set" -> x))
  }

  // Able to fetch one field for the respective result
  def getResult(jobID: String): Future[Option[JsValue]] = {
    val selector = BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))
    resultCollection.map(_.find(selector).cursor[BSONDocument]()).flatMap(_.headOption).map {
      case Some(bsonDoc) => Some(reactivemongo.play.json.BSONFormats.toJSON(bsonDoc))
      case None          => Logger.info("Could not find JSON file."); None
    }
  }

  def findJob(selector: BSONDocument): Future[Option[Job]] = jobCollection.flatMap(_.find(selector).one[Job])

  def findJobAnnotation(selector: BSONDocument): Future[Option[JobAnnotation]] =
    jobAnnotationCollection.flatMap(_.find(selector).one[JobAnnotation])

  def findJobs(selector: BSONDocument): Future[scala.List[Job]] = {
    jobCollection.map(_.find(selector).cursor[Job]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }

  def findJobSGE(jobID: String): Future[Job] = {

    val selector   = BSONDocument("jobID"       -> BSONDocument("$eq" -> jobID))
    val projection = BSONDocument("clusterData" -> 1)
    val job = jobCollection.flatMap(_.find(selector, projection).one[Job]).map {
      case Some(x) => x
    }

    job

  }

  /**
    * Finds the first / last job with the matching sort
    */
  def findSortedJob(selector: BSONDocument, sort: BSONDocument): Future[Option[Job]] = {
    jobCollection.flatMap(_.find(selector).sort(sort).one[Job])
  }

  def selectJob(jobID: String): Future[Option[Job]] = {

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
  def upsertJob(job: Job): Future[Option[Job]] = {
    jobCollection.flatMap(
      _.findAndUpdate(selectjobID(job.jobID), update = job, upsert = true, fetchNewObject = true).map(_.result[Job])
    )
  }

  def insertJob(job: Job): Future[Option[Job]] = {
    jobCollection.flatMap(_.insert(job)).map { a =>
      if (a.ok) { Some(job) } else { None }
    }
  }

  def upsertAnnotation(notes: JobAnnotation): Future[Option[JobAnnotation]] = {

    jobAnnotationCollection.flatMap(
      _.findAndUpdate(selectjobID(notes.jobID), update = notes, upsert = true).map(_.result[JobAnnotation])
    )
  }

  def modifyAnnotation(selector: BSONDocument, modifier: BSONDocument): Future[Option[Job]] = {
    jobAnnotationCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  // Modifies result in database
  def modifyResult(selector: BSONDocument, modifier: BSONDocument): Future[Option[BSONDocument]] = {
    resultCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true, upsert = true).map { x =>
      x.result
    })
  }

  // Modifies a single Job in the database and returns it
  def modifyJob(selector: BSONDocument, modifier: BSONDocument): Future[Option[Job]] = {
    jobCollection.flatMap(
      _.findAndUpdate(
        selector,
        modifier.merge(BSONDocument("$set" -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(DateTime.now().getMillis)))),
        fetchNewObject = true
      ).map(_.result[Job])
    )
  }
  // Updates multiple Jobs in the database but does not return them
  def updateJobs(selector: BSONDocument, modifier: BSONDocument): Future[UpdateWriteResult] = {
    jobCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  def modifyFrontendJob(selector: BSONDocument, modifier: BSONDocument): Future[Option[Job]] = {
    frontendJobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  // Job event Log DB Access
  def eventLogCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobevents"))

  def addJobLog(jobEventLog: JobEventLog): Future[WriteResult] =
    eventLogCollection.flatMap(_.insert(jobEventLog))

  def findJobEventLogs(selector: BSONDocument): Future[scala.List[JobEventLog]] = {
    eventLogCollection
      .map(_.find(selector).cursor[JobEventLog]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[JobEventLog]]()))
  }

  // Statistics DB access
  def statisticsCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("statistics"))

  def getStatistics: Future[scala.List[ToolStatistic]] = {
    statisticsCollection
      .map(_.find(BSONDocument.empty).cursor[ToolStatistic]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[ToolStatistic]]()))
  }

  def addStatistic(toolStatistic: ToolStatistic): Future[WriteResult] =
    statisticsCollection.flatMap(_.insert(toolStatistic))

  def upsertStatistics(toolStatistic: ToolStatistic): Future[Option[ToolStatistic]] = {
    statisticsCollection.flatMap(
      _.findAndUpdate(selector = BSONDocument(ToolStatistic.IDDB -> toolStatistic.toolID),
                      update = toolStatistic,
                      upsert = true).map(_.result[ToolStatistic])
    )
  }

  def increaseJobCount(toolName: String, failed: Boolean = false): Future[WriteResult] = {
    statisticsCollection.flatMap(
      _.update(
        BSONDocument(ToolStatistic.TOOLNAME -> toolName),
        BSONDocument(
          "$inc" ->
          BSONDocument({ if (failed) { ToolStatistic.CURRENTFAILED } else { ToolStatistic.CURRENT } }
          -> 1)
        )
      )
    )
  }

  def loadStatisticsCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("loadStatistics"))

  def upsertLoadStatistic(clusterLoadEvent: ClusterLoadEvent): Future[Option[ClusterLoadEvent]] =
    loadStatisticsCollection.flatMap(
      _.findAndUpdate(selector = BSONDocument(ClusterLoadEvent.IDDB -> clusterLoadEvent.id),
                      update = clusterLoadEvent,
                      upsert = true,
                      fetchNewObject = true).map(_.result[ClusterLoadEvent])
    )

  // User DB access
  def userCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

  def addUser(user: User): Future[WriteResult] = userCollection.flatMap(_.insert(user))

  def findUser(selector: BSONDocument): Future[Option[User]] =
    userCollection.flatMap(_.find(selector).one[User])

  def getUserData(selector: BSONDocument): Future[Option[UserData]] =
    userCollection.flatMap(_.find(selector).one[UserData])

  def findUsers(selector: BSONDocument): Future[scala.List[User]] = {
    userCollection.map(_.find(selector).cursor[User]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[User]]()))
  }

  def modifyUser(selector: BSONDocument, modifier: BSONDocument): Future[Option[User]] = {
    userCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[User]))
  }

  def upsertUser(user: User): Future[Option[User]] = {
    userCollection.flatMap(
      _.findAndUpdate(selector = BSONDocument(User.IDDB -> user.userID),
                      update = user,
                      upsert = true,
                      fetchNewObject = true).map(_.result[User])
    )
  }

  //def removeUser(selector : BSONDocument) : Future[WriteResult] = userCollection.flatMap(_.remove(selector))
}
