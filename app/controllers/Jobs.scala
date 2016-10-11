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



  def updateJob = Action(BodyParsers.parse.json) { request =>


    /*
    val j = request.body.validate[Job]

    j.fold(

      errors => {
        BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toJson(errors)))
      },
      job => {

        Ok
      }
    ) */
    Logger.info("Job successfully received")
    Ok
  }

  def updateJobStatus(jobID : String) = Action { request =>
    jobManager ! UpdateJobStatus(BSONObjectID(jobID))
    Ok
  }

}

