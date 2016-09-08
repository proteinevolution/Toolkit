package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager.Prepare
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import models.database.JobState.{Running, Done}
import models.database.Job
import models.search.JobDAO
import models.tools.ToolModel

import modules.tools.{ToolMatcher, FNV}
import play.api.cache._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONObjectID, BSONDocument}

import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import better.files._

import scala.util.{Failure, Success}

object Tool {

  lazy val tools:Seq[ToolModel] = ToolModel.values // this list is completely dynamic and depends only on the case objects in the tool model. frontend tools are excluded at the moment.

}


@Singleton
final class Tool @Inject()(val messagesApi      : MessagesApi,
                           @NamedCache("userCache") userCache : CacheApi,
                           val reactiveMongoApi : ReactiveMongoApi,
                           implicit val mat     : Materializer,
                           val jobDao           : JobDAO,
                           val toolMatcher      : ToolMatcher,
                           @Named("jobManager") jobManager : ActorRef) extends Controller with I18nSupport with UserSessions with Common {

  implicit val timeout = Timeout(5.seconds)


  def submit(toolName: String, start : Boolean, jobID : Option[String]) = Action.async { implicit request =>


    getUser(request, userCollection, userCache).flatMap { user =>
      // Fetch the job ID from the submission, might be the empty string
      //val jobID = request.body.asFormUrlEncoded.get("jobid").head --- There won't be a job ID in the request


      val form = toolMatcher.formMatcher(toolName)

      if (form.isEmpty)
        Future.successful(NotFound)

      else {
        val boundForm = form.get.bindFromRequest // <- params


        lazy val DB = boundForm.data.getOrElse("standarddb","").toFile  // get hold of the database in use
        lazy val jobByteArray = boundForm.data.toString().getBytes // convert params to hashable byte array
        lazy val inputHash = FNV.hash64(jobByteArray).toString()


        lazy val dbName = {
          boundForm.data.get("standarddb") match {
            case None => Some("none")
            case _ => Some(DB.name)
          }
        }

        lazy val dbMtime = {
          boundForm.data.get("standarddb") match {
            case None => Some("1970-01-01T00:00:00Z")
            case _ => Some("2016-08-09T12:46:51Z")
          }
        }



        lazy val hashQuery = jobDao.matchHash(inputHash, dbName, dbMtime)
        boundForm.fold(
          formWithErrors => {

            Future.successful(BadRequest("There was an error with the Form"))
          },

          _ =>
            hashQuery.flatMap { richSearchResponse =>
              println("success: " + richSearchResponse)
              println("hits: " + richSearchResponse.totalHits)

              val richJobList = richSearchResponse.getHits.getHits.toList.map { searchHit =>
                println(searchHit.getId)

                jobCollection.flatMap(_.find(BSONDocument(Job.IDDB -> BSONObjectID(searchHit.getId))).one[Job]).map {
                  case Some(oldJob) =>
                    if (oldJob.status != Done && oldJob.status != Running) {
                      println("job with same signature found but job failed, should submit the job again")
                      // we should delete failed jobs only here because keeping them is normally useful for debugging and statistics
                      jobCollection.flatMap(_.remove(BSONDocument(Job.IDDB -> BSONObjectID(searchHit.getId))))
                      None
                    } else {
                      println("job found: " + oldJob.tool)
                      Some(oldJob)
                    }

                  case None =>
                    println("[WARNING]: job in index but not in database")
                    None
                }
              }

              richJobList.head.map {
                // Found jobs, Prepare them but do not start them. Let the User decide what to do.
                case Some(job) =>
                  // TODO we should take care that the exact same job exists only once in the database
                  if(richJobList.tail.nonEmpty)
                    println("[WARNING]: " + richJobList.tail.length + " extra Jobs found.")

                  jobManager ! Prepare(user, jobID, toolName, boundForm.data, start = false)
                  Ok(Json.toJson(Json.obj("JobSubmitted" -> true,
                                          "JobStarted"   -> false,
                                          "IdenticalJob" -> List(Json.obj("mainID"   -> job.mainID.stringify,
                                                                          "job_id"   -> job.jobID,
                                                                          "state"    -> job.status,
                                                                          "toolname" -> job.tool))))
                  ).withSession(sessionCookie(request, user.sessionID.get))

                // Found no matching Jobs. Start the Job if needed.
                case None =>
                  jobManager ! Prepare(user, jobID, toolName, boundForm.data, start = start)
                  Ok(Json.toJson(Json.obj("JobSubmitted" -> true,
                                          "JobStarted"   -> start))
                  ).withSession(sessionCookie(request, user.sessionID.get))
              }
            })
      }
    }
  }
}


