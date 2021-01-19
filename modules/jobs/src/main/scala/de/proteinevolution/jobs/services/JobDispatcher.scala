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

package de.proteinevolution.jobs.services

import cats.data.{ EitherT, OptionT }
import cats.implicits._
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.common.models.{ ConstantsV2, ToolName }
import de.proteinevolution.jobs.actors.JobActor.PrepareJob
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.{ Job, JobSubmitError }
import de.proteinevolution.user.{ AccountType, User }
import play.api.Logging

import java.security.MessageDigest
import java.time.ZonedDateTime
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class JobDispatcher @Inject() (
    jobDao: JobDao,
    constants: ConstantsV2,
    jobIdProvider: JobIdProvider,
    jobActorAccess: JobActorAccess,
    userSessionService: UserSessionService,
    userDao: UserDao
)(implicit ec: ExecutionContext)
    extends Logging {

  def submitJob(
      toolName: String,
      parts: Map[String, String],
      user: User
  ): EitherT[Future, JobSubmitError, Job] = {
    if (!modellerKeyIsValid(toolName, user)) {
      EitherT.leftT(JobSubmitError.ModellerKeyInvalid)
    } else {
      for {
        generatedId     <- generateJobId(parts)
        _               <- validateJobId(generatedId)
        _               <- checkNotAlreadyTaken(generatedId)
        job             <- EitherT.pure[Future, JobSubmitError](generateJob(toolName, generatedId, parts, user))
        isFromInstitute <- EitherT.pure[Future, JobSubmitError](user.getUserData.eMail.matches(".+@tuebingen.mpg.de"))
        _               <- EitherT.liftF(jobDao.insertJob(job))
        _               <- EitherT.liftF(assignJob(user, job))
        _               <- EitherT.pure[Future, JobSubmitError](send(generatedId, job, parts, isFromInstitute))
      } yield job
    }
  }

  private[this] def send(gid: String, job: Job, parts: Map[String, String], isFromInstitute: Boolean): Unit = {
    jobActorAccess.sendToJobActor(gid, PrepareJob(job, parts, startJob = false, isFromInstitute))
  }

  private[this] def modellerKeyIsValid(toolName: String, user: User): Boolean = {
    !(toolName == ToolName.MODELLER.value && !user.userConfig.hasMODELLERKey)
  }

  private[this] def assignJob(user: User, job: Job): Future[User] = {
    userDao.addJobsToUser(user.userID, List(job.jobID)).map {
      case Some(dbUser) =>
        userSessionService.updateUserInCache(dbUser)
      case None =>
        user
    }
  }

  private[this] def isJobId(id: String): Boolean = constants.jobIDVersionOptionPattern.pattern.matcher(id).matches

  private[this] def generateJobId(parts: Map[String, String]): EitherT[Future, JobSubmitError, String] = {
    if (!parts.contains("jobID")) {
      EitherT.liftF(jobIdProvider.runSafe.unsafeToFuture())
    } else {
      EitherT.rightT[Future, JobSubmitError](parts("jobID"))
    }
  }

  private[this] def validateJobId(jobId: String): EitherT[Future, JobSubmitError, Boolean] = {
    if (!isJobId(jobId)) {
      logger.warn("job id is invalid")
      EitherT.leftT[Future, Boolean](JobSubmitError.InvalidJobID)
    } else {
      EitherT.rightT[Future, JobSubmitError](true)
    }
  }

  private[this] def checkNotAlreadyTaken(jobId: String): EitherT[Future, JobSubmitError, Boolean] = {
    OptionT(jobDao.findJob(jobId)).toLeft[Boolean](true).leftMap(_ => JobSubmitError.AlreadyTaken)
  }

  private[this] def generateJob(
      toolName: String,
      jobID: String,
      form: Map[String, String],
      user: User
  ): Job = {
    val now = ZonedDateTime.now
    val dateDeletionOn = now.plusDays(
      if (user.isRegistered) constants.jobDeletionRegistered.toLong
      else constants.jobDeletion.toLong
    )
    new Job(
      jobID = jobID,
      ownerID = user.userID,
      parentID = form.get("parentID"),
      isPublic = form.get("isPublic").exists(_.toBoolean) || user.accountType == AccountType.NORMALUSER,
      emailUpdate = form.get("emailUpdate").exists(_.toBoolean),
      tool = toolName,
      watchList = List(user.userID),
      dateViewed = now,
      dateDeletionOn = dateDeletionOn,
      IPHash = Some(MessageDigest.getInstance("MD5").digest(user.sessionData.head.ip.getBytes).mkString)
    )
  }

}
