package controllers

import javax.inject.{Inject,Singleton}

import actors.JobActor.JobStateChanged
import models.database._
import models.job.JobActorAccess
import modules.CommonModule
import org.joda.time.DateTime
import play.api.Logger
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
                           val reactiveMongoApi: ReactiveMongoApi) extends Controller with CommonModule {

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


  def SGEID(jobID: String, sgeID: String) = Action {

    findJob(BSONDocument(Job.JOBID -> jobID)).foreach {

      case Some(job) =>
        modifyJob(BSONDocument(Job.JOBID -> job.jobID),
          BSONDocument("$set" -> BSONDocument(Job.CLUSTERDATA -> JobClusterData(sgeID,Some(1),Some(1)))))
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

  def updateDateViewed(jobID : String)  = Action {

    modifyJob(BSONDocument(Job.JOBID -> jobID),
      BSONDocument("$set"   -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(DateTime.now().getMillis))))
    Ok
  }

  /**
    *
    * Creates new annotation document and modifies this if it already exists in one method
    * TODO: make this secure against CSRF
    * @param jobID
    * @param content
    * @return
    */

  def annotation(jobID : String, content : String) : Action[AnyContent] = Action {

    val entry = JobAnnotation(mainID = BSONObjectID.generate(),
                              jobID = jobID,
                              content = content,
                              dateCreated = Some(DateTime.now()))

    upsertAnnotation(entry)

    modifyAnnotation(BSONDocument(JobAnnotation.JOBID -> jobID),
      BSONDocument("$set"   -> BSONDocument(JobAnnotation.CONTENT -> content)))


    Ok("annotation upserted")

  }


  def getAnnotation(jobID: String): Action[AnyContent] = Action.async { implicit request =>

    findJobAnnotation(BSONDocument(JobAnnotation.JOBID -> jobID)).map { annotationList =>
      val foundAnnotations = annotationList.map(_.content)

      Ok(foundAnnotations.getOrElse(""))

    }

  }

}