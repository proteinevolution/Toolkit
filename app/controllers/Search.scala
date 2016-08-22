package controllers

import play.api.libs.json.Json
import javax.inject.{Singleton, Inject}
import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDAO
import play.api.mvc.{Action, Controller}
import org.joda.time.DateTime


@Singleton
final class Search @Inject() (jobDao: JobDAO) extends Controller {


  def getJob(jobId: String) = Action.async {
    jobDao.getJobById(jobId) map {
      case None => NotFound
      case Some(job) => Ok(Json.toJson(job))
    }
  }


  def searchJob(q: String) = Action.async {
    jobDao.searchByQueryString(q) map {
      case jobs if jobs.length > 0 =>
        Ok(Json.toJson(jobs)).withHeaders("X-Total-Count" -> jobs.length.toString)
      case empty => NoContent
    }
  }


}
