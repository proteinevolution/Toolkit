/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.jobs.dao

import java.time.ZonedDateTime

import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.jobs.JobState
import de.proteinevolution.jobs.models.{ Job, JobClusterData }
import de.proteinevolution.statistics.{ JobEvent, JobEventLog }
import javax.inject.{ Inject, Singleton }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{ BSONDateTime, BSONDocument }
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{ Cursor, ReadConcern, WriteConcern }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobDao @Inject() (
    private val reactiveMongoApi: ReactiveMongoApi,
    constants: ConstantsV2
)(implicit ec: ExecutionContext) {

  private lazy val jobCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs"))
  }

  private[jobs] lazy val eventLogCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("jobevents"))

  final def findJob(id: String): Future[Option[Job]] =
    jobCollection.flatMap(_.find(BSONDocument(Job.ID -> id), Option.empty[BSONDocument]).one[Job])

  private def internalFindJobs(selector: BSONDocument): Future[List[Job]] =
    jobCollection
      .map(_.find(selector, Option.empty[BSONDocument]).cursor[Job]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))

  def findJobsByHash(hash: Option[String]): Future[List[Job]] =
    internalFindJobs(BSONDocument(Job.HASH -> hash))

  def findJobsByOwnerOrPublicWatched(userID: String, jobs: List[String]): Future[List[Job]] =
    internalFindJobs(
      BSONDocument(
        "$or" -> List(
          BSONDocument(
            Job.ID        -> BSONDocument("$in" -> jobs),
            Job.IS_PUBLIC -> true
          ),
          BSONDocument(Job.OWNER_ID -> userID)
        )
      )
    )

  /**
   * This method gets all the jobs which satisfy one of the following criteria:
   *   1. their tool matches the query string (only for owned jobs) 2. their id matches the query string (public and
   *      owned jobs)
   *
   * @param userID
   *   requesting user
   * @param jobs
   *   jobs which are watched by the user
   * @param jobIDQuery
   *   query string for job id
   * @param toolNames
   *   possible matches with tools
   * @return
   */
  def findJobsByAutocomplete(
      userID: String,
      jobs: List[String],
      jobIDQuery: String,
      toolNames: List[String]
  ): Future[List[Job]] =
    internalFindJobs(
      BSONDocument(
        "$or" -> List(
          BSONDocument(Job.OWNER_ID -> userID, Job.TOOL -> BSONDocument("$in" -> toolNames)),
          BSONDocument(
            Job.ID -> BSONDocument("$regex" -> s"$jobIDQuery.*"),
            BSONDocument(
              "$or" -> List(
                BSONDocument(Job.IS_PUBLIC -> true),
                BSONDocument(Job.OWNER_ID  -> userID)
              )
            )
          )
        )
      )
    )

  /**
   * Get all non-deleted jobs which have not been viewed for the last days and which are scheduled to be deleted
   *
   * @return
   */
  def findOldJobs(): Future[List[Job]] = {
    // grab the current time
    val now: ZonedDateTime = ZonedDateTime.now
    // jobs must not be viewed in the last few days
    val lastViewedDate: ZonedDateTime = now.minusDays(constants.jobDeletionLastViewed.toLong)
    internalFindJobs(
      BSONDocument(
        Job.DATE_VIEWED      -> BSONDocument("$lt" -> BSONDateTime(lastViewedDate.toInstant.toEpochMilli)),
        Job.DATE_DELETION_ON -> BSONDocument("$lt" -> BSONDateTime(now.toInstant.toEpochMilli))
      )
    )
  }

  final def removeJob(jobID: String): Future[WriteResult] = {
    eventLogCollection.foreach(
      _.findAndUpdate(
        BSONDocument(JobEventLog.JOBID -> jobID),
        BSONDocument(
          "$push" ->
          BSONDocument(JobEventLog.EVENTS -> JobEvent(JobState.Deleted, Some(ZonedDateTime.now), Some(0L)))
        ),
        fetchNewObject = true,
        // the following values are default values that are used to distinguish findAndUpdate from deprecated version
        // TODO: why won't it accept it with values left out like in documentation
        upsert = false,
        None,
        None,
        bypassDocumentValidation = false,
        WriteConcern.Default,
        Option.empty,
        Option.empty,
        Seq.empty
      )
    )
    jobCollection.flatMap(_.delete().one(BSONDocument(Job.ID -> jobID)))
  }

  def removeJobs(jobIDs: List[String]): Future[WriteResult] = {
    eventLogCollection.foreach(
      _.findAndUpdate(
        BSONDocument(JobEventLog.JOBID -> BSONDocument("$in" -> jobIDs)),
        BSONDocument(
          "$push" ->
          BSONDocument(JobEventLog.EVENTS -> JobEvent(JobState.Deleted, Some(ZonedDateTime.now), Some(0L)))
        ),
        fetchNewObject = true,
        // the following values are default values that are used to distinguish findAndUpdate from deprecated version
        // TODO: why won't it accept it with values left out like in documentation
        upsert = false,
        None,
        None,
        bypassDocumentValidation = false,
        WriteConcern.Default,
        Option.empty,
        Option.empty,
        Seq.empty
      )
    )
    jobCollection.flatMap(_.delete().one(BSONDocument(Job.ID -> BSONDocument("$in" -> jobIDs))))
  }

  final def findAndSortJobs(hash: String, sort: Int = -1): Future[List[Job]] = {
    jobCollection
      .map(
        _.find(BSONDocument(Job.HASH -> hash), Option.empty[BSONDocument])
          .sort(BSONDocument(Job.DATE_CREATED -> sort))
          .cursor[Job]()
      )
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }

  final def findSortedJob(userID: String, sort: Int = -1): Future[Option[Job]] = {
    jobCollection.flatMap(
      _.find(
        BSONDocument(Job.OWNER_ID -> userID),
        Option.empty[BSONDocument]
      ).sort(BSONDocument(Job.DATE_UPDATED -> sort)).one[Job]
    )
  }

  final def insertJob(job: Job): Future[WriteResult] =
    jobCollection.flatMap(_.insert(ordered = false).one(job))

  private def modifyJob(selector: BSONDocument, modifier: BSONDocument): Future[Option[Job]] = {
    jobCollection.flatMap(
      _.findAndUpdate(
        selector,
        modifier ++
        BSONDocument(
          "$set" -> BSONDocument(Job.DATE_VIEWED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli))
        ),
        fetchNewObject = true,
        // the following values are default values that are used to distinguish findAndUpdate from deprecated version
        // TODO: why won't it accept it with values left out like in documentation
        upsert = false,
        None,
        None,
        bypassDocumentValidation = false,
        WriteConcern.Default,
        Option.empty,
        Option.empty,
        Seq.empty
      ).map(_.result[Job])
    )
  }

  def updateJobStatus(jobID: String, jobState: JobState): Future[Option[Job]] =
    modifyJob(BSONDocument(Job.ID -> jobID), BSONDocument("$set" -> BSONDocument(Job.STATUS -> jobState)))

  def setJobPublic(jobID: String, isPublic: Boolean): Future[Option[Job]] =
    modifyJob(BSONDocument(Job.ID -> jobID), BSONDocument("$set" -> BSONDocument(Job.IS_PUBLIC -> isPublic)))

  def updateSGEID(jobID: String, sgeID: String): Future[Option[Job]] =
    modifyJob(BSONDocument(Job.ID -> jobID), BSONDocument("$set" -> BSONDocument(Job.SGE_ID -> sgeID)))

  def updateClusterDataAndHash(
      jobID: String,
      clusterData: JobClusterData,
      jobHash: Option[String]
  ): Future[Option[Job]] =
    modifyJob(
      BSONDocument(Job.ID -> jobID),
      BSONDocument("$set" -> BSONDocument(Job.CLUSTER_DATA -> clusterData, Job.HASH -> jobHash))
    )

  def addUserToWatchList(jobID: String, userID: String): Future[Option[Job]] =
    modifyJob(BSONDocument(Job.ID -> jobID), BSONDocument("$addToSet" -> BSONDocument(Job.WATCH_LIST -> userID)))

  def removeUserFromWatchList(jobID: String, userID: String): Future[Option[Job]] =
    modifyJob(BSONDocument(Job.ID -> jobID), BSONDocument("$pull" -> BSONDocument(Job.WATCH_LIST -> userID)))

  def countJobsForHashSinceTime(hash: String, time: Long): Future[Long] = {
    jobCollection.flatMap(
      _.count(
        Some(
          BSONDocument(
            "$and" ->
            List(
              BSONDocument(Job.IP_HASH -> hash),
              BSONDocument(
                Job.DATE_CREATED ->
                BSONDocument(
                  "$gt" -> BSONDateTime(time)
                )
              )
            )
          )
        ),
        Some(0),
        0,
        None,
        ReadConcern.Local
      )
    )
  }

  final def addJobLog(jobEventLog: JobEventLog): Future[WriteResult] =
    eventLogCollection.flatMap(_.insert(ordered = false).one(jobEventLog))

  final def findJobEventLogs(instant: Long): Future[scala.List[JobEventLog]] = {
    eventLogCollection
      .map(
        _.find(
          BSONDocument(
            JobEventLog.EVENTS ->
            BSONDocument(
              "$elemMatch" ->
              BSONDocument(
                JobEvent.TIMESTAMP ->
                BSONDocument("$lt" -> BSONDateTime(instant))
              )
            )
          ),
          Option.empty[BSONDocument]
        ).cursor[JobEventLog]()
      )
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[JobEventLog]]()))
  }

}
