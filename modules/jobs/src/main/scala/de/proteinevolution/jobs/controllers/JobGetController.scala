package de.proteinevolution.jobs.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.db.MongoStore
import de.proteinevolution.models.database.jobs.Job
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext

class JobGetController @Inject()(userSessions: UserSessions, mongoStore: MongoStore, cc: ControllerComponents)(
    implicit ec: ExecutionContext,
    config: Configuration
) extends ToolkitController(cc) {

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

}
