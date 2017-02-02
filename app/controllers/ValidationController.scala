package controllers

import javax.inject.{Inject, Singleton}

import models.tools.ToolFactory
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
  *  Controller meant to validate the parameter input of a specific tool and report back if the validation has failed
  *
  * Created by lzimmermann on 02.02.17.
  */
@Singleton
class ValidationController @Inject() (toolFactory: ToolFactory) extends Controller{

  def validate(toolname: String, param: String, value: String) = Action {
      val errors = toolFactory.values(toolname).params(param).validators.foldLeft(Seq.empty[String]) { (a, b) =>
        b(value) match {
          case Some(error) => a.+:(error)
          case None =>  a
        }
      }
     if(errors.isEmpty) {
       Ok
     } else {
      Ok(Json.toJson(errors))
     }
  }
}
