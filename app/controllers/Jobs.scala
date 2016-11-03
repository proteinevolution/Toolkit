package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}

import actors.JobManager._
import actors.UserManager.RunningJobMessage
import models.database._
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
final class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef,
                     @Named("userManager") userManager : ActorRef) extends Controller {

  def jobStatusDone(mainID: String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, Done)
    Ok
  }

  def jobStatusError(mainID: String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, Error)
    Ok
  }

  def jobStatusRunning(mainID: String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, Running)
    Ok
  }

  def jobStatusQueued(mainID: String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, Queued)
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

  def updateDateViewed(mainID : String)  = Action { request =>
    jobManager ! UpdateDateViewed(reactivemongo.bson.BSONObjectID.parse(mainID).get)
    Ok
  }
}