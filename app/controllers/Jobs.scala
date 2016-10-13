package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}

import actors.JobManager
import models.database.Job
import play.api.Logger
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.mvc._
@Singleton
class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef) extends Controller  {

  <!-- @ Lukas please do not delete this Controller-->


  def updateJobStatus(jobID : String) = Action { request =>
    JobManager.UpdateJobStatus(jobID)
    Ok(jobID)
  }

}

