package controllers

import akka.actor.ActorRef
import akka.pattern._
import javax.inject.{Inject, Named, Singleton}

import actors.JobManager
import actors.JobManager.UpdateJobStatus
import models.database.Job
import play.api.Logger
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
@Singleton
class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef) extends Controller  {

  
  def updateJobStatus(jobID : String) = Action { request =>
    jobManager ! UpdateJobStatus(BSONObjectID(jobID))
    Ok
  }

}