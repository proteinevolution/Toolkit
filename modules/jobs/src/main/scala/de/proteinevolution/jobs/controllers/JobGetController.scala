package de.proteinevolution.jobs.controllers

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.db.MongoStore
import de.proteinevolution.jobs.models.JobHashError
import de.proteinevolution.jobs.services.JobHashService
import de.proteinevolution.models.database.jobs.Job
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext

class JobGetController @Inject()(
    jobHashService: JobHashService,
    userSessions: UserSessions,
    mongoStore: MongoStore,
    cc: ControllerComponents
)(implicit ec: ExecutionContext, config: Configuration)
    extends ToolkitController(cc) {

  def listJobs: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      mongoStore.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$in" -> user.jobs))).map { jobs =>
        Ok(Json.toJson(jobs.map(_.cleaned())))
      }
    }
  }

  def loadJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { _ =>
      mongoStore.selectJob(jobID).map {
        case Some(job) => Ok(job.cleaned())
        case None      => NotFound
      }
    }
  }

  def checkHash(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    (for {
      _   <- OptionT.liftF(userSessions.getUser)
      job <- jobHashService.checkHash(jobID)
      if job.dateCreated.isDefined
    } yield {
      (job.jobID, job.dateCreated.get.toInstant.toEpochMilli)
    }).value.map {
      case Some((latestJobId, dateCreated)) =>
        Ok(Json.obj("jobID" -> latestJobId, "dateCreated" -> dateCreated))
      case None => NotFound(JobHashError.JobNotFound.msg)
    }
  }

}
