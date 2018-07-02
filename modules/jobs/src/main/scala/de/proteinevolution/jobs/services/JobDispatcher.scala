package de.proteinevolution.jobs.services

import java.security.MessageDigest
import java.time.ZonedDateTime

import cats.data.EitherT
import cats.implicits._
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.jobs.actors.JobActor.PrepareJob
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.JobSubmitError
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.models.database.users.User
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.mvc.MultipartFormData
import play.api.libs.Files
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobDispatcher @Inject()(
    jobDao: JobDao,
    constants: ConstantsV2,
    jobIdProvider: JobIdProvider,
    jobActorAccess: JobActorAccess,
    userSessions: UserSessions
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def submitJob(
      toolName: String,
      form: MultipartFormData[Files.TemporaryFile],
      user: User
  ): EitherT[Future, JobSubmitError, Job] = {
    var parts = form.dataParts.mapValues(_.mkString(constants.formMultiValueSeparator)) - "file"
    form.file("file").foreach { file =>
      val source = scala.io.Source.fromFile(file.ref.path.toFile)
      try {
        parts = parts.updated("alignment", source.getLines().mkString("\n"))
      } finally {
        source.close()
      }
    }
    // remove empty parameter
    parts.get("alignment_two").foreach { alignment =>
      if (alignment.isEmpty) parts = parts - "alignment_two"
    }
    // Check if the user has the Modeller Key when the requested tool is Modeller
    if (toolName == ToolName.MODELLER.value && user.userConfig.hasMODELLERKey) {
      parts = parts.updated("regkey", constants.modellerKey)
    }
    for {
      generatedId <- generateJobId(parts)
      _           <- validateJobId(generatedId)
      _           <- EitherT(checkNotAlreadyTaken(generatedId))
    } yield {
      val job             = generateJob(toolName, generatedId, parts, user)
      val isFromInstitute = user.getUserData.eMail.matches(".+@tuebingen.mpg.de")
      jobActorAccess.sendToJobActor(generatedId, PrepareJob(job, parts, startJob = false, isFromInstitute))
      jobIdProvider.trash(generatedId)
      jobDao.insertJob(job)
      assignJob(user, job)
      job
    }
  }

  private def assignJob(user: User, job: Job): Future[Option[User]] = {
    userSessions.modifyUserWithCache(
      BSONDocument(User.IDDB   -> user.userID),
      BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> job.jobID))
    )
  }

  private def isJobId(id: String): Boolean = {
    id match {
      case constants.jobIDVersionOptionPattern(_, _) => true
      case _                                         => false
    }
  }

  private def generateJobId(parts: Map[String, String]): EitherT[Future, JobSubmitError, String] = {
    if (parts.get("jobID").isEmpty) {
      EitherT.rightT[Future, JobSubmitError](jobIdProvider.provide)
    } else {
      EitherT.rightT[Future, JobSubmitError](parts("jobID"))
    }
  }

  private def validateJobId(jobId: String): EitherT[Future, JobSubmitError, Boolean] = {
    if (!isJobId(jobId)) {
      logger.warn("job id is invalid")
      EitherT.leftT[Future, Boolean](JobSubmitError.InvalidJobID)
    } else {
      EitherT.rightT[Future, JobSubmitError](true)
    }
  }

  private def checkNotAlreadyTaken(jobId: String): Future[Either[JobSubmitError, Boolean]] = {
    jobDao.selectJob(jobId).map {
      case Some(_) => Left(JobSubmitError.AlreadyTaken)
      case None    => Right(true)
    }
  }

  private def generateJob(
      toolName: String,
      jobID: String,
      form: Map[String, String],
      user: User
  ): Job = {
    val now          = ZonedDateTime.now
    val dateDeletion = user.userData.map(_ => now.plusDays(constants.jobDeletion.toLong))
    new Job(
      jobID = jobID,
      ownerID = Some(user.userID),
      isPublic = form.get("public").isDefined || user.accountType == User.NORMALUSER,
      emailUpdate = toBoolean(form.get("emailUpdate")),
      tool = toolName,
      watchList = List(user.userID),
      dateCreated = Some(now),
      dateUpdated = Some(now),
      dateViewed = Some(now),
      dateDeletion = dateDeletion,
      IPHash = Some(MessageDigest.getInstance("MD5").digest(user.sessionData.head.ip.getBytes).mkString)
    )
  }

  private def toBoolean(s: Option[String]): Boolean = {
    if (s.isDefined) true else false
  }

}
