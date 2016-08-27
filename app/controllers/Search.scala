package controllers

import play.api.libs.json.Json
import javax.inject.{Singleton, Inject}
import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDAO
import play.api.mvc.{Action, Controller}
import org.joda.time.DateTime


@Singleton
final class Search @Inject() (jobDao: JobDAO) extends Controller {


  // TODO more actions from the views


}
