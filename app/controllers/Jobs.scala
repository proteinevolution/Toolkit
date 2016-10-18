package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}

import actors.JobManager._
import models.database.JobState
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
@Singleton
class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef) extends Controller  {

  
  def jobStatusDone(mainID : String) = Action { request =>
    jobManager ! UpdateJobStatus(BSONObjectID(mainID), JobState.Done)
    Ok
  }
  def jobStatusError(mainID : String) = Action { request =>
    jobManager ! UpdateJobStatus(BSONObjectID(mainID), JobState.Error)
    Ok
  }
  def jobStatusRunning(mainID : String) = Action { request =>
    jobManager ! UpdateJobStatus(BSONObjectID(mainID), JobState.Running)
    Ok
  }
}