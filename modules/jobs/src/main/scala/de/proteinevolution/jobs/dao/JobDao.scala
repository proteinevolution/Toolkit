package de.proteinevolution.jobs.dao

import java.time.ZonedDateTime

import de.proteinevolution.jobs.models.Job
import de.proteinevolution.models.database.statistics.JobEventLog
import javax.inject.{ Inject, Singleton }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{ Cursor, ReadConcern }
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.{ BSONDateTime, BSONDocument }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobDao @Inject()(private val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  private lazy val jobCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs")).map { collection =>
      collection.indexesManager.ensure(Index(Seq(Job.JOBID -> IndexType.Text), background = true, unique = true))
      collection
    }
  }

  private[jobs] lazy val eventLogCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobevents"))

  def findJob(selector: BSONDocument): Future[Option[Job]] = jobCollection.flatMap(_.find(selector, None).one[Job])

  def findJobs(selector: BSONDocument): Future[List[Job]] = {
    jobCollection
      .map(_.find(selector, None).cursor[Job]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }

  def selectJob(jobID: String): Future[Option[Job]] = {
    findJob(BSONDocument(Job.JOBID -> jobID))
  }

  def removeJob(selector: BSONDocument): Future[WriteResult] = {
    jobCollection.flatMap(_.delete().one(selector))
  }

  def findAndSortJobs(selector: BSONDocument, sort: BSONDocument): Future[List[Job]] = {
    jobCollection
      .map(_.find(selector, None).sort(sort).cursor[Job]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }

  def findSortedJob(selector: BSONDocument, sort: BSONDocument): Future[Option[Job]] = {
    jobCollection.flatMap(_.find(selector, None).sort(sort).one[Job])
  }

  def insertJob(job: Job): Future[Option[Job]] = {
    jobCollection.flatMap(_.insert(job)).map { a =>
      if (a.ok) { Some(job) } else { None }
    }
  }

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

  def countJobs(selector: BSONDocument): Future[Long] = {
    jobCollection.flatMap(_.count(Some(selector), Some(0), 0, None, ReadConcern.Local))
  }

  def addJobLog(jobEventLog: JobEventLog): Future[WriteResult] =
    eventLogCollection.flatMap(_.insert(jobEventLog))

  def findJobEventLogs(selector: BSONDocument): Future[scala.List[JobEventLog]] = {
    eventLogCollection
      .map(_.find(selector, None).cursor[JobEventLog]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[JobEventLog]]()))
  }

}
