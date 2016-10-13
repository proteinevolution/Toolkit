package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager.Prepare
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import better.files._
import models.database.{Job, JobState}
import models.search.JobDAO
import models.tools.ToolModel
import modules.Common
import modules.tools.{FNV, ToolMatcher}
import play.api.Logger
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object Tool {

  lazy val tools:Seq[ToolModel] = ToolModel.values // this list is completely dynamic and depends only on the case objects in the tool model. frontend tools are excluded at the moment.

}


@Singleton
final class Tool @Inject()(val messagesApi      : MessagesApi,
                           @NamedCache("userCache") implicit val userCache : CacheApi,
                           val reactiveMongoApi : ReactiveMongoApi,
                           implicit val mat     : Materializer,
                           val jobDao           : JobDAO,
                           val toolMatcher      : ToolMatcher,
                           @Named("jobManager") jobManager : ActorRef) extends Controller with I18nSupport with UserSessions with Common {

  implicit val timeout = Timeout(5.seconds)


  def submit(toolName: String, start : Boolean, jobID : Option[String]) = Action.async { implicit request =>

    getUser.flatMap { user =>
      // Fetch the job ID from the submission, might be the empty string
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

            hashQuery.flatMap { richSearchResponse =>
              println("success: " + richSearchResponse)
              println("hits: " + richSearchResponse.totalHits)

              // Generate a list of hits and convert them into a list of future option jobs
              val mainIDs = richSearchResponse.getHits.getHits.toList.map { hit => BSONObjectID(hit.getId) }

              // Find the Jobs in the Database
              val futureJobs = jobCollection.map(_.find(BSONDocument(Job.IDDB ->
                                                        BSONDocument("$in" -> mainIDs))).cursor[Job]())

              // Collect the list
              futureJobs.flatMap(_.collect[List]()).map { jobList =>
                // all mainIDs from the DB
                val foundMainIDs   = jobList.map(_.mainID)

                // mainIDs which were not in the DB
                val unfoundMainIDs = mainIDs.filterNot(checkMainID => foundMainIDs contains checkMainID)

                // jobs with a partition of (Failed, NotFailed)
                val jobsPatition   = jobList.partition(_.status == JobState.Error)

                // Delete index-zombie jobs
                unfoundMainIDs.foreach { mainID =>
                  println("[WARNING]: job in index but not in database")
                  jobDao.deleteJob(mainID.stringify)
                }

                // Mark Failed Jobs
                jobsPatition._1.foreach { job =>
                  println("job with same signature found but job failed, should submit the job again")
                  //TODO we should delete failed jobs only here because keeping them is normally useful for debugging and statistics
                  //jobCollection.flatMap(_.remove(BSONDocument(Job.IDDB -> job.mainID))  // would only delete the database entry
                  //jobManager ! DeleteJob(user.userID, job.mainID)                       // deletes all files and database entries
                }

                jobsPatition._2.headOption match {

                  //  Identical job has been found
                  case Some(job) =>
                    Ok(Json.obj("jobSubmitted"  -> true,
                                "jobStarted"    -> false,
                                "identicalJobs" -> true,
                                "job"           -> job.cleaned(),
                                "mainID"        -> job.mainID.stringify)
                    ).withSession(sessionCookie(request, user.sessionID.get))

                  // No identical job submission, a new Job Instance will be prepared
                  case None =>
                    val newMainID = BSONObjectID.generate()
                    jobManager ! Prepare(user, jobID, newMainID, toolName, boundForm.data, start = start)
                    Ok(Json.obj("jobSubmitted"  -> true,
                                "jobStarted"    -> start,
                                "identicalJobs" -> false,
                                "mainID"        -> newMainID.stringify)
                    ).withSession(sessionCookie(request, user.sessionID.get))
                }
              }
            }
      }
    }
  }
}


