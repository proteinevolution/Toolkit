package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager.{StartJob, Prepare}
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import better.files._

import models.database._
import models.search.JobDAO
import models.tools.ToolModel
import modules.CommonModule
import modules.tools.FNV
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.ReactiveMongoApi

import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
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
                           @Named("jobManager") jobManager : ActorRef) extends Controller with I18nSupport with UserSessions with CommonModule {

  implicit val timeout = Timeout(5.seconds)


  def submit(toolname: String, mainID: String, jobID : String) = Action.async { implicit request =>

    getUser.flatMap { user =>

      val boundForm = ToolModel.jobForm.bindFromRequest



      lazy val DB = boundForm.data.getOrElse("standarddb","").toFile  // get hold of the database in use


      lazy val hashParams = boundForm.data - "mainID" - "jobID" // don't hash the mainID or the jobID

      lazy val jobByteArray = hashParams.toString().getBytes // convert params to hashable byte array
      lazy val inputHash = FNV.hash64(jobByteArray).toString()

      println("job hash: " + inputHash)


      lazy val dbName = {
        boundForm.data.get("standarddb") match {
          case None => Some("none")
          case _ => Some(DB.name)
        }
      }

      lazy val dbMtime = {
        boundForm.data.get("standarddb") match {
          case None => Some("1970-01-01T00:00:00Z")
          case _ => Some(DB.lastModifiedTime.toString)
        }
      }


      jobDao.matchHash(inputHash, dbName, dbMtime).flatMap { richSearchResponse =>
        println("success: " + richSearchResponse)
        println("hits: " + richSearchResponse.totalHits)

        // Generate a list of hits and convert them into a list of future option jobs
        val mainIDs = richSearchResponse.getHits.getHits.toList.map { hit =>
          BSONObjectID.parse(hit.getId).getOrElse(BSONObjectID.generate()) // Not optimal, as a fake Object ID is generated, but apply(id : String) was deprecated
        }



        // Find the Jobs in the Database
        findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs))).map { jobList =>
          // all mainIDs from the DB
          val foundMainIDs   = jobList.map(_.mainID)

          // mainIDs which were not in the DB
          val unFoundMainIDs = mainIDs.filterNot(checkMainID => foundMainIDs contains checkMainID)

          // jobs with a partition of (Failed, NotFailed)
          val jobsPartition   = jobList.partition(_.status == Error)

          // Delete index-zombie jobs
          unFoundMainIDs.foreach { mainID =>
            println("[WARNING]: job in index but not in database: " + mainID.stringify)
            jobDao.deleteJob(mainID.stringify)
          }

          // Mark Failed Jobs
          //jobManager ! DeleteJobs(user.userID, jobsPatition._1.map(_.mainID), JobDeletionFlag.Automated) // marks all database entries with failed jobs for deletion
          /*
          jobsPatition._1.foreach { job =>
            println("job with same signature found but job failed, should submit the job again")
            //TODO we should delete failed jobs only here because keeping them is normally useful for debugging and statistics
            //jobCollection.flatMap(_.remove(BSONDocument(Job.IDDB -> job.mainID))  // would only delete the database entry
          }
          */
          val newMainID = BSONObjectID.parse(mainID).get

          jobsPartition._2.headOption match {
            //  Identical job has been found
            case Some(job) =>

              Ok(Json.obj("jobSubmitted"  -> true,
                "jobStarted"    -> false,
                "existingJobs"  -> true,
                "existingJob"   -> job.cleaned(),
                "mainID"        -> newMainID.stringify)
              ).withSession(sessionCookie(request, user.sessionID.get))

            // No identical job submission, Start the job right away.
            case None =>
              // Prepare the Job

              jobManager ! Prepare(user, jobID, newMainID, toolname, boundForm.data)
              jobManager ! StartJob(user.userID, newMainID)
              Ok(Json.obj("jobSubmitted"  -> true,
                "identicalJobs" -> false,
                "existingJobs"  -> false,
                "mainID"        -> newMainID.stringify)
              ).withSession(sessionCookie(request, user.sessionID.get))
          }
        }
      }
    }
  }



  def submitAgain(toolname: String, mainID: String, jobID : String) = Action.async { implicit request =>

    getUser.flatMap { user =>

      val boundForm = ToolModel.jobForm.bindFromRequest

      // Prepare the Job
      val newMainID = BSONObjectID.parse(mainID).get
      jobManager ! Prepare(user, jobID, newMainID, toolname, boundForm.data)
      jobManager ! StartJob(user.userID, newMainID)
      Future.successful(Ok)
    }
  }

}