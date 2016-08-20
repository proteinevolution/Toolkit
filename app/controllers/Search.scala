package controllers

import play.api.libs.json.Json
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDao
import play.api.mvc.{Action, Controller}
import org.joda.time.DateTime

/**
 * Created by zin on 21.08.16.
 */

class Search  @Inject() (bookDao: JobDao) extends Controller {


  /*def get(bookId: String) = Action.async {
    jobDao.getJobById(jobId) map {
      case None => NotFound
      case Some(book) => Ok(Json.toJson(job))
    }
  }*/



}
