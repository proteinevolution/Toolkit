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
    val parts = form.dataParts.mapValues(_.mkString(constants.formMultiValueSeparator)) - "file"
    form.file("file").foreach { file =>
      val source = scala.io.Source.fromFile(file.ref.path.toFile)
      try {
        parts + ("alignment" -> source.getLines().mkString("\n"))
      } finally {
        source.close()
      }
    }
    // remove empty parameter
    parts.get("alignment_two").foreach { alignment =>
      if (alignment.isEmpty) parts - "alignment_two"
    }
    // Check if the user has the Modeller Key when the requested tool is Modeller
    if (toolName == ToolName.MODELLER.value && user.userConfig.hasMODELLERKey) {
      parts + ("regkey" -> constants.modellerKey)
    }
    for {
      jobId     <- EitherT.fromOption[Future](parts.get("jobID"), JobSubmitError.Undefined)
      checkedId <- EitherT(generateJobId(jobId))
      job       <- EitherT.fromOption[Future](generateJob(toolName, checkedId, parts, user), JobSubmitError.Undefined)
      _         <- EitherT.liftF(jobDao.insertJob(job))
      _         <- EitherT.liftF(assignJob(user, job))
    } yield {
      val isFromInstitute = user.getUserData.eMail.matches(".+@tuebingen.mpg.de")
      jobActorAccess.sendToJobActor(jobId, PrepareJob(job, parts, startJob = false, isFromInstitute))
      jobIdProvider.trash(jobId)
      job
    }
  }

  private def assignJob(user: User, job: Job): Future[Option[User]] = {
    userSessions.modifyUserWithCache(BSONDocument(User.IDDB   -> user.userID),
                                     BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> job.jobID)))
  }

  private def isJobId(id: String): Boolean = {
    id match {
      case constants.jobIDVersionOptionPattern(_, _) => true
      case _                                         => false
    }
  }

  private def generateJobId(jobId: String): Future[Either[JobSubmitError, String]] = {
    if (!isJobId(jobId)) {
      logger.warn("job id is invalid")
      Future.successful(Left(JobSubmitError.InvalidJobID))
    } else {
      jobDao.selectJob(jobId).map {
        case Some(_) => Right(jobIdProvider.provide)
        case None    => Right(jobId)
      }
    }
  }

  private def generateJob(
      toolName: String,
      jobID: String,
      form: Map[String, String],
      user: User
  ): Option[Job] = {
    val now = ZonedDateTime.now
    user.userData.map(_ => now.plusDays(constants.jobDeletion.toLong)).map { dateDeletion =>
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
        dateDeletion = Some(dateDeletion),
        IPHash = Some(MessageDigest.getInstance("MD5").digest(user.sessionData.head.ip.getBytes).mkString)
      )
    }
  }

  private def toBoolean(s: Option[String]): Boolean = {
    if (s.isDefined) true else false
  }

}
