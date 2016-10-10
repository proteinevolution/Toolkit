package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}

import models.database.Job
import play.api.Logger
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.mvc._
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
        Logger.info("Job successfully received")
        Ok
      }
    ) */
    Ok
  }

}

