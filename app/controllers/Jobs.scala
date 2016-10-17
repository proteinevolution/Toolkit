package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}

import actors.JobManager._
import models.database.JobState
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
@Singleton
class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef) extends Controller  {

  
  def jobStatusDone(jobID : String) = Action { request =>
    jobManager ! UpdateJobStatus(BSONObjectID(jobID), JobState.Done)
    Ok
  }
  def jobStatusError(jobID : String) = Action { request =>
    jobManager ! UpdateJobStatus(BSONObjectID(jobID), JobState.Error)
    Ok
  }
  def jobStatusRunning(jobID : String) = Action { request =>
    jobManager ! UpdateJobStatus(BSONObjectID(jobID), JobState.Running)
    Ok
  }
}