package controllers

import javax.inject.{Inject, Singleton}

import actors.JobActor.{JobStateChanged, UpdateLog}
import models.database.jobs._
import models.job.JobActorAccess
import modules.{CommonModule, LocationProvider}
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global


/*
TODO
We can introduce auto-coercion of the Job MainID to the BSONObject ID
 */
/**
  * This controller is supposed to handle request coming from the Backend, such as compute
  * nodes from a gridengine.
  *
  */
@Singleton
final class Jobs @Inject()(jobActorAccess: JobActorAccess,
                           @NamedCache("userCache") implicit val userCache : CacheApi,
                           implicit val locationProvider : LocationProvider,
                           val reactiveMongoApi: ReactiveMongoApi) extends Controller with CommonModule with UserSessions {

  def jobStatusDone(jobID: String) = Action {
    jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Done))
    Ok
  }

  def jobStatusError(jobID: String) = Action {
    jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Error))
    Ok
  }

  def jobStatusRunning(jobID: String) = Action {
    jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Running))
    Ok
  }

  def jobStatusQueued(jobID: String) = Action {
    jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Queued))
    Ok
  }

  def updateLog(jobID: String) = Action {
    jobActorAccess.sendToJobActor(jobID, UpdateLog(jobID))
    Ok
  }

  //TODO make <strike>america</strike> Jobs <strike>great</strike> secure again!
  def SGEID(jobID: String, sgeID: String) = Action {

    findJob(BSONDocument(Job.JOBID -> jobID)).foreach {

      case Some(job) =>
        modifyJob(BSONDocument(Job.JOBID -> job.jobID),
          BSONDocument("$set" -> BSONDocument("clusterData.sgeid" -> sgeID)))
        Logger.info(jobID + " gets job-ID " + sgeID + " on SGE")
      case None =>
        Logger.info("Unknown ID " + jobID.toString)
    }

    Ok

  }

  def pushMessage(jobID : String, message : String)  = Action {
    //userManager ! RunningJobMessage(reactivemongo.bson.BSONObjectID.parse(jobID).get, message)
    Ok
  }


  // TODO make secure

  def updateDateViewed(jobID : String)  = Action {

    modifyJob(BSONDocument(Job.JOBID -> jobID),
      BSONDocument("$set"   -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(DateTime.now().getMillis))))
    Ok
  }

  /**
    *
    * Creates new annotation document and modifies this if it already exists in one method
    *
 *
    * @param jobID
    * @param content
    * @return
    */

  def annotation(jobID : String, content : String) : Action[AnyContent] = Action.async { implicit request =>

    getUser.flatMap { user =>

      findJob(BSONDocument(Job.JOBID -> jobID)).map {

        case x if x.get.ownerID.get == user.userID =>


          val entry = JobAnnotation(mainID = BSONObjectID.generate(),
            jobID = jobID,
            content = content,
            dateCreated = Some(DateTime.now()))

          upsertAnnotation(entry)

          modifyAnnotation(BSONDocument(JobAnnotation.JOBID -> jobID),
            BSONDocument("$set" -> BSONDocument(JobAnnotation.CONTENT -> content)))
          Ok("annotation upserted")

        case _ =>

          Logger.info("Unknown ID " + jobID.toString)
          BadRequest("Permission denied")

      }
    }

  }


  def getAnnotation(jobID: String): Action[AnyContent] = Action.async { implicit request =>

    getUser.flatMap { user =>

      findJobAnnotation(BSONDocument(JobAnnotation.JOBID -> jobID)).flatMap {

        case Some(x) =>

          findJob(BSONDocument(Job.JOBID -> jobID)).map { jobList =>

            if (jobList.get.ownerID.get == user.userID) {

              Ok(x.content)

            } else BadRequest("Permission denied")

          }

        case None =>

          findJob(BSONDocument(Job.JOBID -> jobID)).map { jobList =>

            if (jobList.get.ownerID.get == user.userID) {

              Ok

            } else BadRequest("Permission denied")

          }

      }
    }
  }
}