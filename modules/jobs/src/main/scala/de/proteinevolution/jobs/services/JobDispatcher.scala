package de.proteinevolution.jobs.services

import java.security.MessageDigest
import java.time.ZonedDateTime

import better.files._
import cats.data.EitherT
import cats.implicits._
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.jobs.actors.JobActor.PrepareJob
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.{ Job, JobSubmitError }
import de.proteinevolution.models.database.users.User
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.libs.Files
import play.api.mvc.MultipartFormData
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class JobDispatcher @Inject()(
    jobDao: JobDao,
    constants: ConstantsV2,
    jobIdProvider: JobIdProvider,
    jobActorAccess: JobActorAccess,
    userSessions: UserSessions
)(implicit ec: ExecutionContext) {

  private[this] val logger = Logger(this.getClass)

  def submitJob(
      toolName: String,
      dataParts: Map[String, Seq[String]],
      filePart: Option[MultipartFormData.FilePart[Files.TemporaryFile]],
      user: User
  ): EitherT[Future, JobSubmitError, Job] = {
    if (!modellerKeyIsValid(toolName, user)) {
      EitherT.leftT(JobSubmitError.ModellerKeyInvalid)
    } else {
      for {
        parts           <- EitherT.pure[Future, JobSubmitError](readForm(dataParts, filePart))
        generatedId     <- generateJobId(parts)
        _               <- validateJobId(generatedId)
        _               <- EitherT(checkNotAlreadyTaken(generatedId))
        job             <- EitherT.pure[Future, JobSubmitError](generateJob(toolName, generatedId, parts, user))
        isFromInstitute <- EitherT.pure[Future, JobSubmitError](user.getUserData.eMail.matches(".+@tuebingen.mpg.de"))
        _               <- EitherT.liftF(jobDao.insertJob(job))
        _               <- EitherT.liftF(assignJob(user, job))
        _               <- EitherT.pure[Future, JobSubmitError](jobIdProvider.trash(generatedId))
        _               <- EitherT.pure[Future, JobSubmitError](send(generatedId, job, parts, isFromInstitute))
      } yield job
    }
  }

  private[this] def readForm(
      dataParts: Map[String, Seq[String]],
      filePart: Option[MultipartFormData.FilePart[Files.TemporaryFile]]
  ): Map[String, String] = {
    filePart
      .map { file =>
        val lines = File(file.ref.path).newInputStream.autoClosed.map(_.lines.mkString("\n")).get()
        val form  = dataParts.mapValues(_.mkString(constants.formMultiValueSeparator)) - "file"
        if (("alignment" :: "alignment_two" :: Nil).contains(file.key) && lines.nonEmpty) {
          form.updated(file.key, lines)
        } else { form }
      }
      .getOrElse(Map.empty)
      .collect {
        case (k, v) if v.nonEmpty => (k, v)
      }
  }

  private[this] def send(gid: String, job: Job, parts: Map[String, String], isFromInstitute: Boolean): Unit = {
    jobActorAccess.sendToJobActor(gid, PrepareJob(job, parts, startJob = false, isFromInstitute))
  }

  private[this] def modellerKeyIsValid(toolName: String, user: User): Boolean = {
    !(toolName == ToolName.MODELLER.value && !user.userConfig.hasMODELLERKey)
  }

  private[this] def assignJob(user: User, job: Job): Future[Option[User]] = {
    userSessions.modifyUserWithCache(
      BSONDocument(User.IDDB   -> user.userID),
      BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> job.jobID))
    )
  }

  private[this] def isJobId(id: String): Boolean = constants.jobIDVersionOptionPattern.pattern.matcher(id).matches

  private[this] def generateJobId(parts: Map[String, String]): EitherT[Future, JobSubmitError, String] = {
    if (parts.get("jobID").isEmpty) {
      EitherT.liftF(jobIdProvider.provide)
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

  private[this] def checkNotAlreadyTaken(jobId: String): Future[Either[JobSubmitError, Boolean]] = {
    jobDao.selectJob(jobId).map {
      case Some(_) => Left(JobSubmitError.AlreadyTaken)
      case None    => Right(true)
    }
  }

  private[this] def generateJob(
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
      parentID = form.get("parent_id"),
      isPublic = form.get("public").isDefined || user.accountType == User.NORMALUSER,
      emailUpdate = form.get("emailUpdate").isDefined,
      tool = toolName,
      watchList = List(user.userID),
      dateCreated = Some(now),
      dateUpdated = Some(now),
      dateViewed = Some(now),
      dateDeletion = dateDeletion,
      IPHash = Some(MessageDigest.getInstance("MD5").digest(user.sessionData.head.ip.getBytes).mkString)
    )
  }

}
