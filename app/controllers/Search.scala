package controllers

import play.api.libs.json.Json
import javax.inject.{Singleton, Inject}
import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDAO
import play.api.mvc.{Action, Controller}
import org.joda.time.DateTime


@Singleton
final class Search @Inject() (jobDao: JobDAO) extends Controller {


//  def getJob(jobId: String) = Action.async {
//    jobDao.getJobById(jobId) map {
//      case None => NotFound
//      case Some(job) => Ok(job.toString)
//    }
//  }

//  (TODO) this does not convert any output from the ES DB. see comment about function above as to why.
  def getJob(jobId: String) = Action.async {
    jobDao.getJobByIdAsJSObject(jobId) map {
      case None => NotFound
      case Some(job) => Ok(job)
    }
  }


  def searchJob(q: String) = Action.async {
    jobDao.searchByQueryString(q) map {
      case jobs if jobs.length > 0 =>
        Ok(Json.toJson(jobs)).withHeaders("X-Total-Count" -> jobs.length.toString)
      case empty => NoContent
    }
  }

 /* def checkExistantJob(job: String) = Action.async {

    NoContent

  }*/


}
