package de.proteinevolution.db

import java.time.ZonedDateTime
import javax.inject.{ Inject, Singleton }

import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.models.database.statistics.{ JobEventLog, StatisticsObject }
import de.proteinevolution.models.database.users.User
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{ UpdateWriteResult, WriteResult }
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.{ BSONDateTime, BSONDocument }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class MongoStore @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext)
    extends ReactiveMongoComponents {

  /*
   *                Job Collection
   */
  /**
   * Basic job collection access with ensured jobID indexing
   */
  lazy val jobCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs")).map { collection =>
      collection.indexesManager.ensure(Index(Seq("jobID" -> IndexType.Text), background = true, unique = true))
      collection
    }
  }

  /**
   * Returns the first job with the matching selector
   * @param selector
   * @return
   */
  def findJob(selector: BSONDocument): Future[Option[Job]] = jobCollection.flatMap(_.find(selector).one[Job])

  /**
   * Returns the first job with the matching jobID
   * @param jobID
   * @return
   */
  def selectJob(jobID: String): Future[Option[Job]] = {
    findJob(BSONDocument("jobID" -> jobID))
  }

  /**
   * Returns all jobs with the matching selectors
   * @param selector
   * @return
   */
  def findJobs(selector: BSONDocument): Future[scala.List[Job]] = {
    jobCollection.map(_.find(selector).cursor[Job]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }

  /**
   * Returns all jobs with the matching selectors
   * @param selector
   * @return
   */
  def findAndSortJobs(selector: BSONDocument, sort: BSONDocument): Future[scala.List[Job]] = {
    jobCollection
      .map(
        _.find(selector).sort(sort).cursor[Job]()
      )
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }

  /**
   * Counts the jobs with the matching selectors
   * @param selector
   * @return
   */
  def countJobs(selector: BSONDocument): Future[Int] = {
    jobCollection.flatMap(_.count(Some(selector)))
  }

  /**
   *  Finds the first / last job with the matching sort
   * @param selector
   * @param sort
   * @return
   */
  def findSortedJob(selector: BSONDocument, sort: BSONDocument): Future[Option[Job]] = {
    jobCollection.flatMap(_.find(selector).sort(sort).one[Job])
  }

  /**
   * Adds a job to the job collection
   * @param job
   * @return
   */
  def insertJob(job: Job): Future[Option[Job]] = {
    jobCollection.flatMap(_.insert(job)).map { a =>
      if (a.ok) { Some(job) } else { None }
    }
  }

  /**
   * Modifies a single Job in the database and returns it
   * @param selector
   * @param modifier
   * @return
   */
  def modifyJob(selector: BSONDocument, modifier: BSONDocument): Future[Option[Job]] = {
    jobCollection.flatMap(
      _.findAndUpdate(
        selector,
        modifier.merge(
          BSONDocument(
            "$set" -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli))
          )
        ),
        fetchNewObject = true
      ).map(_.result[Job])
    )
  }

  /**
   * Updates multiple Jobs in the database but does not return them
   * @param selector
   * @param modifier
   * @return
   */
  def updateJobs(selector: BSONDocument, modifier: BSONDocument): Future[UpdateWriteResult] = {
    jobCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  /**
   * Removes a job from both the job and result collection
   * @param selector
   * @return
   */
  def removeJob(selector: BSONDocument): Future[WriteResult] = {
    jobCollection.flatMap(_.remove(selector))
  }

  /*
   *                Job event Log DB Access
   */
  lazy val eventLogCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobevents"))

  def addJobLog(jobEventLog: JobEventLog): Future[WriteResult] =
    eventLogCollection.flatMap(_.insert(jobEventLog))

  def findJobEventLogs(selector: BSONDocument): Future[scala.List[JobEventLog]] = {
    eventLogCollection
      .map(_.find(selector).cursor[JobEventLog]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[JobEventLog]]()))
  }

  /*
   *                Complete Statistics collection
   */
  /**
   * Basic access to the statistics collection
   */
  lazy val statisticsCol: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("statistics"))

  /**
   * Returns the (only) statistic object in the database
   * @return
   */
  def getStats: Future[StatisticsObject] = {
    statisticsCol.map(_.find(BSONDocument())).flatMap(_.one[StatisticsObject]).map(_.getOrElse(StatisticsObject()))
  }

  /**
   * Updates / inserts and returns the statistic object
   * @param statisticsObject statistic object to update
   * @return
   */
  def updateStats(statisticsObject: StatisticsObject): Future[Option[StatisticsObject]] = {
    statisticsCol.flatMap(
      _.findAndUpdate(selector = BSONDocument(StatisticsObject.IDDB -> statisticsObject.statisticsID),
                      update = statisticsObject,
                      upsert = true,
                      fetchNewObject = true).map(_.result[StatisticsObject])
    )
  }

  /**
   * Modifies the statistics object
   * @param statisticsObject
   * @param modifier
   * @return
   */
  def modifyStats(statisticsObject: StatisticsObject, modifier: BSONDocument): Future[Option[StatisticsObject]] = {
    statisticsCol.flatMap(
      _.findAndUpdate(selector = BSONDocument(StatisticsObject.IDDB -> statisticsObject.statisticsID),
                      update = modifier,
                      fetchNewObject = true).map(_.result[StatisticsObject])
    )
  }

  /*
   *                Cluster load statistics
   */

  /**
   * Basic access to the load statistics collection
   * @return
   */
  lazy val loadStatisticsCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("loadStatistics"))
  }

  /*
   *                User collection access
   */

  /**
   * Basic access to the user collection
   * @return
   */
  lazy val userCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))
  }

  /**
   * Inserts a user to the collection
   * @param user
   * @return
   */
  def addUser(user: User): Future[WriteResult] = userCollection.flatMap(_.insert(user))

  /**
   * Finds a user in the collection
   * @param selector
   * @return
   */
  def findUser(selector: BSONDocument): Future[Option[User]] =
    userCollection.flatMap(_.find(selector).one[User])

  /**
   * Returns multiple users from the collection
   * @param selector
   * @return
   */
  def findUsers(selector: BSONDocument): Future[scala.List[User]] = {
    userCollection.map(_.find(selector).cursor[User]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[User]]()))
  }

  /**
   * Modifies and returns the changed user
   * @param selector
   * @param modifier
   * @return
   */
  def modifyUser(selector: BSONDocument, modifier: BSONDocument): Future[Option[User]] = {
    userCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[User]))
  }

  /**
   * Modifies multiple users
   * @param selector
   * @param modifier
   * @return
   */
  def modifyUsers(selector: BSONDocument, modifier: BSONDocument): Future[WriteResult] = {
    userCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  /**
   * Removes users with the matching selector
   * @param selector
   * @return
   */
  def removeUsers(selector: BSONDocument): Future[WriteResult] = {
    userCollection.flatMap(_.remove(selector))
  }

  /**
   * Overwrites or inserts a User
   * @param user
   * @return
   */
  def upsertUser(user: User): Future[Option[User]] = {
    userCollection.flatMap(
      _.findAndUpdate(selector = BSONDocument(User.IDDB -> user.userID),
                      update = user,
                      upsert = true,
                      fetchNewObject = true).map(_.result[User])
    )
  }
}
