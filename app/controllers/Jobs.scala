package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}
import reactivemongo.bson._
import actors.JobManager._
import models.database.JobState
import play.api.mvc._
@Singleton
class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef) extends Controller  {

  def jobStatusQueued(mainID : String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, JobState.Queued)
    Ok
  }

  def jobStatusDone(mainID : String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, JobState.Done)
    Ok
  }
  def jobStatusError(mainID : String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, JobState.Error)
    Ok
  }
  def jobStatusRunning(mainID : String) = Action { request =>
    jobManager ! UpdateJobStatus(reactivemongo.bson.BSONObjectID.parse(mainID).get, JobState.Running)
    Ok
  }
}