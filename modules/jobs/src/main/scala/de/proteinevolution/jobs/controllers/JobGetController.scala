package de.proteinevolution.jobs.controllers

import java.time.ZonedDateTime

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.JobHashError
import de.proteinevolution.jobs.services.{ JobFolderValidation, JobHashService }
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.services.ToolConfig
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext
import io.circe.JsonObject
import io.circe.syntax._

@Singleton
class JobGetController @Inject()(
    jobHashService: JobHashService,
    userSessions: UserSessions,
    jobDao: JobDao,
    cc: ControllerComponents,
    toolConfig: ToolConfig,
    constants: ConstantsV2
)(implicit ec: ExecutionContext, config: Configuration)
    extends ToolkitController(cc)
    with JobFolderValidation {

  def jobManagerListJobs: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      jobDao.findJobs(BSONDocument(Job.OWNERID -> user.userID, Job.DELETION -> BSONDocument("$exists" -> false))).map {
        jobs =>
          NoCache(Ok(jobs.filter(job => jobFolderIsValid(job.jobID, constants)).map(_.jobManagerJob())))
      }
    }
  }

  def listJobs: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      jobDao.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$in" -> user.jobs))).map { jobs =>
        Ok(jobs.filter(job => jobFolderIsValid(job.jobID, constants)).map(_.cleaned(toolConfig)))
      }
    }
  }

  def loadJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { _ =>
      jobDao.selectJob(jobID).map {
        case Some(job) if jobFolderIsValid(job.jobID, constants) => Ok(job.cleaned(toolConfig).asJson)
        case _                                                   => NotFound
      }
    }
  }

  def checkHash(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    (for {
      _   <- OptionT.liftF(userSessions.getUser)
      job <- jobHashService.checkHash(jobID)
    } yield {
      (job.jobID, job.dateCreated.getOrElse(ZonedDateTime.now).toInstant.toEpochMilli)
    }).value.map {
      case Some((latestJobId, dateCreated)) =>
        Ok(JsonObject("jobID" -> latestJobId.asJson, "dateCreated" -> dateCreated.asJson))
      case None => NotFound(errors(JobHashError.JobNotFound.msg))
    }
  }

}
