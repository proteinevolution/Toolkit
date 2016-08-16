package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}


import play.api.mvc._



@Singleton
class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef) extends Controller  {


  def updateJob = Action(BodyParsers.parse.json) { request =>




    Ok
  }
}



/*
def saveBook = Action(BodyParsers.parse.json) { request =>
    val b = request.body.validate[Book]
    b.fold(
      errors => {
        BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toFlatJson(errors)))
      },
      book => {
        addBook(book)
        Ok(Json.obj("status" -> "OK"))
}



 */

