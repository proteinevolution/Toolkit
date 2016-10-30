package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}

import actors.JobManager._
import actors.UserManager.RunningJobMessage
import models.database.JobState
import play.api.mvc._

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
class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef,
                     @Named("userManager") userManager : ActorRef) extends Controller {

  def jobStatusDone(mainID: String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, JobState.Done)
    Ok
  }

  def jobStatusError(mainID: String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, JobState.Error)
    Ok
  }

  def jobStatusRunning(mainID: String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, JobState.Running)
    Ok
  }

  def jobStatusQueued(mainID: String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, JobState.Queued)
    Ok
  }

  def SGEID(mainID: String, sgeID: String) = Action { request =>
    jobManager ! AddSGEjobID(reactivemongo.bson.BSONObjectID.parse(mainID).get, sgeID)
    Ok

  }

  def pushMessage(mainID : String, message : String)  = Action { request =>
    userManager ! RunningJobMessage(reactivemongo.bson.BSONObjectID.parse(mainID).get, message)
    Ok
  }
}